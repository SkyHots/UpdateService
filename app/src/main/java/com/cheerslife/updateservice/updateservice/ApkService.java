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
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cheerslife.updateservice.App;
import com.cheerslife.updateservice.C;
import com.cheerslife.updateservice.R;
import com.cheerslife.updateservice.bean.PostBean;
import com.cheerslife.updateservice.rabbit.RabbitMQManager;
import com.cheerslife.updateservice.rabbit.SubscriberManager;
import com.cheerslife.updateservice.rabbit.subscriber.CallEventSubscriber;
import com.cheerslife.updateservice.utils.DownloadHelper;
import com.cheerslife.updateservice.utils.HttpUtil;
import com.cheerslife.updateservice.utils.Manage;
import com.cheerslife.updateservice.utils.SilentInstallUtils;

import java.io.File;
import java.util.List;

public class ApkService extends Service {

    public static final String TAG = ApkService.class.getName();
    private int mDeviceType;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
        App.initRabbit();
        CallEventSubscriber subscriber = new CallEventSubscriber(this);
        SubscriberManager.getInstance().subscribe(subscriber);
    }

    public void installApk(String apkUrl, int deviceType, String newVersionName, String officeID, String id) {
        String apkName = newVersionName + ".apk";
        mDeviceType = deviceType;
        if (deviceType == 1) {
            Manage.readConfigXML(Manage.mFileBedDoor);
            if (officeID.equals("全部") || officeID.equals(Manage.OFFICE_ID)) {
                Log.e(TAG, "床头屏下载更新APK ");
                String verName = getVerName(C.BED_DOOR_PACKAGE_NAME);
                DownloadHelper.instance().downloadAPK(apkUrl, apkName, new DownloadHelper.CallBack() {
                    @Override
                    public void downApkSuccess(String path, String apkName) {
                        int verCode = getVerCode(C.BED_DOOR_PACKAGE_NAME);
                        int verCodeNew = getVerCodeNew(path + File.separator + apkName, ApkService.this);
                        String verNameNew = getVerNameNew(path + File.separator + apkName, ApkService.this);
                        Log.e(TAG, "verCode: " + verCode);
                        Log.e(TAG, "verCodeNew: " + verCodeNew);
                        if (verCodeNew > verCode) {
                            silenceInstall(path, apkName, C.BED_DOOR_PACKAGE_NAME, verName, verNameNew, id);
                        } else if (verCodeNew == verCode) {
                            Log.e(TAG, getString(R.string.alreadyIsNews));
                        }
                    }

                    @Override
                    public void downApkFail() {
                        publishRabbitMQ(verName, newVersionName, "失败", "下载apk失败", id);
                    }
                });
            } else {
                Log.e(TAG, "当前床头屏设备科室不匹配");
                ToastUtils.showLong("当前床头屏设备科室不匹配");
            }
        } else if (deviceType == 2) {
            Manage.readConfigXML(Manage.mFileBedDoor);
            if (officeID.equals("全部") || officeID.equals(Manage.OFFICE_ID)) {
                Log.e(TAG, "门口屏下载更新APK ");
                String verName = getVerName(C.BED_DOOR_PACKAGE_NAME);
                DownloadHelper.instance().downloadAPK(apkUrl, apkName, new DownloadHelper.CallBack() {
                    @Override
                    public void downApkSuccess(String path, String apkName) {
                        int verCode = getVerCode(C.BED_DOOR_PACKAGE_NAME);
                        int verCodeNew = getVerCodeNew(path + File.separator + apkName, ApkService.this);
                        String verNameNew = getVerNameNew(path + File.separator + apkName, ApkService.this);
                        Log.e(TAG, "verCode: " + verCode);
                        Log.e(TAG, "verCodeNew: " + verCodeNew);
                        if (verCodeNew > verCode) {
                            silenceInstall(path, apkName, C.BED_DOOR_PACKAGE_NAME, verName, verNameNew, id);
                        } else if (verCodeNew == verCode) {
                            Log.e(TAG, getString(R.string.alreadyIsNews));
                        }
                    }

                    @Override
                    public void downApkFail() {
                        publishRabbitMQ(verName, newVersionName, "失败", "下载apk失败", id);
                    }
                });
            } else {
                Log.e(TAG, "当前门口屏设备科室不匹配");
                ToastUtils.showLong("当前门口屏设备科室不匹配");
            }
        } else if (deviceType == 3) {
            Manage.readConfigXML(Manage.mFileCorridor);
            if (officeID.equals("全部") || officeID.equals(Manage.OFFICE_ID)) {
                Log.e(TAG, "走廊屏下载更新APK ");
                String verName = getVerName(C.CORRIDOR_PACKAGE_NAME);
                DownloadHelper.instance().downloadAPK(apkUrl, apkName, new DownloadHelper.CallBack() {
                    @Override
                    public void downApkSuccess(String path, String apkName) {
                        int verCode = getVerCode(C.CORRIDOR_PACKAGE_NAME);
                        int verCodeNew = getVerCodeNew(path + File.separator + apkName, ApkService.this);
                        String verNameNew = getVerNameNew(path + File.separator + apkName, ApkService.this);
                        Log.e(TAG, "verCode: " + verCode);
                        Log.e(TAG, "verCodeNew: " + verCodeNew);
                        if (verCodeNew > verCode) {
                            silenceInstall(path, apkName, C.CORRIDOR_PACKAGE_NAME, verName, verNameNew, id);
                        } else if (verCodeNew == verCode) {
                            Log.e(TAG, getString(R.string.alreadyIsNews));
                        }
                    }

                    @Override
                    public void downApkFail() {
                        publishRabbitMQ(verName, newVersionName, "失败", "下载apk失败", id);
                    }
                });
            } else {
                Log.e(TAG, "当前走廊屏设备科室不匹配");
                ToastUtils.showLong("当前走廊屏设备科室不匹配");
            }
        }
    }

    private void silenceInstall(String path, String apkName, String bedDoorPackageName, String currentVersion,
                                String newVersion, String id) {
        HttpUtil.getExecutorService().execute(() -> {
            try {
                boolean installSuccess = SilentInstallUtils.install(ApkService.this,
                        path + File.separator + apkName);
                if (installSuccess) {
                    Log.e(TAG, "静默安装成功！！！！！！！！！！");
                    Intent intent1 =
                            ApkService.this.getPackageManager().getLaunchIntentForPackage(bedDoorPackageName);
                    if (intent1 != null)
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent1);
                    publishRabbitMQ(currentVersion, newVersion, "成功", "success", id);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                .setProductType(mDeviceType == 1 ? "床头屏" : mDeviceType == 2 ? "门口屏" : mDeviceType == 3 ? "走廊屏" :
                        "未知设备")
                .setFactoryName("狄耐克")
                .setOfficeName(Manage.OFFICE_NAME)
                .setWardName(Manage.WARD_NAME)
                .setBedNo(Manage.BED_NO)
                .setApkType(mDeviceType == 1 ? "床头屏apk" : mDeviceType == 2 ? "门口屏apk" : mDeviceType == 3 ?
                        "走廊屏apk" : "未知apk")
                .setCurrentVersion(currentVersion)
                .setNewVersion(newVersion)
                .setUpdateStatus(updateStatus)
                .setUpdateLog(updateLog)
                .build();
        String routingKey = mDeviceType == 1 ? C.BedsideScreen + ".Update.Callback" + ".#" : mDeviceType == 2 ?
                C.DoorScreen + ".Update.Callback" + ".#" : mDeviceType == 3 ?
                C.CorridorScreen + ".Update.Callback" + ".#" : "";
        RabbitMQManager.getInstance().publish(postBean, routingKey);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 30 * 1000;
        long triggerAtMillis = SystemClock.elapsedRealtime() + interval;
        Intent alarmIntent = new Intent(this, ApkService.class);
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