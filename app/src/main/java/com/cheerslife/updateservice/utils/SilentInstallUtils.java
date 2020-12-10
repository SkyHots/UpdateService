package com.cheerslife.updateservice.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.UriUtils;
import com.blankj.utilcode.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;


public final class SilentInstallUtils {

    private static final String TAG = "SilentInstallUtils";

    private static final String SP_NAME_PACKAGE_INSTALL_RESULT = "package_install_result";
    private static volatile Method sInstallPackage;
    private static volatile Method sDeletePackage;
    private static volatile SharedPreferences sPreferences;

    /**
     * 静默安装
     * 会依次调用Stream-->反射-->Shell
     *
     * @param apkFile APK文件
     * @return 成功或失败
     */
    @SuppressLint("PackageManagerGetSignatures")
    @RequiresPermission(Manifest.permission.INSTALL_PACKAGES)
    public static synchronized boolean install(Context context, String apkFile) throws InterruptedException {
        File file;
        if (TextUtils.isEmpty(apkFile) || !(file = new File(apkFile)).exists())
            return false;
        context = context.getApplicationContext();
        //加上apk合法性判断
        AppUtils.AppInfo apkInfo = AppUtils.getApkInfo(file);
        if (apkInfo == null || TextUtils.isEmpty(apkInfo.getPackageName())) {
            LogUtils.iTag(TAG, "apk info is null, the file maybe damaged: " + file.getAbsolutePath());
            return false;
        }

        //加上本地apk版本判断
        AppUtils.AppInfo appInfo = AppUtils.getAppInfo(apkInfo.getPackageName());
        if (appInfo != null) {

            //已安装的版本比apk版本要高, 则不需要安装
            if (appInfo.getVersionCode() >= apkInfo.getVersionCode()) {
                LogUtils.iTag(TAG, "The latest version has been installed locally: " + file.getAbsolutePath(),
                        "app info: packageName: " + appInfo.getPackageName() + "; app name: " + appInfo.getName(),
                        "apk version code: " + apkInfo.getVersionCode(),
                        "app version code: " + appInfo.getVersionCode());
                return true;
            }

            //已安装的版本比apk要低, 则需要进一步校验签名和ShellUID

            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo appPackageInfo, apkPackageInfo;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    appPackageInfo = pm.getPackageInfo(appInfo.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                    apkPackageInfo = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_SIGNING_CERTIFICATES);
                } else {
                    appPackageInfo = pm.getPackageInfo(appInfo.getPackageName(), PackageManager.GET_SIGNATURES);
                    apkPackageInfo = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_SIGNATURES);
                }

                if (appPackageInfo != null && apkPackageInfo != null &&
                        !compareSharedUserId(appPackageInfo.sharedUserId, apkPackageInfo.sharedUserId)) {
                    LogUtils.wTag(TAG, "Apk sharedUserId is not match",
                            "app shellUid: " + appPackageInfo.sharedUserId,
                            "apk shellUid: " + apkPackageInfo.sharedUserId);
                    return false;
                }

            } catch (Throwable ignored) {

            }


        }
        //        try {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //由于调用PackageInstaller安装失败的情况下, 重复安装会导致内存占用无限增长的问题.
            //所以在安装之前需要判断当前包名是否有过失败记录, 如果以前有过失败记录, 则不能再使用该方法进行安装
            if (sPreferences == null) {
                sPreferences = context.getSharedPreferences(SP_NAME_PACKAGE_INSTALL_RESULT, Context.MODE_PRIVATE);
            }
            String packageName = apkInfo.getPackageName();
            boolean canInstall = sPreferences.getBoolean(packageName, true);
            if (canInstall) {
                boolean success = installByPackageInstaller(context, file, apkInfo);
                sPreferences.edit().putBoolean(packageName, success).apply();
                if (success) {
                    LogUtils.iTag(TAG, "Install Success[PackageInstaller]: " + file.getAbsolutePath());
                    return true;
                }
            }
        }

        if (installByReflect(context, file)) {
            if (sPreferences != null)
                sPreferences.edit().putBoolean(apkInfo.getPackageName(), true).apply();
            LogUtils.iTag(TAG, "Install Success[Reflect]", file.getPath());
            return true;
        }

        if (installByShell(file, DeviceUtils.isDeviceRooted())) {
            if (sPreferences != null)
                sPreferences.edit().putBoolean(apkInfo.getPackageName(), true).apply();
            LogUtils.iTag(TAG, "Install Success[Shell]", file.getPath());
            return true;
        }
        //        } catch (InterruptedException e) {
        //            throw e;
        //        } catch (Throwable e) {
        //            e.printStackTrace();
        //            LogUtils.wTag(TAG, e);
        //        }
        LogUtils.iTag(TAG, "Install Failure: " + file.getAbsolutePath());
        return false;
    }


    /**
     * 卸载
     * PackageInstaller-->反射-->Shell
     */
    @RequiresPermission(Manifest.permission.DELETE_PACKAGES)
    public static synchronized boolean uninstall(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;

        //如果未安装, 直接返回成功即可
        if (!AppUtils.isAppInstalled(packageName))
            return true;

        //如果是系统app, 则不支持卸载
        AppUtils.AppInfo appInfo = AppUtils.getAppInfo(packageName);
        if (appInfo != null && appInfo.isSystem()) {
            LogUtils.iTag(TAG, "Uninstall Failure[System App]: " + packageName);
            return false;
        }

        context = context.getApplicationContext();
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (uninstallByPackageInstaller(context, packageName)) {
                    LogUtils.iTag(TAG, "Uninstall Success[PackageInstaller]: " + packageName);
                    return true;
                }
            }

            if (uninstallByReflect(context, packageName)) {
                LogUtils.iTag(TAG, "Uninstall Success[Reflect]: " + packageName);
                return true;
            }

            if (uninstallByShell(packageName, DeviceUtils.isDeviceRooted())) {
                LogUtils.iTag(TAG, "Uninstall Success[Shell]: " + packageName);
                return true;
            }

        } catch (Throwable ignored) {
        }

        LogUtils.iTag(TAG, "Uninstall Failure: " + packageName);
        return false;
    }


    /**
     * 通过Shell命令静默安装
     *
     * @param file   apk文件
     * @param isRoot 设备是否有root权限,
     *               如果没有root权限, 在Android7.0及以上设备需要声明 android:sharedUserId="android.uid.shell"
     *               Android 9.0 设备可能不支持shell命令安装
     * @return 是否安装成功
     */
    private static boolean installByShell(File file, boolean isRoot) {
        String cmd = "pm install -r '" + file.getAbsolutePath() + "'";
        return executeShell(cmd, isRoot) || executeShell(cmd, !isRoot);
    }

    private static boolean uninstallByShell(String packageName, boolean isRoot) {
        String cmd = "pm uninstall '" + packageName + "'";
        return executeShell(cmd, isRoot) || executeShell(cmd, !isRoot);
    }


    private static boolean executeShell(String cmd, boolean isRoot) {
        LogUtils.iTag(TAG, "ShellCommand: " + cmd, "isRoot: " + isRoot);
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, isRoot);
        LogUtils.iTag(TAG, "ShellCommand Result: " + result.toString());
        return result.successMsg != null && result.successMsg.toLowerCase(Locale.US).contains("success");
    }


    /**
     * 调用反射方式安装, 通过PackageManager#installPackage方法进行安装, 该方法在7.0已经移除
     */
    private static boolean installByReflect(Context context, File file) throws InterruptedException {
        LogUtils.iTag(TAG, "InstallByReflect", file.getPath());
        Method installer = getInstallPackageMethod();
        if (installer == null)
            return false;

        final AtomicBoolean result = new AtomicBoolean(false);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        IPackageInstallObserver observer = new IPackageInstallObserver.Stub() {
            @Override
            public void packageInstalled(String packageName, int returnCode) {
                try {
                    result.set(returnCode == 1);
                } finally {
                    countDownLatch.countDown();
                }
            }
        };

        try {
            installer.invoke(
                    context.getPackageManager(),
                    UriUtils.file2Uri(file),
                    observer,
                    0x00000002,//flag=2表示如果存在则覆盖升级)
                    context.getPackageName()
            );
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            countDownLatch.countDown();
        }

        countDownLatch.await();
        return result.get();
    }


    /**
     * 调用反射方式卸载, 通过PackageManager#deletePackage, 该方法在7.0已经移除
     */
    private static boolean uninstallByReflect(Context context, String packageName) throws InterruptedException {
        LogUtils.iTag(TAG, "UninstallByReflect", packageName);
        Method deleter = getDeletePackageMethod();
        if (deleter == null)
            return false;

        final AtomicBoolean result = new AtomicBoolean(false);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        IPackageDeleteObserver observer = new IPackageDeleteObserver.Stub() {
            @Override
            public void packageDeleted(String packageName, int returnCode) {
                try {
                    result.set(returnCode == 1);
                } finally {
                    countDownLatch.countDown();
                }
            }
        };

        try {
            deleter.invoke(
                    context.getPackageManager(),
                    packageName, observer, 0x00000002);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            countDownLatch.countDown();
        }

        countDownLatch.await();
        return result.get();
    }

    /**
     * 通过流的形式进行静默安装
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @RequiresPermission(Manifest.permission.INSTALL_PACKAGES)
    private static boolean installByPackageInstaller(Context context, File file, AppUtils.AppInfo apkInfo) throws InterruptedException {
        LogUtils.iTag(TAG, "InstallByPackageInstaller", file.getPath());

        OutputStream out = null;
        InputStream in = null;
        PackageInstaller.Session session = null;
        int sessionId = -1;
        boolean success = false;
        PackageManager pm = context.getPackageManager();
        PackageInstaller installer = pm.getPackageInstaller();
        try {

            //初始化安装参数
            PackageInstaller.SessionParams params =
                    new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setSize(file.length());
            params.setAppIcon(ImageUtils.drawable2Bitmap(apkInfo.getIcon()));
            params.setAppLabel(apkInfo.getName());
            params.setAppPackageName(apkInfo.getPackageName());
            sessionId = installer.createSession(params);
            //sessionId 会返回一个正数非零的值, 如果小于0, 表示会话开启错误
            if (sessionId > 0) {
                InstallReceiver callback = new InstallReceiver(context, true, file.getAbsolutePath());
                session = installer.openSession(sessionId);
                out = session.openWrite(file.getName(), 0, file.length());
                in = new FileInputStream(file);
                int len;
                byte[] buffer = new byte[8192];
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                session.fsync(out);
                in.close();
                out.close();
                session.commit(callback.getIntentSender());
                success = callback.isSuccess();
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtils.wTag(TAG, e);
        } finally {
            //如果会话已经开启, 但是没有成功, 则需要将会话进行销毁
            try {
                if (sessionId > 0 && !success) {
                    if (session != null)
                        session.abandon();
                    installer.abandonSession(sessionId);
                }
            } catch (Throwable ignored) {
            }
            CloseUtils.closeIOQuietly(in, out, session);
        }
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @RequiresPermission(Manifest.permission.DELETE_PACKAGES)
    private static boolean uninstallByPackageInstaller(Context context, String packageName) {
        LogUtils.iTag(TAG, "UninstallByPackageInstaller", packageName);
        try {
            InstallReceiver callback = new InstallReceiver(context, false, packageName);
            PackageInstaller installer = Utils.getApp().getPackageManager().getPackageInstaller();
            installer.uninstall(packageName, callback.getIntentSender());
            return callback.isSuccess();
        } catch (Throwable ignored) {
        }
        return false;
    }


    @Nullable
    private static Method getInstallPackageMethod() {
        if (sInstallPackage != null)
            return sInstallPackage;
        try {
            //noinspection JavaReflectionMemberAccess
            sInstallPackage = PackageManager.class.getMethod("installPackage",
                    Uri.class, IPackageInstallObserver.class, int.class, String.class);
            return sInstallPackage;
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    @Nullable
    private static Method getDeletePackageMethod() {
        if (sDeletePackage != null)
            return sDeletePackage;
        try {
            //noinspection JavaReflectionMemberAccess
            sDeletePackage = PackageManager.class.getMethod("deletePackage",
                    String.class, IPackageDeleteObserver.class, int.class);
            return sDeletePackage;
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }


    /**
     * 安装/卸载回调广播
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static final class InstallReceiver extends BroadcastReceiver {
        private final String ACTION = InstallReceiver.class.getName() + SystemClock.elapsedRealtimeNanos();
        private final Context mContext;
        private final String mOperate;
        private final String mParam;
        /**
         * 用于将异步转同步
         */
        private final CountDownLatch mCountDownLatch = new CountDownLatch(1);
        private boolean mSuccess = false;

        private InstallReceiver(Context context, boolean isInstall, String param) {
            this.mContext = context.getApplicationContext();
            this.mOperate = isInstall ? "Install" : "Uninstall";
            this.mParam = param;
            this.mContext.registerReceiver(this, new IntentFilter(ACTION));
        }


        private boolean isSuccess() throws InterruptedException {
            try {
                //安装最长等待2分钟.
                mCountDownLatch.await(2L, TimeUnit.MINUTES);
                return mSuccess;
            } finally {
                mContext.unregisterReceiver(this);
            }
        }

        private IntentSender getIntentSender() {
            return PendingIntent
                    .getBroadcast(mContext, ACTION.hashCode(), new Intent(ACTION).setPackage(mContext.getPackageName()),
                            PendingIntent.FLAG_UPDATE_CURRENT)
                    .getIntentSender();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int status = -200;
                if (intent == null) {
                    mSuccess = false;
                } else {
                    status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
                    mSuccess = status == PackageInstaller.STATUS_SUCCESS;
                }
                LogUtils.iTag(TAG, mParam, mOperate + " Result: " + mSuccess + "[" + status + "]");
            } finally {
                mCountDownLatch.countDown();
            }
        }
    }


    private static boolean compareSharedUserId(String appUid, String apkUid) {
        return TextUtils.equals(appUid, apkUid) || (appUid != null && appUid.equalsIgnoreCase(apkUid));
    }

}
