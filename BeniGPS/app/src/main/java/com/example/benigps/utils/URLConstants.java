package com.example.benigps.utils;

public class URLConstants {

    public static final String BASE="http://192.168.0.101:3000";
    public static final String Track=BASE+"/api/track";
    public static final String Track_Update=BASE+"/api/track/update";
    public static final String Update_OutTime=BASE+"/api/track/updateOutTime";
    public static final String Track_Time_Basis=BASE+"/api/track/getLatAndLongBetweenTime";

    public static final String Track_Fullpath=BASE+"/api/track/getLatAndLongByDate";
    public static final String Track_Spilt_Time=BASE+"/api/track/getLatAndLongByDateWithShiftTime";

    public static final String Get_Name=BASE+"/api/track/getUsernameByDeviceId";
    public static final String Update_InTime=BASE+"/api/track/updateInTime";

    public static final String Send_Notify=BASE+"/api/notif/sendFcmNotification";
    public static final String Register=BASE+"/api/user/registerUser";

}
