package edu.review.moviesappreview.presentation.screen.camerascream.exonative

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.video.VideoRendererEventListener
import java.util.ArrayList

@UnstableApi
class MyRenderersFactory(context: Context) : DefaultRenderersFactory(context) {
    @OptIn(UnstableApi::class)
    override fun buildVideoRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        eventHandler: android.os.Handler,
        eventListener: VideoRendererEventListener,
        allowedVideoJoiningTimeMs: Long,
        out: ArrayList<Renderer>
    ) {
        // Add your custom C++ backed renderer to the list of available renderers
        out.add(
            MyCustomVideoRenderer(eventHandler, eventListener)
        )

        // Call super so ExoPlayer still adds standard hardware renderers as a fallback
        super.buildVideoRenderers(
            context, extensionRendererMode, mediaCodecSelector,
            enableDecoderFallback, eventHandler, eventListener,
            allowedVideoJoiningTimeMs, out
        )
    }
}
