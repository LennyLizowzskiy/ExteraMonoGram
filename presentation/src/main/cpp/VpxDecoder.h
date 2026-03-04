#pragma once

#include <jni.h>
#include <android/native_window.h>
#include <android/asset_manager.h>
#include <media/NdkMediaExtractor.h>
#include <android/bitmap.h>
#include <vector>
#include <string>

#include "vpx/vpx_decoder.h"
#include "vpx/vp8dx.h"

class VpxDecoder {
public:
    VpxDecoder();
    ~VpxDecoder();

    bool open(int fd, off_t offset, off_t length);
    long decodeFrame(JNIEnv* env, jobject bitmap);
    void close();

    [[nodiscard]] int getWidth() const { return width; }
    [[nodiscard]] int getHeight() const { return height; }

private:
    AMediaExtractor* extractor = nullptr;
    vpx_codec_ctx_t codec_ctx;
    vpx_codec_iface_t* codec_iface = nullptr;

    bool isCodecInitialized = false;
    int width = 0;
    int height = 0;
    long lastFrameDurationMs = 33;

    int frameCount = 0;

    std::vector<uint8_t> inputBuffer;

    void convertYuvToRgb(const vpx_image_t* img, uint8_t* dstPixels, int dstStride);
};