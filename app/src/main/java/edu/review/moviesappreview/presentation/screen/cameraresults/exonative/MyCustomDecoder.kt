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

/**
 * @param mimeType 👈 Add the 3rd parameter here,
 * to configure itself dynamically for H.264, HEVC, VP9, or any other video codec.
 */
@OptIn(UnstableApi::class)
class MyCustomDecoder(
    private val format: Format,
    private val surface: Surface?,
    private val mimeType: String
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
        nativeDecoder.init(format.width, format.height, surface, mimeType)
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
        reset: Boolean  // to be used for forwarding / jumping to 15 seconds control
    ): DecoderException? {

        // 1. 🚀 ExoPlayer is telling us a Seek just happened! Dump the C++ hardware memory.
        if (reset) {
            nativeDecoder.flush()
        }

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

        // try to avoid crash
        // "ava.util.ArrayDeque.grow with the tag com.amazon.device.crashmanager.AppFileArtifactSource"
        // android.util.Log.d("JNI_DEBUG", "KOTLIN DECODE TRIGGERED")

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

        // (Drop Frame) Checkpoint 1: Hardware is buffering (no output frame produced)
        if (result < 0) {
            outputBuffer.data?.order(java.nio.ByteOrder.nativeOrder())
            outputBuffer.data?.putInt(0, -1) // Clear stale index from recycled buffer

            // Safe to ignore deprecation. Tells ExoPlayer to drop the frame but keep the clock alive.
            @Suppress("DEPRECATION")
            outputBuffer.addFlag(androidx.media3.common.C.BUFFER_FLAG_DECODE_ONLY)
            return null
        }

        // 4. Hardware Success! Pass the ExoPlayer timestamp forward to unblock the clock
//        outputBuffer.mode = currentOutputMode

        // 🚀 Force ExoPlayer to treat this as a hardware surface ticket!
        outputBuffer.mode = androidx.media3.common.C.VIDEO_OUTPUT_MODE_SURFACE_YUV


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

    // (Drop Frame) Checkpoint 2: Frame returned to pool without being rendered
    override fun releaseOutputBuffer(outputBuffer: VideoDecoderOutputBuffer) {
        val data = outputBuffer.data
        if (data != null && data.limit() >= 4) {
            data.order(java.nio.ByteOrder.nativeOrder())
            val outIdx = data.getInt(0)

            // 🚀 If the index is still >= 0, ExoPlayer dropped the frame!
            // We MUST return the ticket to C++ so the hardware doesn't starve.
            if (outIdx >= 0) {
                nativeDecoder.dropFrame(outIdx) // Calls C++ AMediaCodec_releaseOutputBuffer(..., false)
                data.putInt(0, -1);     // wipe it clean, released
            }

        }

        // Let ExoPlayer recycle the Kotlin wrapper
        super.releaseOutputBuffer(outputBuffer)
    }

    override fun release() {
        super.release()         // 1. Let ExoPlayer clear its Java memory pools
        nativeDecoder.release() // 2. Tell C++ to destroy the hardware chip
    }

}