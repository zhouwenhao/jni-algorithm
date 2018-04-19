#include <jni.h>
#include <pthread.h>
#include <unistd.h>

int convertByteToInt(jbyte data)
{
    int heightBit = (int) ((data >> 4) & 0x0F);
    int lowBit = (int) (0x0F & data);
    return heightBit * 16 + lowBit;
}

JNIEXPORT jintArray
JNICALL
Java_com_timark_jnialgorithm_NativeImageUtils_argbToBitmapColors(JNIEnv *env, jobject obj, jbyteArray srcArray)
{
    if (srcArray == NULL)
    {
        return NULL;
    }

    jsize srcLen = (*env)->GetArrayLength(env, srcArray);
    int size = srcLen;

    if (size == 0)
    {
        return NULL;
    }

    jbyte aar[srcLen];
    (*env)->GetByteArrayRegion(env, srcArray, 0, srcLen, aar);
    int arg = 0;
    if (size %3 != 0)
    {
        arg = 1;
    }

    const int desLen = size / 3 + arg;
    jintArray  color = (*env)->NewIntArray(env, desLen);
    jint *desAar = (*env)->GetIntArrayElements(env, color, 0);
    int red;
    int green;
    int blue;
    int colorLen = desLen;
    if (arg == 0)
    {
        for (int i = 0; i < colorLen; ++i)
        {
            red = convertByteToInt(*(aar + i * 3));
            green = convertByteToInt(*(aar + i * 3 + 1));
            blue = convertByteToInt(*(aar + i * 3 + 2));
            *(desAar + i) = (red << 16) | (green << 8) | blue | 0xFF000000;
        }
    } else
    {
        for (int i = 0; i < desLen - 1; ++i) {
            red = convertByteToInt(*(aar + i * 3));
            green = convertByteToInt(*(aar + i * 3 + 1));
            blue = convertByteToInt(*(aar + i * 3 + 2));
            *(desAar + i) = (red << 16) | (green << 8) | blue | 0xFF000000;
        }

        *(desAar + desLen - 1) = 0xFF000000;
    }
    (*env)->ReleaseIntArrayElements(env, color, desAar, 0);
    return color;
}
