package edu.review.moviesappreview.presentation.screen.camerascream.exonative

import java.nio.ByteBuffer

// 2. THE JNI BRIDGE LAYER (Can be in the same file!)
class NativeDecoder {
    private var decoderPtr: Long = 0L
    private external fun initNative(width: Int, height: Int): Long

    // ... load library ...
    fun init(width: Int, height: Int) {
        decoderPtr = initNative(width, height)
    }



    fun decode(inputData: ByteBuffer?, outputData: ByteBuffer?): Int {
        if (inputData == null || outputData == null || decoderPtr == 0L) return -1
        return decodeNative(decoderPtr, inputData, outputData)
    }

    fun release() {
        if (decoderPtr != 0L) {
            releaseNative(decoderPtr)
            decoderPtr = 0L
        }
    }

    private external fun initNative(): Long
    private external fun decodeNative(decoderPtr: Long, input: ByteBuffer, output: ByteBuffer): Int
    private external fun releaseNative(decoderPtr: Long)
}