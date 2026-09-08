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

    @C.FormatSupport
    override fun supportsFormat(format: Format): Int {
        val mimeType = format.sampleMimeType

        // Example for H.265 / HEVC. Change to match your C++ codec's supported format.
//        return if (MimeTypes.VIDEO_H265.equals(mimeType, ignoreCase = true)) {
//            RendererCapabilities.create(C.FORMAT_HANDLED)
//        }
//        else {
//            RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE)
//        }
        // Force to use C++ codec
        return RendererCapabilities.create(C.FORMAT_HANDLED)

    }

    override fun createDecoder(
        format: Format,
        cryptoConfig: CryptoConfig?
    ): MyCustomDecoder {

        //2. Instantiate and store the decoder instance
        val newDecoder = MyCustomDecoder(format)
        this.decoder = newDecoder
        return newDecoder
//        return MyCustomDecoder(format)
    }

    override fun renderOutputBufferToSurface(
        outputBuffer: VideoDecoderOutputBuffer,
        surface: Surface
    ) {

        // 🚀 ADD THIS LOG:
        android.util.Log.d("JNI_DEBUG", "KOTLIN: Handing off buffer to C++ for painting")

        // 🚀 THE HANDOFF: Tell C++ to paint the buffer onto the surface
        decoder?.nativeDecoder?.render(outputBuffer, surface)

        // 🚀 THE SIGNAL: Tell ExoPlayer a frame was drawn!
        onProcessedOutputBuffer(outputBuffer.timeUs)
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