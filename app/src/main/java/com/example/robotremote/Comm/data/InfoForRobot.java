package com.example.robotremote.Comm.data;
import android.util.Log;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  机器人信息，
 *  包括与F1的20位数据
 *  与F4的64位数据
 * @modificationHistory
 */

public class InfoForRobot
{
    final String TAG="ROBOT DATA";
    public static STATUS status=STATUS.STOP;
    public static InfoForF1 infof1=new InfoForF1();
    public static InfoForF4 infof4=new InfoForF4();

    public enum STATUS
    {
        LEFT,//正转
        RIGHT,//反转
        STOP,//停机
        QUICKSTOP,//刹车
        MODE1,//遥控模式
        MODE2,//跟机模式
        MODE3,//自动巡航
        POWEROFF,// 确认关闭电源
    };
}

