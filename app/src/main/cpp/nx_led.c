//
// Created by root on 9/8/20.
//

#include "nx_led.h"
#include <jni.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <fcntl.h>		// open
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <error.h>
#include <stdbool.h>

#define ON 1
#define OFF 0
#define DEVICE_NAME		"/dev/real_led"

JNIEXPORT jboolean JNICALL
Java_com_example_robotremote_HardCol_ledOn(JNIEnv *env, jclass clazz, jint fd) {
    // TODO: implement ledOn()
    ioctl(fd, ON, 1);
    return true;
}

JNIEXPORT jboolean JNICALL
Java_com_example_robotremote_HardCol_ledOff(JNIEnv *env, jclass thiz, jint fd) {
    // TODO: implement ledOff()

    ioctl(fd, OFF, 1);

    return true;
}

JNIEXPORT jint JNICALL
Java_com_example_robotremote_HardCol_ledInit(JNIEnv *env, jclass thiz) {
    // TODO: implement ledInit()

    int fd = open(DEVICE_NAME, 0);
    if(fd==-1) return -1;
    else return fd;

}

JNIEXPORT jint JNICALL
Java_com_example_robotremote_HardCol_ledchange(JNIEnv *env, jclass thiz, jint fd) {
    // TODO: implement ledchange()
    return 0;
}
