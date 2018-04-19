package com.timark.jnialgorithm;

/**
 * Created by yc on 2018/4/19.
 */

public class NativeImageUtils {
    static {
        System.loadLibrary("native-lib");
    }

    public static native int[] argbToBitmapColors(byte[] srcData);
}
