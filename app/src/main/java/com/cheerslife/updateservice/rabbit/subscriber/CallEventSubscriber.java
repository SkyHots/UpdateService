package com.cheerslife.updateservice.rabbit.subscriber;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.cheerslife.updateservice.C;
import com.cheerslife.updateservice.rabbit.IDelegateSubscriber;
import com.cheerslife.updateservice.rabbit.RabbitMQManager;
import com.cheerslife.updateservice.rabbit.config.RabbitConfig;
import com.cheerslife.updateservice.rabbit.entity.CallEventEntity;
import com.cheerslife.updateservice.updateservice.UpdateService;
import com.cheerslife.updateservice.utils.Manage;
import com.google.gson.Gson;

import java.util.List;

public class CallEventSubscriber implements IDelegateSubscriber<CallEventEntity> {

    private static final String TAG = "CallEventSubscriber";

    private UpdateService mService;

    public CallEventSubscriber(UpdateService service) {
        mService = service;
    }

    @Override
    public boolean isAvailable(String message) {
        try {
            new Gson().fromJson(message, CallEventEntity.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public CallEventEntity format(String message) {
        try {
            return new Gson().fromJson(message, CallEventEntity.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void dispatch(CallEventEntity entities) {
        try {
            RabbitConfig config = RabbitMQManager.getInstance().getConfig();
            String routingKey = "";
            if (config != null) {
                routingKey = config.getRotingKeyArray().get(0);
                Log.e(TAG, "routingKey: " + routingKey);
            }

            if (mService != null) {
                if (entities == null)
                    return;

                String deviceType = entities.deviceType;

                String verName = getInstalledAppVerName(deviceType.equals(C.BedsideScreen) ? C.BED_DOOR_PACKAGE_NAME :
                        deviceType.equals(C.DoorScreen) ?
                                C.BED_DOOR_PACKAGE_NAME : deviceType.equals(C.CorridorScreen) ? C.CORRIDOR_PACKAGE_NAME : "lll");
                String versionNameNew = entities.versionCode;
                if (verName.equals(versionNameNew)) {
                    Log.e(TAG, "版本号相同，不下载！");
                    return;
                }

                switch (deviceType) {
                    case C.BedsideScreen:
                        if (!routingKey.contains(C.BedsideScreen)) {//key不匹配
                            Log.e(TAG, "当前设备不是床头屏，不下载！");
                            return;
                        }
                        Manage.readConfigXML(Manage.mFileBedDoor);
                        break;
                    case C.DoorScreen:
                        if (!routingKey.contains(C.DoorScreen)) {//key不匹配
                            Log.e(TAG, "当前设备不是门口屏，不下载！");
                            return;
                        }
                        Manage.readConfigXML(Manage.mFileBedDoor);
                        break;
                    case C.CorridorScreen:
                        if (!routingKey.contains(C.CorridorScreen)) {//key不匹配
                            Log.e(TAG, "当前设备不是走廊屏，不下载！");
                            return;
                        }
                        Manage.readConfigXML(Manage.mFileCorridor);
                        break;
                    default:
                        return;
                }

                String apkUrl = entities.apkUrl;
                if (!TextUtils.isEmpty(apkUrl)) {
                    mService.installApk(apkUrl, deviceType, versionNameNew, entities.officeID, entities.id);
                }
            }
            Log.e(TAG, "dispatch: " + entities);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getInstalledAppVerName(String pName) {
        List<AppUtils.AppInfo> appsInfo = AppUtils.getAppsInfo();
        for (int i = 0; i < appsInfo.size(); i++) {
            AppUtils.AppInfo appInfo = appsInfo.get(i);
            String packageName = appInfo.getPackageName();
            if (packageName.equals(pName)) {
                return appInfo.getVersionName();
            }
        }
        return "0";
    }
}
