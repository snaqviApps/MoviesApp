package edu.review.moviesappreview.presentation.screen.cameraresults.exonative

import android.view.Surface
import androidx.annotation.OptIn
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.DecoderException
import androidx.media3.decoder.DecoderInputBuffer
import androidx.media3.decoder.SimpleDecoder
import androidx.media3.decoder.VideoDecoderOutputBuffer
import androidx.media3.common.C.BUFFER_FLAG_END_OF_STREAM

@OptIn(UnstableApi::class)
class MyCustomDecoder(
    private val format: Format,
    private val surface: Surface?
) : SimpleDecoder<DecoderInputBuffer, VideoDecoderOutputBuffer, DecoderException>(
    arrayOfNulls(NUM_INPUT_BUFFERS),
    arrayOfNulls(NUM_OUTPUT_BUFFERS)
) {
    companion object {
        private const val NUM_INPUT_BUFFERS = 8
        private const val NUM_OUTPUT_BUFFERS = 8
    }

    val nativeDecoder = NativeDecoder()

    // 1. ADD THESE TWO THINGS:
    @Volatile
    private var currentOutputMode: Int = androidx.media3.common.C.VIDEO_OUTPUT_MODE_NONE

    fun setOutputMode(mode: Int) {
        this.currentOutputMode = mode
    }

    init {
        // 1. Load the compiled C++ library, for custom-codec
        System.loadLibrary("moviesappreview")

        // 2. Initialize the C++ engine via the JNI bridge
        nativeDecoder.init(format.width, format.height, surface)
    }

    override fun getName(): String = "MyCustomDecoder"

    override fun createInputBuffer(): DecoderInputBuffer {
        return DecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_DIRECT)
    }

    override fun createOutputBuffer(): VideoDecoderOutputBuffer {
        return VideoDecoderOutputBuffer { outputBuffer -> releaseOutputBuffer(outputBuffer) }
    }

    override fun createUnexpectedDecodeException(error: Throwable): DecoderException {
        return DecoderException("Unexpected decode error", error)
    }

//    override fun decode(
//        inputBuffer: DecoderInputBuffer,
//        outputBuffer: VideoDecoderOutputBuffer,
//        reset: Boolean
//    ): DecoderException? {
//
//        // 1. Manually ensure the output buffer has a ByteBuffer allocated
//        // We calculate size: Width * Height * 4 (for RGBA_8888)
//        val width = format.width
//        val height = format.height
//
//        // This function tells ExoPlayer to prepare the buffer for the given size
////        outputBuffer.init(inputBuffer.timeUs, VideoDecoderOutputBuffer.COLORSPACE_BT2020, null)
//        outputBuffer.initForYuvFrame(
//            width,
//            height,
//            width,
//            width / 2,
//            VideoDecoderOutputBuffer.COLORSPACE_BT2020
//        )
//
//        android.util.Log.d("JNI_DEBUG", "KOTLIN DECODE TRIGGERED")
//
//
//        // 3. Delegate the actual math to the C++ engine
//
//        // 3-1. Safety Check: If data is not ready, just skip this frame!
//        val inputData = inputBuffer.data
//        val outputData = outputBuffer.data
//        if (inputData == null || outputData == null) {
//            // We return null (no exception), but don't call C++.
//            // This tells ExoPlayer: "Nothing to see here, moving on."
//            return null
//        }
//
////        val result = nativeDecoder.decode(inputData, outputBuffer = outputBuffer)
//
//        // 1. Extract the exact payload size, time, and config flags
//        val inputSize = inputData.limit()
//        val timeUs = inputBuffer.timeUs
//
//        // 2. Pass them to C++, hardcoding flags to 0
//        val result = nativeDecoder.decode(
//            inputSize,
//            timeUs,
//            0,
//            inputData, // 👈 Hardcoded
//            outputBuffer,
//            outputData.capacity().toLong()
//        )
//
//        if (result < 0) {
//            // 1. DO NOT throw DecoderException.
//            // 2. Silently flag the buffer as decode-only so ExoPlayer drops it but keeps the pipeline alive.
//            outputBuffer.addFlag(androidx.media3.common.C.BUFFER_FLAG_DECODE_ONLY)
//            return null
//        }
//
//        // 4. Pass the timing information back to ExoPlayer
//        outputBuffer.timeUs = inputBuffer.timeUs
//        outputBuffer.mode = currentOutputMode
//
//        // Clear any error flags
//        outputBuffer.clearFlag(BUFFER_FLAG_END_OF_STREAM)
//        outputBuffer.addFlag(BUFFER_FLAG_KEY_FRAME) // Mark as a Key Frame
//
//        if (result == 0) {
//            outputBuffer.data!!.order(java.nio.ByteOrder.nativeOrder())
//
//        // 1. Read the 64-bit timestamp we saved at byte offset 4
//        val hardwarePts = outputBuffer.data!!.getLong(4)
//
//        // 2. 🚀 Tell ExoPlayer exactly when this frame should be painted
//        outputBuffer.timeUs = hardwarePts
//
//         return null // Success
//        }
//
//        return null
//    }
//
//    override fun release() {
//        super.release()
//        nativeDecoder.release()
//    }

    override fun decode(
        inputBuffer: DecoderInputBuffer,
        outputBuffer: VideoDecoderOutputBuffer,
        reset: Boolean
    ): DecoderException? {

        val width = format.width
        val height = format.height

        // Required to force ExoPlayer to allocate the ByteBuffer
        outputBuffer.initForYuvFrame(
            width, height, width, width / 2,
            VideoDecoderOutputBuffer.COLORSPACE_UNKNOWN
        )

        // 1. RESTORED: ExoPlayer REQUIRES this to allocate the buffer and set dimensions!
        outputBuffer.initForYuvFrame(
            width,
            height,
            width,
            width / 2,
            VideoDecoderOutputBuffer.COLORSPACE_UNKNOWN
        )

        android.util.Log.d("JNI_DEBUG", "KOTLIN DECODE TRIGGERED")

        val inputData = inputBuffer.data
        val outputData = outputBuffer.data
        if (inputData == null || outputData == null) {
            return null // This is where it was silently dying!
        }

        val inputSize = inputData.limit()
        val timeUs = inputBuffer.timeUs

        // 2. Call C++
        val result = nativeDecoder.decode(
            inputSize = inputSize,
            timeUs = timeUs,
            flags = 0,
            inputData = inputData,
            outputBuffer = outputBuffer,
            capacity = outputData.capacity().toLong()
        )

        // 3. Hardware is buffering
        if (result < 0) {
            // Safe to ignore deprecation. Tells ExoPlayer to drop the frame but keep the clock alive.
            @Suppress("DEPRECATION")
            outputBuffer.addFlag(androidx.media3.common.C.BUFFER_FLAG_DECODE_ONLY)
            return null
        }

        // 4. Hardware Success! Pass the ExoPlayer timestamp forward to unblock the clock
        outputBuffer.mode = currentOutputMode

        // 1. Ensure the bytes are read in the correct processor order
        outputBuffer.data!!.order(java.nio.ByteOrder.nativeOrder())

        // 2. 🚀 Read the 64-bit timestamp from byte offset 8
        val hardwarePts = outputBuffer.data!!.getLong(8)

        // 3. Assign the true hardware timestamp to unblock the ExoPlayer clock!
        outputBuffer.timeUs = hardwarePts    // outputBuffer.timeUs = timeUs -----> previous time-stamps


        // Clear EOS flag, but DO NOT add BUFFER_FLAG_KEY_FRAME here
        outputBuffer.clearFlag(BUFFER_FLAG_END_OF_STREAM)


        return null
    }

    override fun release() {
        super.release()
        nativeDecoder.release()
    }

}