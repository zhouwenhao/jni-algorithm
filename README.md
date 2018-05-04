# jni-algorithm
持续更新：


jni实现的多个算法

算法1：NativeImageUtils：public static native int[] argbToBitmapColors(byte[] srcData);      
RGB或者ARGB格式转码成像素点   之后由像素点生成bitmap

转码时间：04-19 15:53:36.421 21300-21300/com.timark.jni_algorithm I/test: native zhuan 105ms
像素点生成bitmap时间：04-19 15:53:36.444 21300-21300/com.timark.jni_algorithm I/test: native 23ms


算法2：  greatestCommonDivisor  求最大公约数


算法3：  argbToNv21  bitmap的pixel转成nv21格式
1080P分辨率 耗时 158

算法4：  yuvTonv21   将y u v 三组数据拼接成nv21格式

RenderScrpit: GPU加速

