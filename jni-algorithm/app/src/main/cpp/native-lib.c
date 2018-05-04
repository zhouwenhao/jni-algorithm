#include <jni.h>
#include <pthread.h>
#include <unistd.h>

int convertByteToInt(jbyte data)
{
    int heightBit = (int) ((data >> 4) & 0x0F);
    int lowBit = (int) (0x0F & data);
    return heightBit * 16 + lowBit;
}

/**
 * argb转bitmap的colors
 * @param env
 * @param obj
 * @param srcArray
 * @return
 */
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

int
gcd(int a, int b)
{
    return b==0 ? a : gcd(b,a%b);
}

/**
 * 求最大公约数
 * @return
 */
JNIEXPORT jint
JNICALL
Java_com_timark_jnialgorithm_NativeImageUtils_greatestCommonDivisor(JNIEnv *env, jobject obj, jint ja, jint jb)
{
    return gcd(ja, jb);
}

/**
 * y u v 三个通道拼接NV21
 * @param env
 * @param obj
 * @param jyarray
 * @param juarray
 * @param jvarray
 * @param jyrow
 * @param jurow
 * @param jvrow
 * @param jheight
 * @param jwitdh
 * @return
 */
JNIEXPORT jbyteArray
JNICALL
Java_com_timark_jnialgorithm_NativeImageUtils_yuvTonv21(JNIEnv *env, jobject obj, jbyteArray jyarray, jbyteArray juarray, jbyteArray jvarray, jint jyrow, jint jurow, jint jvrow, jint jwitdh, jint jheight)
{
    int y_len = jyrow * jheight;
//    int u_len = jurow * ((jheight + 1) / 2);
//    int v_len = jvrow * ((jheight + 1) / 2);
    int data_len = y_len + (jyrow * ((jheight + 1) / 2));
    jbyteArray nv21_data = (*env)->NewByteArray(env, data_len);
//    jbyteArray n_data = (*env)->NewByteArray(env, u_len);
//    jbyteArray v_data = (*env)->NewByteArray(env, v_len);
    jbyte *nvaar = (*env)->GetByteArrayElements(env, nv21_data, 0);
    jbyte *yaar = (*env)->GetByteArrayElements(env, jyarray, 0);
    jbyte *uaar = (*env)->GetByteArrayElements(env, juarray, 0);
    jbyte *vaar = (*env)->GetByteArrayElements(env, jvarray, 0);

    int loop_row_c = ((jheight+1)/2);
    int loop_c = ((jwitdh+1)/2);

    int dst_row = y_len;
    int src_v_row = 0;
    int src_u_row = 0;

    int srcLen = (*env)->GetArrayLength(env, jyarray);
    for (int i = 0; i < srcLen; ++i) {
        *(nvaar + i) = *(yaar + i);
    }

    for ( int i = 0; i < loop_row_c; ++i)
    {
        int dst_pos = dst_row;

        for ( int j = 0; j <loop_c; ++j )
        {
            *(nvaar + dst_pos) = *(vaar + src_v_row + j);
            ++dst_pos;
            *(nvaar + dst_pos) = *(uaar + src_u_row + j);
            ++dst_pos;
        }

        dst_row   += jyrow;
        src_v_row += jvrow;
        src_u_row += jurow;
    }

    (*env)->ReleaseByteArrayElements(env, nv21_data, nvaar, 0);
    (*env)->ReleaseByteArrayElements(env, jyarray, yaar, 0);
    (*env)->ReleaseByteArrayElements(env, juarray, uaar, 0);
    (*env)->ReleaseByteArrayElements(env, jvarray, vaar, 0);

    return nv21_data;
}


/**
 * argb转nv21
 * @param env
 * @param obj
 * @param jwidth
 * @param jheight
 * @param jargbarray
 * @return
 */
JNIEXPORT jbyteArray
JNICALL
Java_com_timark_jnialgorithm_NativeImageUtils_argbToNv21(JNIEnv *env, jobject obj, jint jwidth, jint jheight, jintArray jargbarray)
{
    int len = jwidth * jheight;
    jbyteArray nv21_data = (*env)->NewByteArray(env, len * 3 / 2);
    jbyte *nvaar = (*env)->GetByteArrayElements(env, nv21_data, 0);
    jint *argbaar = (*env)->GetIntArrayElements(env, jargbarray, 0);

    int y;
    int u;
    int v;

    for (int i = 0; i < jheight; ++i) {
        for (int j = 0; j < jwidth; ++j) {
            //屏蔽ARGB的透明度值
            int rgb = *(argbaar + i * jwidth + j) & 0x00FFFFFF;
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
            *(nvaar + i * jwidth + j) = (jbyte)y;
            *(nvaar + (len + (i >> 1) * jwidth + (j & ~1) + 0)) = (jbyte)u;
            *(nvaar + (len + +(i >> 1) * jwidth + (j & ~1) + 1)) = (jbyte)v;
        }
    }

    (*env)->ReleaseIntArrayElements(env, jargbarray, argbaar, 0);
    (*env)->ReleaseByteArrayElements(env, nv21_data, nvaar, 0);
    return nv21_data;
}

jclass
findSortClass(JNIEnv *env, const char* path)
{
    return (*env)->FindClass(env, path);
}

void
copyArray(JNIEnv *env, jmethodID method, jobjectArray *src, jobjectArray *des, jsize len, jboolean isUp)
{
    int temp[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int ii = 0;
    for (int i = 0; i < len; ++i) {
        if (temp[i] == 0)
        {
            int tempIndex = i;
            jobject *dobj = NULL;
            jobject obj = (*env)->GetObjectArrayElement(env, *src, i);
            dobj = &obj;
            int tempId = (*env)->CallIntMethod(env, obj, method, NULL);
            for (int j = 0; j < len; ++j) {
                if (i == j || temp[j] != 0)
                {
                    continue;
                }
                jobject obj = (*env)->GetObjectArrayElement(env, *src, j);
                int id = (*env)->CallIntMethod(env, obj, method, NULL);
                if (isUp)  //升序
                {
                    if (id < tempId)
                    {
                        dobj = &obj;
                        tempIndex = j;
                        tempId = id;
                    }
                } else
                {
                    if (id > tempId)
                    {
                        dobj = &obj;
                        tempIndex = j;
                        tempId = id;
                    }
                }
            }
            temp[tempIndex] = 1;
            (*env)->SetObjectArrayElement(env, *des, ii++, *dobj);
        }
    }
}

//jobject*
//sort(JNIEnv *env, jmethodID method, jobjectArray *array, jsize len)
//{
//
//}

JNIEXPORT jobjectArray
JNICALL
Java_com_timark_jnialgorithm_NativeImageUtils_nativeSort(JNIEnv *env, jobject obj, jobjectArray objarray, jboolean isUp)
{
    jsize len = (*env)->GetArrayLength(env, objarray);
    jclass clz = findSortClass(env, "com/timark/jnialgorithm/INativeSort");
    jmethodID method = (*env)->GetMethodID(env, clz, "getId", "()I");

    jobjectArray newArray = (*env)->NewObjectArray(env, len, clz, NULL);
    copyArray(env, method, &objarray, &newArray, len, isUp);

}
