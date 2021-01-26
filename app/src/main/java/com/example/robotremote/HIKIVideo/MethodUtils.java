package com.example.robotremote.HIKIVideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 * 海康摄像头通用方法
 * @modificationHistory
 */
public class MethodUtils {
    private Context context = null;
    private static MethodUtils mInstance = null;
    public MethodUtils(){
    }

    public static synchronized MethodUtils getInstance(){
        if (mInstance == null){
            mInstance = new MethodUtils();
        }
        return mInstance;
    }

    /**
     * 初始化海康视频SDK
     * @return
     */
    public static boolean initHCNetSDK() {
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            return false;
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/", true);
        return true;
    }

    /**
     * 退出Activity，设置返回值
     * @param activity  待退出activity
     * @param resultCode    返回码
     * @param msg   返回信息
     */
    public void quitActivity(Activity activity,int resultCode, String msg){
        Intent intent = new Intent();
        intent.putExtra("result",msg);
        activity.setResult(resultCode,intent);
        activity.finish();
    }

    /**
     * 获取异常回调
     *
     * @return  异常回调
     */
    public ExceptionCallBack getExceptiongCbf() {
        return new ExceptionCallBack() {
            public void fExceptionCallBack(int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {
                Log.e("ExceptionCallBack", "recv exception, type:" + paramAnonymousInt1);
            }
        };
    }

    /**
     * 获取连接错误信息
     * 当前只例举了常见错误码对应错误信息，更多错误码信息详见《设备网络编程指南（Android）》第4章
     *
     * @param errorCode 错误码
     * @return
     */
    public String getNETDVRErrorMsg(int errorCode){
        String errorMsg = "";
        switch (errorCode){
            case 1:
                errorMsg = "用户名或密码错误";
                break;
            case 2:
                errorMsg = "无当前设备操作权限";
                break;
            case 3:
                errorMsg = "SDK未初始化";
                break;
            case 4:
                errorMsg = "通道号错误";
                break;
            case 5:
                errorMsg = "连接到设备的用户数超过最大";
                break;
            case 7:
                errorMsg = "连接设备失败";
                break;
            case 11:
                errorMsg = "传送的数据有误";
                break;
            case 13:
                errorMsg = "无此权限";
                break;
            default:
                errorMsg = "错误码" + errorCode;
                break;
        }
        return errorMsg;
    }

}
