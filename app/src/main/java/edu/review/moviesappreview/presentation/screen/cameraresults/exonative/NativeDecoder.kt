package edu.review.moviesappreview.presentation.screen.cameraresults.exonative

import java.nio.ByteBuffer

class NativeDecoder {
    private var decoderPtr: Long = 0L

    // Unified external declarations matching the C++ functions
    private external fun initNative(width: Int, height: Int): Long
    private external fun decodeNative(ptr: Long, input: ByteBuffer, output: ByteBuffer): Int
    private external fun releaseNative(ptr: Long): Int

    fun init(width: Int, height: Int) {
        if (decoderPtr == 0L) {
            decoderPtr = initNative(width, height)
        }
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
}