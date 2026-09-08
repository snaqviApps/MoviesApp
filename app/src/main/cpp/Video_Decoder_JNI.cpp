// app/src/main/cpp/video_decoder_jni.cpp
#include <jni.h> // 👉 HERE is where JNI is explicitly used
#include <android/log.h>

#include <cstring>

#define LOG_TAG "JNI_DEBUG"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#include <android/native_window.h>
#include <android/native_window_jni.h>

// 1. A representation of your actual C++ Decoder class/engine
class Video_Decoder_JNI {
public:
    int width;
    int height;

    Video_Decoder_JNI(int w, int h) : width(w), height(h) {
        // Initialize your library, allocate internal frame buffers,
        // or set up codec context (e.g., FFmpeg, libvpx, or custom C++ state)
        LOGI("C++ Decoder created for resolution: %dx%d", width, height);
    }

    ~Video_Decoder_JNI() {
        // Free internal C++ frame buffers or codec state here
        LOGI("C++ Decoder destroyed");
    }

//    static int decodeFrame(void *inputData, int inputSize, void *outputData) {
    static int decodeFrame(void *inputData, int inputSize, void *outputData, jlong outputCapacity) {
        // Your actual decoding algorithm/math goes here

        auto* outPixels = (uint32_t*) outputData;

        // Calculate exactly how many 32-bit pixels we can safely fit in the provided memory
        jlong safePixelCount = outputCapacity / 4;


        // Fill a 1920x1080 buffer with solid Green
        // for (int i = 0; i < 1920 * 1080; i++) {

        // FIll safely, withOut pipeline-crashing, top-portion
        for (int i = 0; i < safePixelCount; i++) {
            outPixels[i] = 0xFF00FF00; // Little-Endian ABGR
        }

        return 0; // Return 0 for success, -1 for error
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
        JNIEnv *env,
        jobject thiz,
        jlong decoder_ptr,
        jobject input_data,
        jobject output_buffer_obj
) {
    __android_log_print(ANDROID_LOG_INFO, "JNI_DEBUG", "=== C++ DECODE REACHED ===");

    // 1. Extract the Encoded Input Data
    auto* encodedBytes = (uint8_t*) env->GetDirectBufferAddress(input_data);
    jlong encodedSize = env->GetDirectBufferCapacity(input_data);

    if(!encodedBytes || encodedSize == 0) {
        __android_log_print(ANDROID_LOG_INFO, "JNI_DEBUG", "Empty input buffer");
        return -1;
    }

    // 2. Extract the Output Buffer
    jclass outputBufferClass = env->GetObjectClass(output_buffer_obj);
    jfieldID dataField = env->GetFieldID(outputBufferClass, "data", "Ljava/nio/ByteBuffer;");
    jobject dataBufferObj = env->GetObjectField(output_buffer_obj, dataField);

    // 🚀 ADD THIS CHECK:
    if (!dataBufferObj) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI_DEBUG", "Output ByteBuffer is null");
        return -1;
    }


    //    jfieldID modeField = env->GetFieldID(outputBufferClass, "mode", "I");


    auto *pixels = (uint8_t *) env->GetDirectBufferAddress(dataBufferObj);
    jlong capacity = env->GetDirectBufferCapacity(dataBufferObj);
    if (!pixels) return -1;

    // 3. RECOVER YOUR C++ INSTANCE
    auto *decoder = reinterpret_cast<Video_Decoder_JNI *>(decoder_ptr);

    // 4. DECODE AND WRITE
    // Pass the encoded bytes IN, and the output `pixels` buffer to write the uncompressed frame OUT
    int decodeStatus = decoder->decodeFrame(encodedBytes, encodedSize, pixels, capacity);

    if (decodeStatus < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI_DEBUG", "C++ Engine failed to decode frame");
        return -1;
    }

//    // 1. Calculate the exact sizes
//    int width = 1920;
//    int height = 1080;
//    int y_size = width * height;

    // 2. Only write to the Y plane (the first portion of the buffer)
    // This makes the screen definitely opaque (no more transparency)



//    if (pixels) {
//        // Fill with a visible grey value (128)
//        memset(pixels, 128, y_size);
//        for (int i = 0; i < capacity; i++) {
//            // Create a repeating 0-255 gradient
//            pixels[i] = (uint8_t)(i % 256);
//        }
//    }

    // 3. THE HANDSHAKE
//    env->SetIntField(output_buffer_obj, modeField, 0);

    // Call with EXACT strides
//    jmethodID initMethod = env->GetMethodID(outputBufferClass, "initForYuvFrame", "(IIIII)Z");
//    env->CallBooleanMethod(output_buffer_obj, initMethod, width, height, width, width / 2, 1);




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