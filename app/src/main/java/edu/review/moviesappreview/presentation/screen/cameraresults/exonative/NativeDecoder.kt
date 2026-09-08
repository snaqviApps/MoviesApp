package edu.review.moviesappreview.presentation.screen.cameraresults.exonative

import android.view.Surface
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.VideoDecoderOutputBuffer
import java.nio.ByteBuffer

@OptIn(UnstableApi::class)
class NativeDecoder {
    private var decoderPtr: Long = 0L

    // Unified external declarations matching the C++ functions
    private external fun initNative(width: Int, height: Int): Long

    private external fun decodeNative(
        ptr: Long,
        input: ByteBuffer,
        inputSize: Int,
        timeUs: Long,
        flags: Int,
        output: VideoDecoderOutputBuffer,
        capacity: Long
    ): Int

//    private external fun initNativeDecoder(width: Int, height: Int): Long
    private external fun releaseNative(ptr: Long): Int

    fun init(width: Int, height: Int) {
        if (decoderPtr == 0L) {
            decoderPtr = initNative(width, height)

            // 🚀 Added THIS LOG, to see if the pointer:decodePtr is working
            android.util.Log.d("JNI_DEBUG", "Kotlin: Received Pointer = $decoderPtr")
        }
    }

//    fun decode(inputData: ByteBuffer?, outputData: ByteBuffer?): Int {
    fun decode(
            inputSize: Int,
            timeUs: Long,
            flags: Int,
            inputData: ByteBuffer?,
            outputBuffer: VideoDecoderOutputBuffer?,
            capacity: Long
        ): Int {
        if (inputData == null || outputBuffer == null || decoderPtr == 0L) return -1
         return decodeNative(
             decoderPtr,
             inputData,
             inputSize,
             timeUs,
             flags,
             outputBuffer,
             capacity
         )
    }

    fun release() {
        if (decoderPtr != 0L) {
            releaseNative(decoderPtr)
            decoderPtr = 0L
        }
    }

    private external fun renderToSurface(
        decoderPtr: Long,
        outputBuffer: VideoDecoderOutputBuffer,
        surface: Surface
    ): Int

    fun render(outputBuffer: VideoDecoderOutputBuffer, surface: Surface) {
        if (decoderPtr != 0L) {
            renderToSurface(
                decoderPtr,
                outputBuffer,
                surface
            )
        }
    }
}
