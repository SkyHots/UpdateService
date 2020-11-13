package com.cheerslife.updateservice.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Xml;

import com.blankj.utilcode.util.SPUtils;
import com.cheerslife.updateservice.App;
import com.cheerslife.updateservice.C;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Author:rent
 * date:2018/12/20
 * des:
 */
public class Manage {

    public static final String TAG = "Manage";

    public static String OFFICE_ID;
    public static String OFFICE_NAME;
    public static int DEFAULT_PORT;
    public static String DEFAULT_HOST;

    public static String BED_NO;
    public static String HIS_BED_NO;
    public static String WARD_ID;
    public static String WARD_NAME;

    public static int RES_DEFAULT_PORT;
    public static String RES_DEFAULT_PATH;

    public static File mFileBedDoor = new File(Environment.getExternalStorageDirectory()
            .getPath().concat(File.separator) + "cheers_config.xml");

    public static File mFileCorridor = new File(Environment.getExternalStorageDirectory()
            .getPath().concat(File.separator) + "cheers_corridor_config.xml");


    public static void readConfigXML(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(fis, "UTF-8");
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();
                    if ("default_host".equals(tag)) {
                        DEFAULT_HOST = xpp.nextText();
                    }
                    if ("default_port".equals(tag)) {
                        DEFAULT_PORT = Integer.parseInt(xpp.nextText());
                    }
                    if ("res_path".equals(tag)) {
                        String res_url = xpp.nextText();
                        if (!TextUtils.isEmpty(res_url)) {
                            if (res_url.startsWith("/")) {
                                res_url = res_url.substring(1);
                            }
                            if (res_url.endsWith("/")) {
                                res_url = res_url.substring(0, res_url.length() - 1);
                            }
                        }
                        RES_DEFAULT_PATH = res_url;
                    }
                    if ("res_port".equals(tag)) {
                        RES_DEFAULT_PORT = Integer.parseInt(xpp.nextText());
                    }
                    if ("officeid".equals(tag)) {
                        OFFICE_ID = xpp.nextText();
                    }
                    if ("officename".equals(tag)) {
                        OFFICE_NAME = xpp.nextText();
                    }
                    if ("wardid".equals(tag)) {
                        WARD_ID = xpp.nextText();
                    }
                    if ("wardname".equals(tag)) {
                        WARD_NAME = xpp.nextText();
                    }
                    if ("bedno".equals(tag)) {
                        BED_NO = xpp.nextText();
                    }
                    if ("hisbedno".equals(tag)) {
                        HIS_BED_NO = xpp.nextText();
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    //读取系统所有包名
    private static List<String> allPackage() {
        PackageManager packageManager = App.getApp().getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<>();
        for (int i = 0; i < packageInfos.size(); i++) {
            String packName = packageInfos.get(i).packageName;
            packageNames.add(packName);
        }
        return packageNames;
    }

    /**
     *
     * @return  1.床头屏 2.门口屏 3.走廊屏
     */
    public static int getDeviceType() {
        int type = SPUtils.getInstance().getInt(C.DEVICE_TYPE);
        if (type == 1 || type == 2 || type == 3) {
            return type;
        } else {
            List<String> packages = allPackage();
            if (packages.contains(C.BED_DOOR_PACKAGE_NAME)) {
                readConfigXML(mFileBedDoor);
                if (!TextUtils.isEmpty(BED_NO)) {
                    return 1;
                } else {
                    if (!TextUtils.isEmpty(WARD_ID)) {
                        return 2;
                    }
                }
            } else if (packages.contains(C.CORRIDOR_PACKAGE_NAME)) {
                return 3;
            }
        }
        return 0;
    }

}
