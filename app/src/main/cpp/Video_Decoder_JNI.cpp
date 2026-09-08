// app/src/main/cpp/video_decoder_jni.cpp
#include <jni.h> // 👉 HERE is where JNI is explicitly used
#include <android/log.h>

#include <cstring>

#define LOG_TAG "JNI_DEBUG"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

/** 👈 ADD THIS INCLUDE
 *  for NDK-media codec
 */
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaFormat.h>

#include <android/native_window.h>
#include <android/native_window_jni.h>

// 1. A representation of your actual C++ Decoder class/engine
class Video_Decoder_JNI {
public:
    int width;
    int height;
    AMediaCodec* codec = nullptr;

    Video_Decoder_JNI(int w, int h) : width(w), height(h) {
        // Initialize your library, allocate internal frame buffers,
        // or set up codec context (e.g., FFmpeg, libvpx, or custom C++ state)
        LOGI("C++ Decoder created for resolution: %dx%d", width, height);

        // 1. Create H.264 Decoder
        codec = AMediaCodec_createDecoderByType("video/avc");

        // 2. Configure Format
        AMediaFormat* format = AMediaFormat_new();
        AMediaFormat_setString(format, AMEDIAFORMAT_KEY_MIME, "video/avc");
        AMediaFormat_setInt32(format, AMEDIAFORMAT_KEY_WIDTH, width);
        AMediaFormat_setInt32(format, AMEDIAFORMAT_KEY_HEIGHT, height);

        // 3. Configure and start Codec (passing null for surface and crypto)
        AMediaCodec_configure(codec, format, nullptr, nullptr, 0);
        AMediaCodec_start(codec);
        AMediaFormat_delete(format);
    }

    ~Video_Decoder_JNI() {
        // Free internal C++ frame buffers or codec state here
        LOGI("C++ Decoder destroyed");
        if(codec) {
            AMediaCodec_stop(codec);
            AMediaCodec_delete(codec);
        }
    }

    // REMOVE 'static' keyword here
    int decodeFrame(
            void *inputData,
            int inputSize,
            jlong timeUs,      // 👈 Added parameter
            jint flags,        // 👈 Added parameter
            void *outputData,
            jlong outputCapacity
      ) {
        // Your actual decoding algorithm/math goes here
        if (!codec) return -1;

        // 1. Feed the input data NAL UNIT to the codec (2000 microsecond timeout)
        ssize_t inIdx = AMediaCodec_dequeueInputBuffer(codec, 2000);
        if (inIdx >= 0) {
            size_t bufSize;
            uint8_t* inBuf = AMediaCodec_getInputBuffer(codec, inIdx, &bufSize);
            if(inBuf && inputSize <= bufSize) {
                memcpy(inBuf, inputData, inputSize);
                AMediaCodec_queueInputBuffer(codec, inIdx, 0, inputSize, timeUs, flags);
            }
        }

        // 2. EXTRACT THE UNCOMPRESSED YUV FRAME
        AMediaCodecBufferInfo info;
        ssize_t outIdx = AMediaCodec_dequeueOutputBuffer(codec, &info, 2000);
        if (outIdx >= 0) {
            __android_log_print(ANDROID_LOG_INFO, "JNI_DEBUG", "FRAME DECODED! Size: %d", info.size);
            size_t outSize;
            uint8_t* outBuf = AMediaCodec_getOutputBuffer(codec, outIdx, &outSize);

            // Copy if the hardware frame fits in your Kotlin buffer
            if (outBuf && info.size <= outputCapacity) {

            /**
              * Mismatch in below
              * current info.size = 3110400 or 3.1MB (YUV420P: 1.5-Bytes per pixel),
              * While, expectation is RGBA 8888, i.e: 4 bytes per pixel = 8.2MB
              */
                memcpy(outputData, outBuf + info.offset, info.size);
            }

            // Release back to the hardware pool
            AMediaCodec_releaseOutputBuffer(codec, outIdx, false);
            return 0; // Return 0 for success, -1 for error
        }

        return -1;  // Decoder still processing, no frame ready yet
    }
};

// 2. The JNI function called from Kotlin's NativeDecoder.initNative()
extern "C" {
//[[maybe_unused]] JNIEXPORT jlong JNICALL
JNIEXPORT jlong JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_initNative(
        JNIEnv *env,
        jobject thiz,
        jint width,
        jint height
) {

    // STEP A: Instantiate your C++ class on the native heap
    auto *decoder = new Video_Decoder_JNI(width, height);
    jlong jLong_ptr = reinterpret_cast<jlong>(decoder);

    // 🚀 ADD THIS LOG:
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Kotlin: Received Pointer = %lld", jLong_ptr);


    // STEP B: Cast the C++ memory pointer to a jlong and return it to Kotlin
    return jLong_ptr;
}
}

extern "C" {
JNIEXPORT jint JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_decodeNative(
        JNIEnv *env, jobject thiz,
        jlong decoder_ptr,
        jobject input_data,
        jint input_size,      // 👈 Matches Kotlin 'inputSize: Int'
        jlong time_us,        // 👈 Matches Kotlin 'timeUs: Long'
        jint flags,           // 👈 Matches Kotlin 'flags: Int'
        jobject output_buffer_obj,
        jlong capacity        // 👈 Matches Kotlin 'capacity: Long'
) {
    __android_log_print(ANDROID_LOG_INFO, "JNI_DEBUG", "=== C++ DECODE REACHED ===");

    // 1. Extract the Encoded Input Data pointer
    auto* encodedBytes = (uint8_t*) env->GetDirectBufferAddress(input_data);
    if (!encodedBytes || input_size == 0) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI_DEBUG", "Empty input buffer");
        return -1;
    }

    // 2. Extract the Output Buffer pointer
    jclass outputBufferClass = env->GetObjectClass(output_buffer_obj);
    jfieldID dataField = env->GetFieldID(outputBufferClass, "data", "Ljava/nio/ByteBuffer;");
    jobject dataBufferObj = env->GetObjectField(output_buffer_obj, dataField);

    if (!dataBufferObj) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI_DEBUG", "Output ByteBuffer is null");
        return -1;
    }

    auto *pixels = (uint8_t *) env->GetDirectBufferAddress(dataBufferObj);
    if (!pixels) return -1;

    // 3. RECOVER YOUR C++ INSTANCE
    auto *decoder = reinterpret_cast<Video_Decoder_JNI *>(decoder_ptr);

    // 4. DECODE AND WRITE
    // Pass the extracted pointers and variables into your C++ engine
    int decodeStatus = decoder->decodeFrame(
            encodedBytes,
            input_size,
            time_us,
            flags,
            pixels,
            capacity
    );

    if (decodeStatus < 0) {
        // Silently return -1 so Kotlin knows the hardware is still buffering
        return -1;
    }

    return 0;
}
}


extern "C" {
JNIEXPORT jint JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_releaseNative(JNIEnv *env, jobject thiz, jlong decoder_ptr) {


    // 1. CRITICAL STEP: Check for a valid pointer
    if (decoder_ptr == 0) {
        return -1; // Return -1 if the pointer is invalid/null
    }

    // We cast the jlong (the address) back into a C++ pointer
    // 2. THE CLEANUP (The actual "work")
    auto *decoder = reinterpret_cast<Video_Decoder_JNI *>(decoder_ptr);

    // We call the destructor or a manual cleanup function
    // This frees the memory and closes the codec
    if (decoder) {
        // 3. Perform the cleanup (calling the destructor)
        delete decoder;
    }

    // 3. SUCCESS SIGNAL
    return 0; // <-- This is the line you are looking for (Line #62-64 area)
}
}
extern "C" {
JNIEXPORT jint JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_renderToSurface(
        JNIEnv *env, jobject thiz, jlong decoder_ptr, jobject output_buffer, jobject surface
        ) {

    __android_log_print(ANDROID_LOG_INFO, "JNI_DEBUG", "=== C++ RENDER REACHED ===");

    // 2-1. Get the class and fields
    jclass outputBufferClass = env->GetObjectClass(output_buffer);
    jfieldID dataField = env->GetFieldID(outputBufferClass, "data", "Ljava/nio/ByteBuffer;");

    // 2-2 Extract the ByteBuffer pointer
    jobject dataBufferObj = env->GetObjectField(output_buffer, dataField);
    if(!dataBufferObj) return -1; // Drop frame if buffer is null

    auto* decodedPixels = (uint8_t*) env->GetDirectBufferAddress(dataBufferObj);


    // Get the physical window
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (!window) return -1;

    // 3. FORCE FORMAT: Ensure the buffer allocates 4 bytes per pixel (RGBA_8888)
    // Passing 0, 0 keeps the surface's existing width/height
    ANativeWindow_setBuffersGeometry(window, 0, 0, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer windowBuffer;

    // 4. LOCK
    if (ANativeWindow_lock(window, &windowBuffer, nullptr) == 0) {

        // 5. PAINT ROW-BY-ROW (Respecting the stride padding)
        // Cast to uint8_t* first so pointer math operates byte-by-byte
        auto* destLine = (uint8_t*) windowBuffer.bits;
        auto* srcLine = decodedPixels;
        int bytePerPixel = 4;   // RGBA_8888

        for (int y = 0; y < windowBuffer.height; y++) {
            /**
             * working solid-Magenta color, instead of moving frames
             *
             * // Cast the start of the current row back to 32-bit pixels
             * auto* pixels = (uint32_t*) destLine;
             * for (int x = 0; x < windowBuffer.width; x++) {
             *    pixels[x] = 0x2FFF00FF; // RED (AARRGGBB)       // it is working with Solid Magenta color
             * }
             */

            //copy exactly one row of visiable width
            memcpy(destLine, srcLine, windowBuffer.width * bytePerPixel);

            // Advance the destination pointer by the stride (padding included) in bytes
            destLine += (windowBuffer.stride * bytePerPixel);
            // Advance the source pointer by the width in bytes (assuming C++ output has no stride padding)
            srcLine += (windowBuffer.stride * bytePerPixel);
        }

        // 6. POST
        ANativeWindow_unlockAndPost(window);
    }

    ANativeWindow_release(window);
    return 0;
}
}