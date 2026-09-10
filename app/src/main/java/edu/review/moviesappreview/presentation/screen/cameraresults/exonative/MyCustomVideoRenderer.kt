package edu.review.moviesappreview.presentation.screen.cameraresults.exonative

import android.view.Surface
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.CryptoConfig
import androidx.media3.decoder.VideoDecoderOutputBuffer
import androidx.media3.exoplayer.video.DecoderVideoRenderer
import androidx.media3.exoplayer.video.VideoRendererEventListener
import android.os.Handler
import androidx.media3.common.C
import androidx.media3.common.MimeTypes

// Keep your other imports (C, Format, MimeTypes, etc.)
import androidx.media3.exoplayer.RendererCapabilities

@UnstableApi
class MyCustomVideoRenderer(
    eventHandler: Handler? = null,
    eventListener: VideoRendererEventListener? = null
) : DecoderVideoRenderer(
    DEFAULT_ALLOWED_JOINING_TIME_MS,
    eventHandler as android.os.Handler?,
    eventListener,
    MAX_DROPPED_FRAMES_TO_NOTIFY
) {
    companion object {
        private const val DEFAULT_ALLOWED_JOINING_TIME_MS = 5000L
        private const val MAX_DROPPED_FRAMES_TO_NOTIFY = 50
    }

    // 1. Create a variable to hold the decoder instance
    private var decoder: MyCustomDecoder? = null

    /**
     * Identifies this renderer in ExoPlayer logs and track selection.
     */
    override fun getName(): String {
        return "MyCustomCppVideoRenderer"
    }

    // Add this property to your renderer class
    private var currentSurface: Surface? = null

    override fun handleMessage(messageType: Int, message: Any?) {
        // 👈 Use the Renderer class constant here
        if (messageType == MSG_SET_VIDEO_OUTPUT) {
            currentSurface = message as? Surface
        }
        super.handleMessage(messageType, message)
    }

    @C.FormatSupport
    override fun supportsFormat(format: Format): Int {
        val mimeType = format.sampleMimeType

        // Example for H.264 / HEVC. Change to match your C++ codec's supported format.
        /**
         * only support H264 for now
         */
        return if (MimeTypes.VIDEO_H264.equals(mimeType, ignoreCase = true)) {
            RendererCapabilities.create(C.FORMAT_HANDLED)
        }
        else {
            RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE)
        }
    }

    override fun createDecoder(
        format: Format,
        cryptoConfig: CryptoConfig?
    ): MyCustomDecoder {

        //2. Instantiate and store the decoder instance
        val newDecoder = MyCustomDecoder(format, currentSurface)
        this.decoder = newDecoder
        return newDecoder
//        return MyCustomDecoder(format)
    }

    override fun renderOutputBufferToSurface(
        outputBuffer: VideoDecoderOutputBuffer,
        surface: Surface
    ) {

        // 1. Read the hardware index (the claim ticket) we saved in C++
        outputBuffer.data!!.order(java.nio.ByteOrder.nativeOrder())
        val hardwarePts = outputBuffer.data!!.getLong(8)
        val outIdx = outputBuffer.data!!.getInt(0)

        // ADD THIS LOG:
        android.util.Log.d("JNI_DEBUG", "KOTLIN READ - Index: $outIdx, TimeUs: $hardwarePts")

        // 🚀 ADD THIS LOG:
        android.util.Log.d("JNI_DEBUG", "KOTLIN: Handing off buffer to C++ for painting")

        // 🚀 THE HANDOFF: Tell C++ to paint the buffer onto the surface via GPU
        // 2. Tell C++ to release and paint that specific hardware buffer
        decoder?.nativeDecoder?.render(outIdx)

        // 🚀 THE SIGNAL: Tell ExoPlayer a frame was drawn!
        // 3. 🚀 Notify ExoPlayer the frame was drawn (This unblocks your Audio clock!)
        onProcessedOutputBuffer(outputBuffer.timeUs)

        // 3. Tell the master clock the video is visible (Unblocks Audio!)
//        maybeNotifyRenderedFirstFrame() -----> private in DecoderVideoRenderer

        // 3. 🚀 THE MISSING LINK: Return the empty buffer to the pool!
        // This prevents the system from stalling after exactly 8 frames.
        outputBuffer.release()
    }

//    override fun setDecoderOutputMode(outputMode: Int) {
////        TODO("Not yet implemented")
//    }

    // 1. Add a variable to track the mode
    private var outputMode: Int = C.VIDEO_OUTPUT_MODE_NONE

    override fun setDecoderOutputMode(outputMode: Int) {
        // 2. Catch the mode ExoPlayer wants
        this.outputMode = outputMode

        // 3. Pass it down to your custom decoder
        decoder?.setOutputMode(outputMode)
    }

    override fun shouldForceRenderOutputBuffer(
        earlyUs: Long,
        elapsedSinceLastRenderUs: Long
    ): Boolean {
        return super.shouldForceRenderOutputBuffer(earlyUs, elapsedSinceLastRenderUs)
    }


}