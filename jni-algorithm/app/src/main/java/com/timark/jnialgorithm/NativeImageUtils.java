package com.timark.jnialgorithm;

/**
 * Created by zhou on 2018/4/19.
 */

public class NativeImageUtils {
    static {
        System.loadLibrary("native-lib");
    }

    public static native int[] argbToBitmapColors(byte[] srcData);
    public static native int greatestCommonDivisor(int a, int b);
    public static native byte[] argbToNv21(int width, int height, int[] argb);  //jint jwidth, jint jheight, jintArray jargbarray
    public static native byte[] yuvTonv21(byte[] yByte, byte[] uByte, byte[] vByte, int yrow, int urow, int vrow, int width, int height); //jbyteArray jyarray, jbyteArray juarray, jbyteArray jvarray, jint jyrow, jint jurow, jint jvrow, jint jwitdh, jint jheight
    public static native INativeSort[] nativeSort(INativeSort[] src, boolean isUp);
}
