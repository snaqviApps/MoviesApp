package edu.review.moviesappreview.presentation.screen.cameraresults.exonative

import android.view.Surface
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.VideoDecoderOutputBuffer
import java.nio.ByteBuffer


/**
 *  Kotlin Wrapper for C++ Decoder
 */
@OptIn(UnstableApi::class)
class NativeDecoder {
    private var decoderPtr: Long = 0L


    private external fun decodeNative(
        ptr: Long,
        input: ByteBuffer,
        inputSize: Int,
        timeUs: Long,
        flags: Int,
        output: VideoDecoderOutputBuffer,
        capacity: Long
    ): Int


    /**
     *  Step# 2:
     * ...Continue from MyCustomDecoder.kt (step# 1)
     * 1. Add mimeType to the native signature
     * 2. Add it to your public Kotlin method. line#36 init()
     *
     */
    private external fun initNative(
        width: Int, height: Int, surface: Surface?, mimeType: String
    ): Long

    fun init(width: Int, height: Int, surface: Surface?, mimeType: String) {
        if (decoderPtr == 0L) {
            decoderPtr = initNative(width, height, surface, mimeType)

            // 🚀 Added THIS LOG, to see if the pointer:decodePtr is working
            android.util.Log.d("JNI_DEBUG", "Kotlin: Received Pointer = $decoderPtr")
        }
    }

    private external fun releaseNative(ptr: Long): Int
    fun release() {
        if (decoderPtr != 0L) {
            releaseNative(decoderPtr)
            decoderPtr = 0L             // Nullify to prevent double-deletion crashes
        }
    }


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


    private external fun renderToSurface(decoderPtr: Long, outIdx: Int): Int
    fun render(outIdx: Int) {
        if (decoderPtr != 0L) {
            renderToSurface(decoderPtr, outIdx)
        } else {
            android.util.Log.e("JNI_DEBUG", "KOTLIN RENDER FAILED: C++ pointer is 0L!")
        }
    }

    private external fun flushNative(decoderPtr: Long)

    fun flush() {
        if(decoderPtr != 0L) {
            flushNative(decoderPtr)
        }
    }

    private external fun dropFrameNative(decoderPtr: Long, outIdx: Int)

    fun dropFrame(outIdx: Int) {
        if(decoderPtr != 0L) {
            dropFrameNative(decoderPtr, outIdx)
        }
    }

    /**
     * Route the Surface in Kotlin
     * @author Sayyid Naqvi
     */
    private external fun setSurfaceNative(decoderPtr: Long, surface: Surface)

    fun setSurface(surface: Surface) {
        if(decoderPtr != 0L) {
            setSurfaceNative(decoderPtr, surface)
        }
    }

}
