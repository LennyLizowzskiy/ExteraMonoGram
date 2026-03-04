#include <jni.h>
#include <string>
#include <android/log.h>

#include "vpx/vpx_decoder.h"
#include "vpx/vp8dx.h"

extern "C" JNIEXPORT jstring JNICALL
Java_org_monogram_presentation_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    const char* vpxVersion = vpx_codec_version_str();

    return env->NewStringUTF(vpxVersion);
}