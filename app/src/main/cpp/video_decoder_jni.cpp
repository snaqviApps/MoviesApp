// app/src/main/cpp/video_decoder_jni.cpp
#include <jni.h> // 👉 HERE is where JNI is explicitly used
#include <android/log.h>

#define LOG_TAG "MyCustomCodec"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// 1. A representation of your actual C++ Decoder class/engine
class CustomCppDecoder {
public:
    int width;
    int height;

    CustomCppDecoder(int w, int h) : width(w), height(h) {
        // Initialize your library, allocate internal frame buffers,
        // or set up codec context (e.g., FFmpeg, libvpx, or custom C++ state)
        LOGI("C++ Decoder created for resolution: %dx%d", width, height);
    }

    ~CustomCppDecoder() {
        // Free internal C++ frame buffers or codec state here
        LOGI("C++ Decoder destroyed");
    }

    int decodeFrame(void* inputData, int inputSize, void* outputData) {
        // Your actual decoding algorithm/math goes here
        return 0; // Return 0 for success, -1 for error
    }
};

// 2. The JNI function called from Kotlin's NativeDecoder.initNative()
extern "C"
[[maybe_unused]] JNIEXPORT jlong JNICALL
Java_com_moviesappreview_NativeDecoder_initNative(
        JNIEnv *env,
        jobject thiz,
        jint width,
        jint height) {

    // STEP A: Instantiate your C++ class on the native heap
    auto *decoder = new CustomCppDecoder(width, height);

    // STEP B: Cast the C++ memory pointer to a jlong and return it to Kotlin
    return reinterpret_cast<jlong>(decoder);
}