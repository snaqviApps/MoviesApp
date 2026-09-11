package edu.review.moviesappreview.presentation.screen.cameraresults

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
import edu.review.moviesappreview.presentation.screen.cameraresults.exonative.MyRenderersFactory

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
    //    val exoPlayer: ExoPlayer = rememberSaveable is WRONG, due to heavy-duty ExoPlayer instance data
     *
     */

        // Moving ExoPlayer instantiation from 'remember{}' block to 'mutableStateOf()',
        // to fix the Issue of returning to ON_RESUME with exoPlayer.play() call on a
        // 'dead / released', After User had pressed the 'Home' button. That made the
        // exoPlayer.release() call, but did not call the .onDispose {}, as

       // 🌟 State to track our player instance dynamically
       var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

       // Helper function to build a fresh, un-released ExoPlayer instance
       fun initPlayer() : ExoPlayer {
           val myRenderersFactory = MyRenderersFactory(context)
           val mediaItem = MediaItem.fromUri(videoUrl)
           return ExoPlayer.Builder(context,
               myRenderersFactory)
               .build()
               .apply {
                   setMediaItem(mediaItem)
                   seekTo(playHeadPosition)
                   prepare()
                   playWhenReady = playWhenReadyState
               }
       }

    // Initialize on first composition if null
    if(exoPlayer == null) {
        exoPlayer = initPlayer()
    }



    // 2. Handle backgrounding/foregrounding (Crucial for hardware/software codecs)
    DisposableEffect(lifecycleOwner, exoPlayer)
    {
        val observer = LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer?.let { player ->
                     player.pause()
                        playWhenReadyState = player.playWhenReady
                        playHeadPosition = player.currentPosition
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    // 🚀 HOME BUTTON PROTECTION: Fully release hardware resources
                    // when app is moved to background via Home button.
                    exoPlayer?.release()
                    exoPlayer = null
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Resume video when app returns to foreground
                    if(exoPlayer == null) {
                        exoPlayer = initPlayer()
                    } else {
                        exoPlayer?.playWhenReady = true
                        exoPlayer?.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.let { player ->
                playHeadPosition = player.currentPosition   // Save the exact time
                player.release()
            }
            exoPlayer = null
        }
    }

    // 3. Render PlayerView using AndroidView
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer

                useController = true // Displays standard play/pause/timeline controls

                // 🚀 ADD THIS: Use TextureView for software decoders
                // (Requires @OptIn(UnstableApi::class))
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                setShutterBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        update = { playerView ->
            // 🚀 CRITICAL: Update the player reference inside AndroidView when reinitialized!
            if(playerView.player != exoPlayer) {
                playerView.player = exoPlayer
            }
        },
        modifier = modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(16f / 12f)
    )

}
