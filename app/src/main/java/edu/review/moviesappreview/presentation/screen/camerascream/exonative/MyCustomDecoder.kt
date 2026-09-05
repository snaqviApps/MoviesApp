package edu.review.moviesappreview.presentation.screen.camerascream.exonative

import androidx.annotation.OptIn
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.DecoderException
import androidx.media3.decoder.DecoderInputBuffer
import androidx.media3.decoder.SimpleDecoder
import androidx.media3.decoder.VideoDecoderOutputBuffer
import java.nio.ByteBuffer


@OptIn(UnstableApi::class)
class MyCustomDecoder(format: Format) : SimpleDecoder<DecoderInputBuffer,
        VideoDecoderOutputBuffer, DecoderException>(
    arrayOfNulls(NUM_INPUT_BUFFERS),
    arrayOfNulls(NUM_OUTPUT_BUFFERS)
) {
    companion object {
        private const val NUM_INPUT_BUFFERS = 8
        private const val NUM_OUTPUT_BUFFERS = 8
    }

    private val nativeDecoder = NativeDecoder() // Your JNI class

    // Load the compiled C++ library (.so file)
    init {
        System.loadLibrary("my_custom_codec")
        // You can extract important codec details from 'format'
        // and pass them to your C++ engine during initialization.
        val width = format.width
        val height = format.height
        val csd = format.initializationData // Codec Specific Data (if any)

        // Pass these details down if your C++ code needs them to configure itself
        nativeDecoder.init(width, height)
    }

    private external fun initNativeDecoder(): Long
    private external fun decodeNative(
        decoderPtr: Long,
        inputData: ByteBuffer,
        outputData: ByteBuffer
    ): Int

    private external fun releaseNative(decoderPtr: Long)

    override fun decode(
        inputBuffer: DecoderInputBuffer,
        outputBuffer: VideoDecoderOutputBuffer,
        reset: Boolean
    ): DecoderException? {
        // Pass data to C++ via JNI
        val result = nativeDecoder.decode(inputBuffer.data, outputBuffer.data)

        if (result < 0) {
            return DecoderException("Failed to decode frame")
        }

        // Populate output buffer metadata (timestamps, etc.)
        outputBuffer.timeUs = inputBuffer.timeUs
        return null
    }

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun release() {
        super.release()
        nativeDecoder.release()
    }

    override fun createInputBuffer(): DecoderInputBuffer {
        TODO("Not yet implemented")
    }

    override fun createOutputBuffer(): VideoDecoderOutputBuffer {
        TODO("Not yet implemented")
    }

    override fun createUnexpectedDecodeException(error: Throwable): DecoderException {
        TODO("Not yet implemented")
    }
}