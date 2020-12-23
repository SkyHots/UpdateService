package com.cheerslife.updateservice;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.cheerslife.updateservice.rabbit.RabbitMQManager;
import com.cheerslife.updateservice.rabbit.SubscriberManager;
import com.cheerslife.updateservice.rabbit.config.RabbitConfig;
import com.cheerslife.updateservice.utils.Manage;

import java.util.ArrayList;

public class App extends Application {

    private static Context mApp;
    public static long mTime;

    @Override
    public void onCreate() {
        super.onCreate();
        mTime = System.currentTimeMillis();
        mApp = this;
        Utils.init(this);
    }

    public static Context getApp() {
        return mApp;
    }

    public static void initRabbit() {
        String ipFromSP = SPUtils.getInstance().getString(C.IP);
        String ip = TextUtils.isEmpty(ipFromSP) ? C.DEFAULT_IP : ipFromSP;

        ArrayList<String> routKey = new ArrayList<>();
        int deviceType = Manage.getDeviceType();
        Log.e("DeviceType", "getDeviceType: " + deviceType);
        if (deviceType == 1) {
            routKey.add(C.BedsideScreen + ".#");//床头屏
        } else if (deviceType == 2) {
            routKey.add(C.DoorScreen + ".#");//门口屏
        } else if (deviceType == 3) {
            routKey.add(C.CorridorScreen + ".#");//走廊屏
        }
        RabbitConfig config = new RabbitConfig.Builder()
                .userName("lifescreen")
                .password("lifescreen")
                .host(ip)
                .port(5672)
                .exchange("SmartScreenBus")//交换机
                .type("topic")//交换机类型
                .rotingKeyArray(routKey)
                .subscriber(SubscriberManager.getInstance())
                .build();
        RabbitMQManager.getInstance().init(config);
        RabbitMQManager.getInstance().connectMQ();
    }
}
