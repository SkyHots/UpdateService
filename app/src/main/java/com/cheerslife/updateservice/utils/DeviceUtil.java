package com.cheerslife.updateservice.utils;

/**
 * <pre>
 *     author : fupp-
 *     time   : 2020/12/11
 *     desc   :
 * </pre>
 */
public class DeviceUtil {
    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }
}
