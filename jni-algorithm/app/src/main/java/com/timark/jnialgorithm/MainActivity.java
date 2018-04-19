package com.timark.jnialgorithm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        byte[] srcArgb = bitmap2RGB(BitmapFactory.decodeResource(getResources(), R.mipmap.test));
        long curTime = System.currentTimeMillis();
//        int[] colors = tt.argbToBitmapColors(srcArgb);
        int[] colors = NativeImageUtils.argbToBitmapColors(srcArgb);
        Log.i("test", "native zhuan " + (System.currentTimeMillis() - curTime) + "ms");
        curTime = System.currentTimeMillis();
        Bitmap bitmap = Bitmap.createBitmap(colors, 1920, 1080, Bitmap.Config.ARGB_8888);
        Log.i("test", "native " + (System.currentTimeMillis() - curTime) + "ms");

        ((ImageView)findViewById(R.id.bottom_img)).setImageBitmap(bitmap);
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
}
