package com.cheerslife.updateservice.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

import com.cheerslife.updateservice.App;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CrashLogService {

    public interface OnCrashedListener {
        void onCrashed(String log);
    }

    public static final String PATH = Environment.getExternalStorageDirectory()
            .getPath().concat(File.separator).concat("_crash")
            .concat(File.separator);

    private static DateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
    private static CrashLogService instance = new CrashLogService();

    public static synchronized CrashLogService getInstance() {
        return instance;
    }

    UncaughtExceptionHandler mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    Map<String, String> infos = new HashMap();
    OnCrashedListener listener;

    private CrashLogService() {
        UncaughtExceptionHandler errorHandler = (thread, ex) -> {
            handleException(ex);
            //程序不处理的话交给系统默认的方式处理异常
            mDefaultHandler.uncaughtException(thread, ex);
        };
        Thread.setDefaultUncaughtExceptionHandler(errorHandler);
    }

    public void setOnCrashedListener(OnCrashedListener l) {
        listener = l;
    }

    private void handleException(Throwable ex) {
        if (ex == null) {
            return;
        }
        collectDeviceInfo(App.getApp());
        String log = saveCrashInfo2File(ex);
        onCrashLog(log);
    }

    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String saveCrashInfo2File(Throwable ex) {
        String RN = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append(RN);
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".txt";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {

                FileUtils.writeFile(PATH + fileName, sb.toString(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void onCrashLog(String log) {
        if (listener != null) {
            listener.onCrashed(log);
        }
    }

}
