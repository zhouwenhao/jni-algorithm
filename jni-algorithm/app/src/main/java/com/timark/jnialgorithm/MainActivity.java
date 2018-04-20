package com.timark.jnialgorithm;

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

import java.nio.ByteBuffer;

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

        Bitmap srcBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.test);
        long curtime = System.currentTimeMillis();
        byte[] nv21 = getNv21(1920, 1080, srcBmp);
        Log.i("test", "bitmap to nv21 time " + (System.currentTimeMillis() - curtime));
        curtime = System.currentTimeMillis();
        Bitmap desBmp = yuv2Bitmap(nv21, 1920, 1080);
        Log.i("test", "nv21 to bitmap time " + (System.currentTimeMillis() - curtime));
        ((ImageView) findViewById(R.id.bottom_img)).setImageBitmap(desBmp);
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

    private byte[] getNv21(int width, int height, Bitmap bitmap){
        long curtime = System.currentTimeMillis();
        int[] argb = new int[width * height];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);
        Log.i("test", "pixel time " + (System.currentTimeMillis() - curtime));
        return NativeImageUtils.argbToNv21(width, height, argb);
    }
}
