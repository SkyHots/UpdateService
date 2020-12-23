package com.cheerslife.updateservice.utils;

import android.os.Environment;
import android.text.TextUtils;


import com.cheerslife.updateservice.App;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogFile {
    static private ExecutorService singleThreadExecutor;

    public final static String LOGFILE_PATH = Environment.getExternalStorageDirectory()
            .getPath().concat(File.separator).concat("_LOG").concat(File.separator);
    public static final SimpleDateFormat SDF_DATE_3 = new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss", Locale.getDefault());

    /**
     * 错误日志保存，，content：错误内容  isShowInApp 是否显示在APP消息板块
     */
    public synchronized static void saveLog(String content) {
        if (singleThreadExecutor == null)
            singleThreadExecutor = Executors.newSingleThreadExecutor();
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        StringBuffer buffer = new StringBuffer("[").append(traceElement.getFileName())
                .append(" | ").append(traceElement.getLineNumber()).append(" | ")
                .append(traceElement.getMethodName()).append("()").append("]");
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(content))
                    return;
                String format = SDF_DATE_3.format(new Date(App.mTime));
                String path = LOGFILE_PATH + format + ".txt";
                String TAG = "\r\n" + SDF_DATE_3.format(new Date()) + "----->" + buffer.toString() + "\r\n";
                FileUtils.writeFile(path, TAG + content, true);
            }
        });
    }

    public static void asynSaveLog(String content) {
        saveLog(content);
    }

}