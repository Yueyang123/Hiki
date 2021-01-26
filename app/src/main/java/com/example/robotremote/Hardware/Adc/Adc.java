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

    public int[][] powMat=
            {{70,0}, {71,5}, {72,10},{73,20},{74,30},{75,40},{76,50},{77,60},{78,70},{79,80},{80,90},{82,100}};
    public int getPower()
    {
        int power=0;
        if(pow<powMat[0][0])power=powMat[0][1];
        else if(pow>powMat[0][0]&&pow<powMat[1][0])power=powMat[0][1];
        else if(pow>powMat[1][0]&&pow<powMat[2][0])power=powMat[1][1];
        else if(pow>powMat[2][0]&&pow<powMat[3][0])power=powMat[2][1];
        else if(pow>powMat[3][0]&&pow<powMat[4][0])power=powMat[3][1];
        else if(pow>powMat[4][0]&&pow<powMat[5][0])power=powMat[4][1];
        else if(pow>powMat[5][0]&&pow<powMat[6][0])power=powMat[5][1];
        else if(pow>powMat[6][0]&&pow<powMat[7][0])power=powMat[6][1];
        else if(pow>powMat[7][0]&&pow<powMat[8][0])power=powMat[7][1];
        else if(pow>powMat[8][0]&&pow<powMat[9][0])power=powMat[8][1];
        else if(pow>powMat[9][0]&&pow<powMat[10][0])power=powMat[9][1];
        else if(pow>powMat[10][0]&&pow<powMat[11][0])power=powMat[10][1];
        else power=powMat[11][1];
        return power;
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
