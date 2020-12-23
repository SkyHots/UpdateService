package com.cheerslife.updateservice.updateservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.cheerslife.updateservice.App;
import com.cheerslife.updateservice.C;
import com.cheerslife.updateservice.bean.PostBean;
import com.cheerslife.updateservice.rabbit.RabbitMQManager;
import com.cheerslife.updateservice.rabbit.SubscriberManager;
import com.cheerslife.updateservice.rabbit.subscriber.CallEventSubscriber;
import com.cheerslife.updateservice.utils.DownloadHelper;
import com.cheerslife.updateservice.utils.HttpUtil;
import com.cheerslife.updateservice.utils.LogFile;
import com.cheerslife.updateservice.utils.Manage;
import com.cheerslife.updateservice.utils.SilentInstallUtils;

import java.io.File;
import java.util.List;

public class UpdateService extends Service {

    public static final String TAG = UpdateService.class.getName();
    private String mDeviceType = "";

    @Override
    public void onCreate() {
        super.onCreate();
        //        WifiHelper.connect("bb", "12345678");
        Log.e(TAG, "onCreate: ");
        App.initRabbit();
        CallEventSubscriber subscriber = new CallEventSubscriber(this);
        SubscriberManager.getInstance().subscribe(subscriber);
    }

    public void installApk(String apkUrl, String deviceType, String newVersionName, String officeID, String id) {
        String apkName = newVersionName + ".apk";
        mDeviceType = deviceType;

        switch (deviceType) {
            case C.BedsideScreen:
                Manage.readConfigXML(Manage.mFileBedDoor);
                if (officeID.equals("全部") || officeID.equals(Manage.OFFICE_ID) || TextUtils.isEmpty(Manage.OFFICE_ID)) {
                    LogFile.saveLog("床头屏开始下载更新APK");
                    String verName = getVerName(C.BED_DOOR_PACKAGE_NAME);
                    DownloadHelper.instance().downloadAPK(apkUrl, apkName, new DownloadHelper.CallBack() {
                        @Override
                        public void downApkSuccess(String path, String apkName) {
                            int verCode = getVerCode(C.BED_DOOR_PACKAGE_NAME);
                            int verCodeNew = getVerCodeNew(path + File.separator + apkName, UpdateService.this);
                            String verNameNew = getVerNameNew(path + File.separator + apkName, UpdateService.this);
                            Log.e(TAG, "verCode: " + verCode);
                            Log.e(TAG, "verCodeNew: " + verCodeNew);
                            if (verCodeNew > verCode) {
                                silenceInstall(path, apkName, C.BED_DOOR_PACKAGE_NAME, verName, verNameNew, id);
                            } else {
                                publishRabbitMQ(verName, newVersionName, "失败",
                                        "当前设备已安装App版本:" + verName + ",推送App版本:" + verNameNew, id);
                                LogFile.saveLog("静默安装失败。 verName: " + verName + "newVersionName: " + newVersionName);
                            }
                        }

                        @Override
                        public void downApkFail() {
                            LogFile.saveLog("下载APK失败");
                            publishRabbitMQ(verName, newVersionName, "失败", "下载apk失败", id);
                        }
                    });
                } else {
                    LogFile.saveLog("当前床头屏设备科室不匹配");
                }
                break;
            case C.DoorScreen:
                Manage.readConfigXML(Manage.mFileBedDoor);
                if (officeID.equals("全部") || officeID.equals(Manage.OFFICE_ID) || TextUtils.isEmpty(Manage.OFFICE_ID)) {
                    LogFile.saveLog("门口屏开始下载更新APK");
                    String verName = getVerName(C.BED_DOOR_PACKAGE_NAME);
                    DownloadHelper.instance().downloadAPK(apkUrl, apkName, new DownloadHelper.CallBack() {
                        @Override
                        public void downApkSuccess(String path, String apkName) {
                            int verCode = getVerCode(C.BED_DOOR_PACKAGE_NAME);
                            int verCodeNew = getVerCodeNew(path + File.separator + apkName, UpdateService.this);
                            String verNameNew = getVerNameNew(path + File.separator + apkName, UpdateService.this);
                            Log.e(TAG, "verCode: " + verCode);
                            Log.e(TAG, "verCodeNew: " + verCodeNew);
                            if (verCodeNew > verCode) {
                                silenceInstall(path, apkName, C.BED_DOOR_PACKAGE_NAME, verName, verNameNew, id);
                            } else {
                                publishRabbitMQ(verName, newVersionName, "失败",
                                        "当前设备已安装App版本:" + verName + ",推送App版本:" + verNameNew, id);
                                LogFile.saveLog("静默安装失败。 verName: " + verName + "newVersionName: " + newVersionName);
                            }
                        }

                        @Override
                        public void downApkFail() {
                            LogFile.saveLog("下载APK失败");
                            publishRabbitMQ(verName, newVersionName, "失败", "下载apk失败", id);
                        }
                    });
                } else {
                    LogFile.saveLog("当前门口屏设备科室不匹配");
                }
                break;
            case C.CorridorScreen:
                Manage.readConfigXML(Manage.mFileCorridor);
                if (officeID.equals("全部") || officeID.equals(Manage.OFFICE_ID) || TextUtils.isEmpty(Manage.OFFICE_ID)) {
                    LogFile.saveLog("走廊屏开始下载更新APK");
                    String verName = getVerName(C.CORRIDOR_PACKAGE_NAME);
                    DownloadHelper.instance().downloadAPK(apkUrl, apkName, new DownloadHelper.CallBack() {
                        @Override
                        public void downApkSuccess(String path, String apkName) {
                            int verCode = getVerCode(C.CORRIDOR_PACKAGE_NAME);
                            int verCodeNew = getVerCodeNew(path + File.separator + apkName, UpdateService.this);
                            String verNameNew = getVerNameNew(path + File.separator + apkName, UpdateService.this);
                            Log.e(TAG, "verCode: " + verCode);
                            Log.e(TAG, "verCodeNew: " + verCodeNew);
                            if (verCodeNew > verCode) {
                                silenceInstall(path, apkName, C.CORRIDOR_PACKAGE_NAME, verName, verNameNew, id);
                            } else {
                                publishRabbitMQ(verName, newVersionName, "失败",
                                        "当前设备已安装App版本:" + verName + ",推送App版本:" + verNameNew, id);
                                LogFile.saveLog("静默安装失败。 verName: " + verName + "newVersionName: " + newVersionName);
                            }
                        }

                        @Override
                        public void downApkFail() {
                            LogFile.saveLog("下载APK失败");
                            publishRabbitMQ(verName, newVersionName, "失败", "下载apk失败", id);
                        }
                    });
                } else {
                    LogFile.saveLog("当前走廊屏设备科室不匹配");
                }
                break;
        }
    }

    private void silenceInstall(String path, String apkName, String bedDoorPackageName, String currentVersion,
                                String newVersion, String id) {
        HttpUtil.getExecutorService().execute(() -> {
            LogFile.saveLog("开始静默安装APK");
            try {
                boolean installSuccess = SilentInstallUtils.install(UpdateService.this,
                        path + File.separator + apkName);
                if (installSuccess) {
                    Intent intent1 =
                            UpdateService.this.getPackageManager().getLaunchIntentForPackage(bedDoorPackageName);
                    if (intent1 != null)
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent1);
                    publishRabbitMQ(currentVersion, newVersion, "成功", "success", id);
                    LogFile.saveLog("静默安装APK成功！");
                } else {
                    publishRabbitMQ(currentVersion, newVersion, "失败", "安装apk失败[May be permission refuse!]", id);
                    LogFile.saveLog("静默安装APK失败: [May be permission refuse!]");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                LogFile.saveLog("静默安装APK失败: " + e.getMessage());
                publishRabbitMQ(currentVersion, newVersion, "失败", "安装apk失败:" + e.getMessage(), id);
            }
        });
    }

    /**
     * 发送更新结果给服务端
     *
     * @param currentVersion 当前版本名
     * @param newVersion     新版本名
     * @param updateStatus   更新状态
     * @param updateLog      更新状态日志
     */
    private void publishRabbitMQ(String currentVersion, String newVersion, String updateStatus, String updateLog, String id) {
        PostBean postBean = new PostBean.Builder().setMacAddress(DeviceUtils.getMacAddress())
                .setId(id)
                .setProductType(mDeviceType.equals(C.BedsideScreen) ? "床头屏" : mDeviceType.equals(C.DoorScreen) ? "门口屏" :
                        mDeviceType.equals(C.CorridorScreen) ? "走廊屏" : "未知设备")
                .setFactoryName(C.DEVICE_FACTORY)
                .setOfficeName(Manage.OFFICE_NAME)
                .setWardName(Manage.WARD_NAME)
                .setBedNo(Manage.BED_NO)
                .setApkType(mDeviceType.equals(C.BedsideScreen) ? "床头屏apk" : mDeviceType.equals(C.DoorScreen) ? "门口屏apk" :
                        mDeviceType.equals(C.CorridorScreen) ?
                                "走廊屏apk" : "未知apk")
                .setCurrentVersion(currentVersion)
                .setNewVersion(newVersion)
                .setUpdateStatus(updateStatus)
                .setUpdateLog(updateLog)
                .build();
        String routingKey = mDeviceType + ".Update.Callback" + ".#";
        RabbitMQManager.getInstance().publish(postBean, routingKey);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 30 * 1000;
        long triggerAtMillis = SystemClock.elapsedRealtime() + interval;
        Intent alarmIntent = new Intent(this, UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 大于等于6.0
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 大于等于4.4
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            } else {//  小于4.4
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
        return START_STICKY;
    }

    public int getVerCodeNew(String absPath, Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            appInfo.sourceDir = absPath;
            appInfo.publicSourceDir = absPath;
            return pkgInfo.versionCode;
        }
        return 0;
    }

    public String getVerNameNew(String absPath, Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            appInfo.sourceDir = absPath;
            appInfo.publicSourceDir = absPath;
            return pkgInfo.versionName;
        }
        return "0";
    }

    private int getVerCode(String appID) {
        List<AppUtils.AppInfo> appsInfo = AppUtils.getAppsInfo();
        for (int i = 0; i < appsInfo.size(); i++) {
            AppUtils.AppInfo appInfo = appsInfo.get(i);
            String packageName = appInfo.getPackageName();
            if (packageName.equals(appID)) {
                return appInfo.getVersionCode();
            }
        }
        return 0;
    }

    private String getVerName(String appID) {
        List<AppUtils.AppInfo> appsInfo = AppUtils.getAppsInfo();
        for (int i = 0; i < appsInfo.size(); i++) {
            AppUtils.AppInfo appInfo = appsInfo.get(i);
            String packageName = appInfo.getPackageName();
            if (packageName.equals(appID)) {
                return appInfo.getVersionName();
            }
        }
        return "0";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}