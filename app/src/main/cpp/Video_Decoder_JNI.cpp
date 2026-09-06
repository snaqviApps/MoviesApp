// app/src/main/cpp/video_decoder_jni.cpp
#include <jni.h> // 👉 HERE is where JNI is explicitly used
#include <android/log.h>

#define LOG_TAG "MyCustomCodec"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

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

    int decodeFrame(void* inputData, int inputSize, void* outputData) {
        // Your actual decoding algorithm/math goes here
        return 0; // Return 0 for success, -1 for error
    }
};

// 2. The JNI function called from Kotlin's NativeDecoder.initNative()
extern "C"
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

    // STEP B: Cast the C++ memory pointer to a jlong and return it to Kotlin
    return reinterpret_cast<jlong>(decoder);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_MyCustomDecoder_initNativeDecoder(JNIEnv *env, jobject thiz) {
    // TODO: implement initNativeDecoder()
}

extern "C"
JNIEXPORT jint JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_decodeNative(
        JNIEnv *env, jobject thiz, jlong decoder_ptr, jobject input_data, jobject output_data
        ) {
    // 1. Cast the jlong (the address) back into a C++ pointer
    auto* decoder = reinterpret_cast<Video_Decoder_JNI*>(decoder_ptr);

    if (!decoder) {
        // Handle null pointer case
        return -1;
    }

    // --- JNI Data Extraction (CRITICAL STEP) ---
    // 2. Convert the JNI ByteBuffer objects (input_data, output_data) into
    //    C++ pointers (void* input, void* output) and sizes (inputSize, outputSize).
    //    This requires complex JNI calls like GetDirectBufferAddress.

    // Placeholder for actual data extraction
    void* input_buffer = nullptr;
    int input_size = 0;

    void* output_buffer = nullptr;
    int output_size = 0;
    // ------------------------------------------

    // 3. Call the actual decoding method from your C++ class
    int status = decoder->decodeFrame(input_buffer, input_size, output_buffer);

    return status;
}


extern "C"
JNIEXPORT jint JNICALL
Java_edu_review_moviesappreview_presentation_screen_cameraresults_exonative_NativeDecoder_releaseNative(JNIEnv *env, jobject thiz, jlong decoder_ptr) {


    // 1. CRITICAL STEP: Check for a valid pointer
    if (decoder_ptr == 0) {
        return -1; // Return -1 if the pointer is invalid/null
    }

    // We cast the jlong (the address) back into a C++ pointer
    // 2. THE CLEANUP (The actual "work")
    auto* decoder = reinterpret_cast<Video_Decoder_JNI*>(decoder_ptr);

    // We call the destructor or a manual cleanup function
    // This frees the memory and closes the codec
    if(decoder) {
        // 3. Perform the cleanup (calling the destructor)
        delete decoder;
    }

    // 3. SUCCESS SIGNAL
    return 0; // <-- This is the line you are looking for (Line #62-64 area)
}