package com.example.robotremote;
import java.io.FileDescriptor;
/**
 * @author yueyang
 * @version V1.0
 * @describation
 *  基于6818的硬件控制器
 * @modificationHistory
 */
public class HardCol {
    public native static boolean GpioInit(int portIndex,int port);
    public native static int GpioSetDirection(int portIndex,boolean gpioDirection);
    public native static void GpioClose(int portIndex);
    public native static int GpioGetValue(int portIndex);
    public native static void GpioSetValue(int portIndex,int value);
    public native static void GpioMalloc(int max);
    public native static FileDescriptor SerialOpen(String path,int baudrate,int flags);
    public native static void SerialClose();
    static {
        System.loadLibrary("led-lib");
        System.loadLibrary("gpio-lib");
        System.loadLibrary("serial-lib");
    }
}
