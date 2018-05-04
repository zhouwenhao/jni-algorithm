#pragma version(1)
#pragma rs java_package_name(com.timark.jnialgorithm)

rs_allocation output;
int width;
int height;

void __attribute__((kernel)) argb2yuv(int in, uint32_t x)
{
                int len = width*height;
                int i = (x)/width;
                int j = (x)%width;

                //屏蔽ARGB的透明度值
                int rgb = in & 0x00FFFFFF;
                //像素的颜色顺序为bgr，移位运算。
                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;
                //套用公式
                int y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                int u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                int v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                //调整
                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);

                //rsDebug("rstest_x", x);
                //rsDebug("rstest_y", (uchar)y);
                //rsDebug("rstest_u", (uchar)u);
                //rsDebug("rstest_v", (uchar)v);

                //赋值
                rsSetElementAt_uchar(output, ((uchar)y), x);
                rsSetElementAt_uchar(output, ((uchar)u), (len + (i >> 1) * width + (j & ~1) + 0));
                rsSetElementAt_uchar(output, ((uchar)v), (len + +(i >> 1) * width + (j & ~1) + 1));
}