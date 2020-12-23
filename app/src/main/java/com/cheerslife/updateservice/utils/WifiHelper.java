package com.cheerslife.updateservice.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.cheerslife.updateservice.App;

import java.util.Random;

import androidx.annotation.NonNull;

/**
 * <pre>
 *     author : fupp-
 *     time   : 2020/06/01
 *     desc   :
 * </pre>
 */
public class WifiHelper {

    private static final String TAG = "WifiHelper";

    // : 2020/5/29 wifi重连身份验证出现问题
    static void resetWifi() {
        WifiManager wifiManager = (WifiManager) App.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                wifiManager.setWifiEnabled(false);
                new Handler().postDelayed(() -> {
                    wifiManager.setWifiEnabled(true);
                }, 5000 + new Random().nextInt(60) * 1000);
            } else {
                new Handler().postDelayed(() -> {
                    wifiManager.setWifiEnabled(true);
                }, 5000 + new Random().nextInt(60) * 1000);
            }
        }
    }

    /**
     * 尝试连接指定wifi
     *
     * @param ssid     wifi名
     * @param password 密码
     * @return 是否连接成功
     */
    public static void connect(@NonNull String ssid, @NonNull String password) {
        WifiManager wifiManager = (WifiManager) App.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            new Handler().postDelayed(() -> {
                int networkId = wifiManager.addNetwork(newWifiConfig(ssid, password, false));
                wifiManager.saveConfiguration();
                wifiManager.enableNetwork(networkId, true);
            }, 2000);
        } else {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String connectedSSID = wifiInfo.getSSID().replace("\"", "");
            Log.e(TAG, "connectedSSID: " + connectedSSID);
            if (!ssid.equals(connectedSSID)) {
                int networkId = wifiManager.addNetwork(newWifiConfig(ssid, password, false));
                wifiManager.saveConfiguration();
                wifiManager.enableNetwork(networkId, true);
            }
        }
    }

    /**
     * 根据wifi名与密码配置 WiFiConfiguration, 每次尝试都会先断开已有连接
     *
     * @param isClient 当前设备是作为客户端,还是作为服务端, 影响SSID和PWD
     */
    @NonNull
    private static WifiConfiguration newWifiConfig(String ssid, String password, boolean isClient) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        if (isClient) {//作为客户端, 连接服务端wifi热点时要加双引号
            config.SSID = "\"" + ssid + "\"";
            config.preSharedKey = "\"" + password + "\"";
        } else {//作为服务端, 开放wifi热点时不需要加双引号
            config.SSID = ssid;
            config.preSharedKey = password;
        }
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        return config;
    }

}
