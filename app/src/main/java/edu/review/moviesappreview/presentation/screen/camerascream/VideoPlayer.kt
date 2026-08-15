package edu.review.moviesappreview.presentation.screen.camerascream

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current

    // 1. Remember ExoPlayer instance tied to Context
    // 2. Add videoUrl as a key, so the player updates when the URL changes
    val exoPlayer = remember(
        context,
        videoUrl
    ) {

        // 2. Configure RTSP Media Source to Force RTP over TCP (Interleaved)
        val mediaItem = MediaItem.fromUri(videoUrl)
        ExoPlayer.Builder(context).build().apply {
//            setMediaSource(rtspMediaSource)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 3. Render PlayerView using AndroidView
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true // Displays standard play/pause/timeline controls
            }
        },
        modifier = modifier
            .fillMaxWidth(0.8f)
//            .height(82.dp)
            .aspectRatio(16f / 12f)
    )

}
