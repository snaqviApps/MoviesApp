package edu.review.moviesappreview.presentation.screen.cameraresults.exonative

import androidx.annotation.OptIn
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.DecoderException
import androidx.media3.decoder.DecoderInputBuffer
import androidx.media3.decoder.SimpleDecoder
import androidx.media3.decoder.VideoDecoderOutputBuffer

@OptIn(UnstableApi::class)
class MyCustomDecoder(format: Format) : SimpleDecoder<DecoderInputBuffer, VideoDecoderOutputBuffer, DecoderException>(
    arrayOfNulls(NUM_INPUT_BUFFERS),
    arrayOfNulls(NUM_OUTPUT_BUFFERS)
) {
    companion object {
        private const val NUM_INPUT_BUFFERS = 8
        private const val NUM_OUTPUT_BUFFERS = 8
    }

    private val nativeDecoder = NativeDecoder()

    init {
        // 1. Load the compiled C++ library
        System.loadLibrary("my_custom_codec")

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
        // 3. Delegate the actual math to the C++ engine
        val result = nativeDecoder.decode(inputBuffer.data, outputBuffer.data)

        if (result < 0) {
            return DecoderException("Native decoding failed")
        }

        // 4. Pass the timing information back to ExoPlayer
        outputBuffer.timeUs = inputBuffer.timeUs
        return null
    }

    override fun release() {
        super.release()
        nativeDecoder.release()
    }
}