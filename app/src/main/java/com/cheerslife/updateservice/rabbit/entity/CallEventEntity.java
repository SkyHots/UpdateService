package com.cheerslife.updateservice.rabbit.entity;

import java.io.Serializable;


public class CallEventEntity implements Serializable {

    /**
     * id : 6efb9fa1f7ba4ffeb28e4802ef6737a9
     * deviceType : DoorScreen
     * apkUrl : http://127.0.0.1:805/updateProgram/DoorScreen/akp1.1.1.1.apk
     * officeID : O000000004
     * versionCode : 1.1
     * fileName : 门口
     */

    public String id;
    public String deviceType;
    public String apkUrl;
    public String officeID;
    public String versionCode;
    public String fileName;

}
