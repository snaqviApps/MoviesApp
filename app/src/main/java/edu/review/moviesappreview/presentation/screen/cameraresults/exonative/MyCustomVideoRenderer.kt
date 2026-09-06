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
        return if (MimeTypes.VIDEO_H265.equals(mimeType, ignoreCase = true)) {
            RendererCapabilities.create(C.FORMAT_HANDLED)
        } else {
            RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE)
        }
    }

    override fun createDecoder(
        format: Format,
        cryptoConfig: CryptoConfig?
    ): MyCustomDecoder {
        return MyCustomDecoder(format)
    }

    override fun renderOutputBufferToSurface(
        outputBuffer: VideoDecoderOutputBuffer,
        surface: Surface
    ) {
        TODO("Not yet implemented")
    }

    override fun setDecoderOutputMode(outputMode: Int) {
        TODO("Not yet implemented")
    }


}