package com.example.robotremote.Hardware.Adc;
import android.util.Log;

import com.example.robotremote.Hardware.Serial.SerialCol;

import java.io.BufferedReader;
import java.io.FileReader;

public class Adc implements Runnable{
    private final String TAG = "ReadAdcResult";
    private SerialCol serialCol;
    public  int pow=0;

    public Adc(SerialCol serialCol){
        this.serialCol  = serialCol;
    }

    public int getPower()
    {
        return pow;
    }

    public void run()
    {
        while(true)
        {
            byte[] temp1=new byte[1];
            byte[] temp2=new byte[1];
            byte[] temp0=new byte[1];
            serialCol.read(temp1);
            if(temp1[0]==0x0b)serialCol.read(temp2);
            if(temp2[0]==0x0b)serialCol.read(temp0);
            pow=(int)temp0[0];
            //Log.d(TAG,Integer.toString(pow));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
