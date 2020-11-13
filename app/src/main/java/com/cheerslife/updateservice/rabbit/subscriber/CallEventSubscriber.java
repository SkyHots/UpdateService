package com.cheerslife.updateservice.rabbit.subscriber;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.cheerslife.updateservice.C;
import com.cheerslife.updateservice.rabbit.IDelegateSubscriber;
import com.cheerslife.updateservice.rabbit.entity.CallEventEntity;
import com.cheerslife.updateservice.updateservice.ApkService;
import com.cheerslife.updateservice.utils.Manage;
import com.google.gson.Gson;

import java.util.List;

public class CallEventSubscriber implements IDelegateSubscriber<CallEventEntity> {

    private static final String TAG = "CallEventSubscriber";

    private ApkService mService;

    public CallEventSubscriber(ApkService service) {
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
            if (mService != null) {
                if (entities == null)
                    return;
                int deviceType = getDeviceType(entities.deviceType);
                String verName = getVerName(deviceType == 1 ? C.BED_DOOR_PACKAGE_NAME : deviceType == 2 ?
                        C.BED_DOOR_PACKAGE_NAME : deviceType == 3 ? C.CORRIDOR_PACKAGE_NAME : "lll");
                String versionNameNew = entities.versionCode;
                if (verName.equals(versionNameNew))
                    return;
                if (deviceType == 1 || deviceType == 2) {
                    Manage.readConfigXML(Manage.mFileBedDoor);
                } else if (deviceType == 3) {
                    Manage.readConfigXML(Manage.mFileCorridor);
                } else {
                    return;
                }

                //                if (!Manage.OFFICE_ID.equals(entities.officeID))
                //                    return;
                String apkUrl = entities.apkUrl;
                if (!TextUtils.isEmpty(apkUrl)) {
                    mService.installApk(apkUrl, deviceType, versionNameNew, entities.officeID,entities.id);
                }
            }
            Log.e(TAG, "dispatch: " + entities);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getDeviceType(String deviceType) {
        if (C.DoorScreen.equals(deviceType)) {
            return 2;
        } else if (C.BedsideScreen.equals(deviceType)) {
            return 1;
        } else if (C.CorridorScreen.equals(deviceType)) {
            return 3;
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
}
