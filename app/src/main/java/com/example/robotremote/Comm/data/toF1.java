package com.example.robotremote.Comm.data;
import android.util.Log;
import com.example.robotremote.Comm.Warn.RobotWarn;
import com.example.robotremote.Hardware.Serial.*;
import com.example.robotremote.Comm.CRC.Crc;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

import static com.example.robotremote.Comm.data.toF4.RobotStatus;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  与F1通讯源代码
 *   左行
 *   右行
 *   停车
 *   刹车
 *   调速0-100%
 *   关闭电源
 *   WIFI故障后查询现场状况（兼作心跳包）
 *   查询F1姿态和测距值
 *   每秒查询F1电机状态（兼作心跳包）
 * @modificationHistory
 */
public class toF1 implements Runnable {
    final String TAG = "TOF1";
    public SerialCol serialCol;
    public dir Dir = dir.F12REMOTE;
    InfoForRobot RobotStatus;
    public boolean havedata = false;
    public static int[] sendflag = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};//发送位，如果要发将那一位置一

    public enum CMD {
        LEFT,//正转
        RIGHT,//反转
        STOP,//停机
        QUICKSTOP,//刹车
        SPEED,//调速0-100
        POWEROFF,// 关闭电源
        ASKHERT,//每秒查询现场状况（71）
        ASK,//查询F1姿态和超声测距值
        HEART,
        REMOVEWARN,
    }

    ;
    private byte[] head = {};
    public static int tof1Timeout = 10000;
    final byte[][] cmd = {{(byte) 0xA1, (byte) 0xC1},
            {(byte) 0xA1, (byte) 0xC2},
            {(byte) 0xA1, (byte) 0xC3},
            {(byte) 0xA1, (byte) 0xC4},
            {(byte) 0xA1, (byte) 0xC5},
            {(byte) 0xA1, (byte) 0xD4},
            {(byte) 0xA1, (byte) 0x71},
            {(byte) 0xA1, (byte) 0xE2},
            {(byte) 0xA1, (byte) 0xE3},
            {(byte) 0xA1, (byte) 0xB1},};

    public enum dir {
        F12REMOTE,
        REMOTE2F1,
    }

    public toF1(SerialCol serialCol, dir Dir) {
        this.serialCol = serialCol;
        this.RobotStatus = toF4.RobotStatus;
        this.Dir = Dir;
    }

    static byte[] concat(byte[] a, byte[] b) {//字符串链接函数

        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public void sendCMD(byte[] head, CMD Cmdtof1) {
        Calendar calendar = Calendar.getInstance();
        byte[] tcmd = new byte[8];
        tcmd[0] = (cmd[Cmdtof1.ordinal()][0]);
        tcmd[1] = (cmd[Cmdtof1.ordinal()][1]);
        byte pwmspeed=0;
        if (sendflag[CMD.SPEED.ordinal()] ==0||RobotStatus.status==InfoForRobot.STATUS.QUICKSTOP||RobotStatus.status==InfoForRobot.STATUS.STOP)pwmspeed=0;
        else if (sendflag[CMD.SPEED.ordinal()] == 1)pwmspeed=10;
        else if (sendflag[CMD.SPEED.ordinal()] == 2)pwmspeed=50;
        else if (sendflag[CMD.SPEED.ordinal()] == 3)pwmspeed=100;
        tcmd[2] = pwmspeed;
        tcmd[3] = (byte) calendar.getTime().getHours();
        tcmd[4] = (byte) calendar.getTime().getMinutes();
        tcmd[5] = (byte) calendar.getTime().getSeconds();
        int crc = Crc.crc16(tcmd, 6);
        tcmd[6] = (byte) (crc >> 0 & 0xFF);
        tcmd[7] = (byte) (crc >> 8 & 0xFF);
        //for(int i=0;i<tcmd.length;i++)Log.d(TAG,Integer.toHexString(tcmd[i]));
        byte[] tof1 = concat(head, tcmd);
        serialCol.send(tof1);
    }

    @Override
    public void run() {
        int[] warntimes = {0, 0, 0, 0, 0, 0, 0, 0, 0};//记录没有链接到的次数,超过十次没有收到相应警告
        while (true) {
            if (Dir == dir.F12REMOTE) {//接受F1的数据
                byte[] temp1=new byte[1];
                byte[] temp0=new byte[9];
                byte[] temp;
                while (temp1[0]!=0x1A)serialCol.read(temp1);
                    serialCol.read(temp0);
                    temp=concat(temp1,temp0);
                    Log.d(TAG+"RECIVE DATA FROM F1",Integer.toHexString((int) temp[0])+" "+Integer.toHexString((int) temp[1]));
                    RobotWarn.WarnFlag[RobotWarn.Warn.LORAWarn.ordinal()]=false;
                    tof1Timeout=10000;
                    for(int i=0;i<temp.length;i++) RobotStatus.data.daTa[i]=temp[i];
                    RobotStatus.data.length=10;
                    RobotStatus.infof1.infomotor.set(temp[2]);
                    /**
                     * 更新相应警告
                     * */
                    if((temp[3]&1)==1){
                        warntimes[4] = 0;
                        RobotWarn.WarnFlag[RobotWarn.Warn.UDPWarn.ordinal()] = true;//警告
                    }
                    if(((temp[3]>>1)&1)==1){
                        warntimes[2] = 0;
                        RobotWarn.WarnFlag[RobotWarn.Warn.POSTUREWarn.ordinal()] = true;//警告
                        Log.d(TAG,"姿态告警");
                    }
                    if(((temp[3]>>2)&1)==1){
                          warntimes[3] = 0;
                          RobotWarn.WarnFlag[RobotWarn.Warn.DISTANCEWarn.ordinal()] = true;//警告
                    }
                    if(((temp[3]>>3)&1)==1){
                        warntimes[8] = 0;
                        RobotWarn.WarnFlag[RobotWarn.Warn.MCUWarn.ordinal()] = true;//警告
                    }
                    if(((temp[3]>>4)&1)==1){
                        warntimes[5] = 0;
                        RobotWarn.WarnFlag[RobotWarn.Warn.TCPWarn.ordinal()] = true;//警告
                    }
                    if(((temp[3]>>5)&1)==1){
                        warntimes[6] = 0;
                        RobotWarn.WarnFlag[RobotWarn.Warn.MOTORWarn.ordinal()] = true;//警告
                    }
                    if(((temp[3]>>6)&1)==1){
                        warntimes[0] = 0;
                        RobotWarn.WarnFlag[RobotWarn.Warn.SMOKEWarn.ordinal()] = true;//警告
                    }
                    if(((temp[3]>>7)&1)==1){
                        warntimes[1] = 0;
                        RobotWarn.WarnFlag[RobotWarn.Warn.GASWarn.ordinal()] = true;//警告
                    }
                    if((temp[3]&1)==0){
                        warntimes[4]++;
                        if (warntimes[4] % 10 == 0) RobotWarn.WarnFlag[RobotWarn.Warn.UDPWarn.ordinal()] = false;//警告
                    }
                    if(((temp[3]>>1)&1)==0){
                       warntimes[2]++;
                        if (warntimes[2] % 10 == 0) RobotWarn.WarnFlag[RobotWarn.Warn.POSTUREWarn.ordinal()] = false;//警告
                    }
                    if(((temp[3]>>2)&1)==0){
                        warntimes[3]++;
                        if (warntimes[3] % 10 == 0) RobotWarn.WarnFlag[RobotWarn.Warn.DISTANCEWarn.ordinal()] = false;//警告
                    }
                    if(((temp[3]>>3)&1)==0){
                        warntimes[8]++;
                        if (warntimes[8] % 10 == 0) RobotWarn.WarnFlag[RobotWarn.Warn.MCUWarn.ordinal()] = false;//警告
                    }
                    if(((temp[3]>>4)&1)==0){
                        warntimes[5]++;
                        if (warntimes[5] % 10 == 0) RobotWarn.WarnFlag[RobotWarn.Warn.TCPWarn.ordinal()] = false;//警告
                    }
                    if(((temp[3]>>5)&1)==0){
                        warntimes[6]++;
                        if (warntimes[6] % 10 == 0) RobotWarn.WarnFlag[RobotWarn.Warn.MOTORWarn.ordinal()] = false;//警告
                    }
                    if(((temp[3]>>6)&1)==0){
                        warntimes[0]++;
                        if (warntimes[0] % 10 == 0) RobotWarn.WarnFlag[RobotWarn.Warn.SMOKEWarn.ordinal()] = false;//警告
                    }
                    if(((temp[3]>>7)&1)==0){
                        warntimes[1]++;
                        if (warntimes[1] % 10 == 0) RobotWarn.WarnFlag[RobotWarn.Warn.GASWarn.ordinal()] = false;//警告
                    }
                    switch (temp[1]&0xFF)
                    {
                        case 0xC1:
                            sendflag[CMD.LEFT.ordinal()]=0;
                            if(RobotStatus.status!= InfoForRobot.STATUS.QUICKSTOP)
                               RobotStatus.status= InfoForRobot.STATUS.LEFT;
                            break;
                        case 0xC2:
                            sendflag[CMD.RIGHT.ordinal()]=0;
                            if(RobotStatus.status!= InfoForRobot.STATUS.QUICKSTOP)
                                RobotStatus.status= InfoForRobot.STATUS.RIGHT;
                            break;
                        case 0xC3:
                            sendflag[CMD.STOP.ordinal()]=0;
                            if(RobotStatus.status!= InfoForRobot.STATUS.QUICKSTOP)
                                RobotStatus.status= InfoForRobot.STATUS.STOP;
                            break;
                        case 0xC4:
                            sendflag[CMD.QUICKSTOP.ordinal()]=0;
                            if(RobotStatus.status!= InfoForRobot.STATUS.QUICKSTOP)
                                RobotStatus.status= InfoForRobot.STATUS.QUICKSTOP;
                            break;
                        case 0xD4: RobotStatus.status= InfoForRobot.STATUS.POWEROFF;break;
                    }
                havedata=true;
            } else//发送数据
            {
                boolean NoHeart=false;
                if (sendflag[CMD.QUICKSTOP.ordinal()] != 0&&NoHeart==false) {//一种发送命令C4
                    NoHeart=true;
                    sendCMD(head, CMD.QUICKSTOP);
                    sendflag[CMD.QUICKSTOP.ordinal()]++;
                    if (sendflag[CMD.QUICKSTOP.ordinal()] > 3)
                        sendflag[CMD.QUICKSTOP.ordinal()] = 0;//超过三次不再发送
                }
                if (sendflag[CMD.REMOVEWARN.ordinal()] != 0&&NoHeart==false) {//一种发送命令B1
                    NoHeart=true;
                    sendCMD(head, CMD.REMOVEWARN);
                    sendflag[CMD.REMOVEWARN.ordinal()]++;
                    if (sendflag[CMD.REMOVEWARN.ordinal()] > 3)
                        sendflag[CMD.REMOVEWARN.ordinal()] = 0;//超过三次不再发送
                }
                if (sendflag[CMD.LEFT.ordinal()] != 0&&NoHeart==false) {//第一种发送命令C1
                    NoHeart=true;
                    sendCMD(head, CMD.LEFT);
                    sendflag[CMD.LEFT.ordinal()]++;
                    if (sendflag[CMD.LEFT.ordinal()] > 3)
                        sendflag[CMD.LEFT.ordinal()] = 0;//超过三次不再发送
                }
                if (sendflag[CMD.RIGHT.ordinal()] != 0&&NoHeart==false) {//一种发送命令C2
                    NoHeart=true;
                    sendCMD(head, CMD.RIGHT);
                    sendflag[CMD.RIGHT.ordinal()]++;
                    if (sendflag[CMD.RIGHT.ordinal()] > 3)
                        sendflag[CMD.RIGHT.ordinal()] = 0;//超过三次不再发送
                }
                if (sendflag[CMD.STOP.ordinal()] != 0&&NoHeart==false) {//一种发送命令C3
                    NoHeart=true;
                    sendCMD(head, CMD.STOP);
                    RobotStatus.infof1.pwmspeed = 0;
                    sendflag[CMD.STOP.ordinal()]++;
                    if (sendflag[CMD.STOP.ordinal()] > 5)
                        sendflag[CMD.STOP.ordinal()] = 0;//超过五次不再发送
                }
                if (sendflag[CMD.POWEROFF.ordinal()] != 0&&NoHeart==false) {//一种发送命令D4
                    NoHeart=true;
                    sendCMD(head, CMD.POWEROFF);
                    sendflag[CMD.POWEROFF.ordinal()]++;
                    if (sendflag[CMD.POWEROFF.ordinal()] > 3)
                        sendflag[CMD.POWEROFF.ordinal()] = 0;//超过三次不再发送
                }
                if(NoHeart==false)
                {
                    NoHeart=true;
                    sendCMD(head, CMD.HEART);
                    sendflag[CMD.HEART.ordinal()]++;
                    if (sendflag[CMD.HEART.ordinal()] > 3)
                        sendflag[CMD.HEART.ordinal()] = 0;//超过三次不再发送
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}