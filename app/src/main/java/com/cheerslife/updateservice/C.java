package com.cheerslife.updateservice;

import com.cheerslife.updateservice.utils.DeviceUtil;

/**
 * <pre>
 *     author : fupp-
 *     time   : 2020/10/16
 *     desc   :
 * </pre>
 */
public class C {
    public static final String BED_DOOR_PACKAGE_NAME = "com.cheerslife.mobhelp";
    public static final String CORRIDOR_PACKAGE_NAME = "com.cheerslife.corridor";

    public static final String DoorScreen = "DoorScreen";
    public static final String BedsideScreen = "BedsideScreen";
    public static final String CorridorScreen = "CorridorScreen";

    public static final String DEVICE_FACTORY = DeviceUtil.getDeviceBrand() + "," + DeviceUtil.getSystemModel();

    public static final String IP = "ip";
    public static final String PORT = "port";
    public static final String DEVICE_TYPE = "device_type";

    public static final String DEFAULT_IP = "192.168.26.201";
    public static final String DEFAULT_PORT = "900";
}
