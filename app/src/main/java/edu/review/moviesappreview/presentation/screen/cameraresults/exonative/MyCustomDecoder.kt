package edu.review.moviesappreview.presentation.screen.cameraresults.exonative

import androidx.annotation.OptIn
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.DecoderException
import androidx.media3.decoder.DecoderInputBuffer
import androidx.media3.decoder.SimpleDecoder
import androidx.media3.decoder.VideoDecoderOutputBuffer


@OptIn(UnstableApi::class)
class MyCustomDecoder(
    private val format: Format
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
        nativeDecoder.init(format.width, format.height)
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

    override fun decode(
        inputBuffer: DecoderInputBuffer,
        outputBuffer: VideoDecoderOutputBuffer,
        reset: Boolean
    ): DecoderException? {

        // 1. Manually ensure the output buffer has a ByteBuffer allocated
        // We calculate size: Width * Height * 4 (for RGBA_8888)
        val width = format.width
        val height = format.height

        // This function tells ExoPlayer to prepare the buffer for the given size
//        outputBuffer.init(inputBuffer.timeUs, VideoDecoderOutputBuffer.COLORSPACE_BT2020, null)
        outputBuffer.initForYuvFrame(width, height, width, width / 2,  VideoDecoderOutputBuffer.COLORSPACE_BT2020)

        android.util.Log.d("JNI_DEBUG", "KOTLIN DECODE TRIGGERED")


        // 3. Delegate the actual math to the C++ engine

        // 3-1. Safety Check: If data is not ready, just skip this frame!
        val inputData = inputBuffer.data
        val outputData = outputBuffer.data

        // 🚀 UPDATE THIS LOG:
        android.util.Log.d("JNI_DEBUG", "KOTLIN DECODE: input=${inputData != null}, output=${outputData != null}")


        if (inputData == null || outputData == null) {
            // We return null (no exception), but don't call C++.
            // This tells ExoPlayer: "Nothing to see here, moving on."
            return null
        }

        val result = nativeDecoder.decode(inputData, outputBuffer = outputBuffer)

        if (result < 0) {
            // 🚀 OPTIONAL: Log this instead of throwing an exception
            android.util.Log.e("JNI_DEBUG", "C++ Decode failed with status: $result")
            return DecoderException("Native decoding failed")
        }

        // 4. Pass the timing information back to ExoPlayer
        outputBuffer.timeUs = inputBuffer.timeUs
//        outputBuffer.mode = androidx.media3.common.C.VIDEO_OUTPUT_MODE_SURFACE_YUV // 👈 ADD THIS
        outputBuffer.mode = currentOutputMode

        // Clear any error flags
//        outputBuffer.clearFlag(androidx.media3.common.C.BUFFER_FLAG_DECODE_ONLY) -----> Flag deprecated in Java
        outputBuffer.clearFlag(androidx.media3.common.C.BUFFER_FLAG_END_OF_STREAM)

        outputBuffer.addFlag(androidx.media3.common.C.BUFFER_FLAG_KEY_FRAME) // Mark as a Key Frame

        return null
    }

    override fun release() {
        super.release()
        nativeDecoder.release()
    }
}