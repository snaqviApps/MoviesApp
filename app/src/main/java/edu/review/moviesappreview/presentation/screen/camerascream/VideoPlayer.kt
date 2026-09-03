package edu.review.moviesappreview.presentation.screen.camerascream

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var playHeadPosition by rememberSaveable { mutableLongStateOf(0L) }
    var playWhenReadyState by rememberSaveable { mutableStateOf(true) }


    // 1. Remember ExoPlayer instance tied to Context
    // 2. Add videoUrl as a key, so the player updates when the URL changes

    /**
     * val exoPlayer: ExoPlayer = rememberSaveable is WRONG, due to heavy-duty ExoPlayer instance data
     *
     */
    val exoPlayer: ExoPlayer = remember (context, videoUrl)
    {
        // 2. Configure RTSP Media Source to Force RTP over TCP (Interleaved)
        val mediaItem = MediaItem.fromUri(videoUrl)
        ExoPlayer.Builder(
            context
        ).build().apply {
            setMediaItem(mediaItem)
            seekTo(playHeadPosition)        //   Resume from the cached time stamp
            prepare()
            playWhenReady = playWhenReadyState
        }
    }

    // 2. Handle backgrounding/foregrounding (Crucial for hardware/software codecs)
    DisposableEffect(lifecycleOwner, exoPlayer)
    {
        val observer = LifecycleEventObserver {_, event ->
            when(event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Pause video and save state when app goes to background
                    exoPlayer.pause()
                    playWhenReadyState = exoPlayer.playWhenReady
                    playHeadPosition = exoPlayer.currentPosition
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Resume video when app returns to foreground
                    exoPlayer.playWhenReady = true
                    exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playHeadPosition = exoPlayer.currentPosition   // Save the exact time
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
            .aspectRatio(16f / 12f)
    )

}
