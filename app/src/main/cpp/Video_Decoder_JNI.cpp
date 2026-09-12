
#include <android/native_window_jni.h>
#include <android/native_window.h>

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


// 1. A representation of your actual C++ Decoder class/engine
class Video_Decoder_JNI {
public:
    int width;
    int height;
    AMediaCodec* codec = nullptr;

    Video_Decoder_JNI(
            int w,
            int h,
            ANativeWindow* window,               // added to handle Surface
            const char* mimeType
    ) : width(w), height(h) {
        // Initialize your library, allocate internal frame buffers,
        // or set up codec context (e.g., FFmpeg, libvpx, or custom C++ state)
        LOGI("C++ Decoder created for resolution: %dx%d", width, height);



        // 1. Instead of hard-coding H.264 Decoder,now generalizing codec, by Passing it to the decoder creation factory
        codec = AMediaCodec_createDecoderByType(mimeType);

        // 2. Configure Format,
        AMediaFormat* format = AMediaFormat_new();

        // 3. Pass generalizing codec, to the Format configuration
        AMediaFormat_setString(format, AMEDIAFORMAT_KEY_MIME, mimeType);
        AMediaFormat_setString(format, AMEDIAFORMAT_KEY_MIME, "video/avc");
        AMediaFormat_setInt32(format, AMEDIAFORMAT_KEY_WIDTH, width);
        AMediaFormat_setInt32(format, AMEDIAFORMAT_KEY_HEIGHT, height);

        // 3. Configure and start Codec (passing null for surface and crypto)
        // Pass the window here instead of nullptr
        AMediaCodec_configure(codec, format, window, nullptr, 0);
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

    int decodeFrame (
            void *inputData,
            int inputSize,
            jlong timeUs,
            jint flags,
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

            // ADD THIS LOG:
            __android_log_print(ANDROID_LOG_INFO, "JNI_DEBUG", "C++ OUT - Index: %d, TimeUs: %lld",
                    (int)outIdx, (long long)info.presentationTimeUs);

            // 1. ❌ DO NOT use AMediaCodec_getOutputBuffer or memcpy anymore!

            // 2. Write the hardware buffer index into the very start of the output ByteBuffer
            auto* outPtr = (int32_t*) outputData;

            // 2-1. Write the 32-bit hardware index at byte offset 0
            outPtr[0] = (int32_t) outIdx;


            // 2. 🚀 Write the 64-bit timestamp strictly at Byte offset 8
            auto* timePtr = (int64_t*) ((uint8_t*)outputData + 8);
            timePtr[0] = (int64_t) info.presentationTimeUs;


            // We return 0 and wait for ExoPlayer to call render.
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
        jint height,
        jobject surface,
        jstring mime_type           // 👈 Accept jstring (step# 4 of generalizing)
) {

    // 4-1. Convert Java string to C++ string safely
    const char *nativeMimeType = env->GetStringUTFChars(mime_type, nullptr);

    // 1. Convert Java Surface to C++ Window
    ANativeWindow* window = nullptr;
    if (surface != nullptr) {
        window = ANativeWindow_fromSurface(env, surface);
    }

    // 2. Pass the window into your constructor
    // 4-2. 2. Pass nativeMimeType to your C++ constructor
    auto *decoder = new Video_Decoder_JNI(width, height, window, nativeMimeType);

    // 3. Release our reference (AMediaCodec keeps its own internal reference)
    if (window) {
        ANativeWindow_release(window);
    }

    // 4-3. 🚀 CRITICAL: Release the string memory to prevent leaks!
    env->ReleaseStringUTFChars(mime_type, nativeMimeType);

    // cast @param decoder to kotlin compatible pointer
    auto jLong_ptr = reinterpret_cast<jlong>(decoder);

    // 🚀 ADD THIS LOG:
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Kotlin: Received Pointer = %lld", jLong_ptr);


    // STEP B: Cast the C++ memory pointer to a jlong and return it to Kotlin
//    return jLong_ptr;
    return reinterpret_cast<jlong>(decoder);
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
        // life-Cycle management to clear memory
        if(decoder->codec) {
            // Power down the chip and return it to the Android OS
            __android_log_print(ANDROID_LOG_INFO, "JNI_DEBUG", "=== C++ HARDWARE POWERED DOWN ===");

            AMediaCodec_stop(decoder->codec);
            AMediaCodec_delete(decoder->codec);

            decoder->codec = nullptr; // 🌟 FIX: Nullify to prevent double-deletion in destructor

        }

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
        JNIEnv *env, jobject thiz, jlong decoder_ptr, jint out_idx
) {
    __android_log_print(ANDROID_LOG_INFO, "JNI_DEBUG", "=== C++ HARDWARE RENDER TRIGGERED ===");

    auto *decoder = reinterpret_cast<Video_Decoder_JNI *>(decoder_ptr);

    if (decoder && decoder->codec) {
        // 🚀 THE MAGIC: 'true' tells the GPU to convert YUV to RGBA and paint it!
        // No manual window locking, no memcpy, zero CPU usage.
        AMediaCodec_releaseOutputBuffer(decoder->codec, out_idx, true);
    }
    return 0;
}
}

extern "C" {
JNIEXPORT void JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_flushNative(
        JNIEnv *env, jobject thiz, jlong decoder_ptr
) {
    auto *decoder = reinterpret_cast<Video_Decoder_JNI *>(decoder_ptr);
    if (decoder && decoder->codec) {
        // 🚀 Dumps the internal hardware buffers so old frames don't mismatch the new audio clock
        AMediaCodec_flush(decoder->codec);
    }
}
}

extern "C" {
JNIEXPORT void JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_dropFrameNative(
        JNIEnv *env, jobject thiz, jlong decoder_ptr, jint out_idx
) {
    auto *decoder = reinterpret_cast<Video_Decoder_JNI *>(decoder_ptr);
    if (decoder && decoder->codec) {
        // 🚀 'false' tells the GPU to release the memory WITHOUT painting it
        AMediaCodec_releaseOutputBuffer(decoder->codec, out_idx, false);
    }
}
}

extern "C" {
JNIEXPORT void JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_setSurfaceNative(
        JNIEnv *env, jobject thiz, jlong decoder_ptr, jobject surface
) {
    auto *decoder = reinterpret_cast<Video_Decoder_JNI *>(decoder_ptr);
    if (decoder && decoder->codec && surface != nullptr) {
        ANativeWindow* window = ANativeWindow_fromSurface(env, surface);

        // 🚀 Dynamically bind the UI to the running hardware chip
        AMediaCodec_setOutputSurface(decoder->codec, window);
        ANativeWindow_release(window);
    }
}
}