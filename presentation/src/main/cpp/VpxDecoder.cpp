#include "VpxDecoder.h"
#include <android/log.h>
#include <algorithm>

#define LOG_TAG "VpxDecoder"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

VpxDecoder::VpxDecoder() {
    inputBuffer.resize(1024 * 1024);
}

VpxDecoder::~VpxDecoder() {
    close();
}

bool VpxDecoder::open(int fd, off_t offset, off_t length) {
    extractor = AMediaExtractor_new();
    media_status_t err = AMediaExtractor_setDataSourceFd(extractor, fd, offset, length);

    if (err != AMEDIA_OK) {
        LOGE("Failed to set data source");
        return false;
    }

    int numTracks = AMediaExtractor_getTrackCount(extractor);
    int videoTrackIndex = -1;

    for (int i = 0; i < numTracks; i++) {
        AMediaFormat* format = AMediaExtractor_getTrackFormat(extractor, i);
        const char* mime;
        if (AMediaFormat_getString(format, AMEDIAFORMAT_KEY_MIME, &mime)) {
            if (std::string(mime) == "video/x-vnd.on2.vp9") {
                codec_iface = vpx_codec_vp9_dx();
                videoTrackIndex = i;

                AMediaFormat_getInt32(format, AMEDIAFORMAT_KEY_WIDTH, &width);
                AMediaFormat_getInt32(format, AMEDIAFORMAT_KEY_HEIGHT, &height);
            } else if (std::string(mime) == "video/x-vnd.on2.vp8") {
                codec_iface = vpx_codec_vp8_dx();
                videoTrackIndex = i;

                AMediaFormat_getInt32(format, AMEDIAFORMAT_KEY_WIDTH, &width);
                AMediaFormat_getInt32(format, AMEDIAFORMAT_KEY_HEIGHT, &height);
            }
        }
        AMediaFormat_delete(format);
        if (videoTrackIndex >= 0) break;
    }

    if (videoTrackIndex < 0 || !codec_iface) {
        LOGE("No VP8/VP9 track found");
        return false;
    }

    AMediaExtractor_selectTrack(extractor, videoTrackIndex);

    vpx_codec_dec_cfg_t cfg = {0};
    cfg.threads = 1;
    cfg.w = 0;
    cfg.h = 0;

    if (vpx_codec_dec_init(&codec_ctx, codec_iface, &cfg, 0)) {
        LOGE("Failed to initialize libvpx: %s", vpx_codec_error(&codec_ctx));
        return false;
    }

    frameCount = 0;
    isCodecInitialized = true;
    return true;
}

long VpxDecoder::decodeFrame(JNIEnv* env, jobject bitmap) {
    if (!isCodecInitialized) return -1;

    int64_t currentPtsUs = AMediaExtractor_getSampleTime(extractor);
    ssize_t sampleSize = AMediaExtractor_readSampleData(extractor, inputBuffer.data(), inputBuffer.size());

    if (sampleSize < 0) {
        if (frameCount <= 1) {
            return -2;
        }

        AMediaExtractor_seekTo(extractor, 0, AMEDIAEXTRACTOR_SEEK_PREVIOUS_SYNC);
        return 0;
    }

    vpx_codec_err_t err = vpx_codec_decode(&codec_ctx, inputBuffer.data(), sampleSize, nullptr, 0);
    if (err) {
        LOGE("Decode error: %s", vpx_codec_error(&codec_ctx));
        AMediaExtractor_advance(extractor);
        return 0;
    }

    vpx_codec_iter_t iter = nullptr;
    vpx_image_t* img = vpx_codec_get_frame(&codec_ctx, &iter);

    long durationMs = 33;

    if (img) {
        void* bitmapPixels;
        if (AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels) == 0) {
            AndroidBitmapInfo info;
            AndroidBitmap_getInfo(env, bitmap, &info);
            convertYuvToRgb(img, (uint8_t*)bitmapPixels, info.stride);
            AndroidBitmap_unlockPixels(env, bitmap);
        }

        frameCount++;

        bool hasNext = AMediaExtractor_advance(extractor);
        if (hasNext) {
            int64_t nextPtsUs = AMediaExtractor_getSampleTime(extractor);
            if (nextPtsUs > currentPtsUs) {
                durationMs = (long)((nextPtsUs - currentPtsUs) / 1000);
            }
        } else {
            durationMs = lastFrameDurationMs;
        }

        if (durationMs > 0) lastFrameDurationMs = durationMs;

        return lastFrameDurationMs;
    }

    AMediaExtractor_advance(extractor);
    return 0;
}
void VpxDecoder::close() {
    if (isCodecInitialized) {
        vpx_codec_destroy(&codec_ctx);
        isCodecInitialized = false;
    }
    if (extractor) {
        AMediaExtractor_delete(extractor);
        extractor = nullptr;
    }
}

void VpxDecoder::convertYuvToRgb(const vpx_image_t* img, uint8_t* dst, int dstStride) {
    int w = img->d_w;
    int h = img->d_h;

    uint8_t* yPlane = img->planes[VPX_PLANE_Y];
    uint8_t* uPlane = img->planes[VPX_PLANE_U];
    uint8_t* vPlane = img->planes[VPX_PLANE_V];

    int yStride = img->stride[VPX_PLANE_Y];
    int uStride = img->stride[VPX_PLANE_U];
    int vStride = img->stride[VPX_PLANE_V];

    for (int y = 0; y < h; ++y) {
        uint8_t* dstRow = dst + y * dstStride;
        uint8_t* yRow = yPlane + y * yStride;
        uint8_t* uRow = uPlane + (y / 2) * uStride;
        uint8_t* vRow = vPlane + (y / 2) * vStride;

        for (int x = 0; x < w; ++x) {
            int Y = yRow[x] - 16;
            int U = uRow[x / 2] - 128;
            int V = vRow[x / 2] - 128;

            int Y298 = 298 * Y;
            int r = (Y298 + 409 * V + 128) >> 8;
            int g = (Y298 - 100 * U - 208 * V + 128) >> 8;
            int b = (Y298 + 516 * U + 128) >> 8;

            dstRow[x * 4 + 0] = std::clamp(r, 0, 255);
            dstRow[x * 4 + 1] = std::clamp(g, 0, 255);
            dstRow[x * 4 + 2] = std::clamp(b, 0, 255);
            dstRow[x * 4 + 3] = 255;
        }
    }
}