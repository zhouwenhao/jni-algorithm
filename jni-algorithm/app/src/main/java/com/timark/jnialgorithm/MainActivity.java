package com.timark.jnialgorithm;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

//        byte[] srcArgb = bitmap2RGB(BitmapFactory.decodeResource(getResources(), R.mipmap.test));
//        long curTime = System.currentTimeMillis();
////        int[] colors = tt.argbToBitmapColors(srcArgb);
//        int[] colors = NativeImageUtils.argbToBitmapColors(srcArgb);
//        Log.i("test", "native zhuan " + (System.currentTimeMillis() - curTime) + "ms");
//        curTime = System.currentTimeMillis();
//        Bitmap bitmap = Bitmap.createBitmap(colors, 1920, 1080, Bitmap.Config.ARGB_8888);
//        Log.i("test", "native " + (System.currentTimeMillis() - curTime) + "ms");
//
//        ((ImageView)findViewById(R.id.bottom_img)).setImageBitmap(bitmap);


//        Bitmap srcBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.test);
//        long curtime = System.currentTimeMillis();
//        byte[] nv21 = getNv21(1920, 1080, srcBmp);
//        Log.i("test", "bitmap to nv21 time " + (System.currentTimeMillis() - curtime));
//        curtime = System.currentTimeMillis();
//        Bitmap desBmp = yuv2Bitmap(nv21, 1920, 1080);
//        Log.i("test", "nv21 to bitmap time " + (System.currentTimeMillis() - curtime));
//        ((ImageView) findViewById(R.id.bottom_img)).setImageBitmap(desBmp);


        Bitmap srcBmp = getImageFromAssetsFile("test2.jpg");
        long curtime = System.currentTimeMillis();
        byte[] nv21 = getNv21(srcBmp.getWidth(), srcBmp.getHeight(), srcBmp);
        Log.i("test", "bitmap to nv21 time " + (System.currentTimeMillis() - curtime));
        curtime = System.currentTimeMillis();
        Bitmap desBmp = yuv2Bitmap(nv21, srcBmp.getWidth(), srcBmp.getHeight());
        Log.i("test", "nv21 to bitmap time " + (System.currentTimeMillis() - curtime));
        ((ImageView) findViewById(R.id.bottom_img)).setImageBitmap(desBmp);

    }

    private Bitmap getImageFromAssetsFile(String fileName)
    {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try
        {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;

    }

    /**
     * @方法描述 Bitmap转RGB
     */
    public static byte[] bitmap2RGB(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();  //返回可用于储存此位图像素的最小字节数

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //  使用allocate()静态方法创建字节缓冲区
        bitmap.copyPixelsToBuffer(buffer); // 将位图的像素复制到指定的缓冲区

        byte[] rgba = buffer.array();
        byte[] pixels = new byte[(rgba.length / 4) * 3];

        int count = rgba.length / 4;

        //Bitmap像素点的色彩通道排列顺序是RGBA
        for (int i = 0; i < count; i++) {

            pixels[i * 3] = rgba[i * 4];        //R
            pixels[i * 3 + 1] = rgba[i * 4 + 1];    //G
            pixels[i * 3 + 2] = rgba[i * 4 + 2];       //B

        }

        return pixels;
    }

    private Bitmap yuv2Bitmap(byte[] nv21, int width, int height){
//        if (yuvType == null)
//        {
        yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
//        }

        in.copyFrom(nv21);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        return bmpout;
    }

    private byte[] argb2YuvRs(int[] inputArray, int width, int height){
        long startTime = System.currentTimeMillis();
        RenderScript mRS = RenderScript.create(this);
        Allocation inputAllocation = Allocation.createSized(mRS, Element.I32(mRS), inputArray.length);
        inputAllocation.copyFrom(inputArray);
        Allocation outputAllocation = Allocation.createSized(mRS, Element.U8(mRS), inputArray.length * 3 / 2);
        ScriptC_argbToYuv myScript = new ScriptC_argbToYuv(mRS);
        myScript.set_output(outputAllocation);
        myScript.set_width(width);
        myScript.set_height(height);
        myScript.forEach_argb2yuv(inputAllocation);
        byte outputArray[] = new byte[inputArray.length*3/2];
        outputAllocation.copyTo(outputArray);
        Log.d("tag", "time = "+ (System.currentTimeMillis() - startTime));

        return outputArray;
    }

    private byte[] getNv21(int width, int height, Bitmap bitmap){
        int[] argb = new int[width * height];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);


        long curtime = System.currentTimeMillis();
//        byte[] bb = NativeImageUtils.syncArgbToNv21(width, height, argb);
        byte[] bb = rgb2YCbCr420ByJava(argb, width, height);
        Log.i("test", "rgb2nv21_JAVA time " + (System.currentTimeMillis() - curtime));
        curtime = System.currentTimeMillis();
        bb = NativeImageUtils.argbToNv21(width, height, argb);
        Log.i("test", "rgb2nv21_C time " + (System.currentTimeMillis() - curtime));
        curtime = System.currentTimeMillis();
        bb = argb2YuvRs(argb, width, height);
        Log.i("test", "rgb2nv21_GPU time " + (System.currentTimeMillis() - curtime));
        return bb;
    }

    public byte[] rgb2YCbCr420ByJava(int[] pixels, int width, int height) {

        int len = width * height;

        //yuv格式数组大小，y亮度占len长度，u,v各占len/4长度。

        byte[] yuv = new byte[len * 3 / 2];

        int y, u, v;

        for (int i = 0; i < height; i++) {

            for (int j = 0; j < width; j++) {

//屏蔽ARGB的透明度值
                int rgb = pixels[i * width + j] & 0x00FFFFFF;

                //像素的颜色顺序为bgr，移位运算。

                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;

                int b = (rgb >> 16) & 0xFF;

                //套用公式

                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

                //调整

                y = y < 16 ? 16 : (y > 255 ? 255 : y);

                u = u < 0 ? 0 : (u > 255 ? 255 : u);

                v = v < 0 ? 0 : (v > 255 ? 255 : v);

                //赋值

                yuv[i * width + j] = (byte) y;

                yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;

                yuv[len + +(i >> 1) * width + (j & ~1) + 1] = (byte) v;

            }

        }

        return yuv;

    }
}
