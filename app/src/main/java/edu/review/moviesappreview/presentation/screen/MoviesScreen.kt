package edu.review.moviesappreview.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.review.moviesappreview.presentation.MoviesUIState
import edu.review.moviesappreview.presentation.viewmodel.MoviesViewModel
import edu.review.moviesappreview.presentation.screen.cameraresults.SecurityCameraScreen
import edu.review.moviesappreview.presentation.screen.cameraresults.VideoPlayerDialog
import edu.review.moviesappreview.util.testStreamUrl

@Composable
fun MoviesScreen(
    modifier: Modifier = Modifier,
    viewModel: MoviesViewModel = hiltViewModel()
) {
    val moviesState by viewModel.moviesState.collectAsStateWithLifecycle()
    LoadMovieScreen(
        modifier = modifier.fillMaxSize(),
        onMovieView = {
            when (val mState = moviesState) {
                is MoviesUIState.Success -> {
                    MovieCard(
                        // FIX 2: Use a clean, fresh Modifier here so it fills the Box perfectly!
                        modifier = Modifier.fillMaxSize(),
                        endPoint = viewModel.currentEndPoint,
                        mState = mState
                    )

                    // ExoPlayer implementation for Security Camera
                    SecurityCameraScreen(
                        modifier = Modifier.fillMaxSize(),
                        streamUrl = testStreamUrl,
                        onSecurityStream = { isSecurityCamera, steamUrl ->
                            if (isSecurityCamera) {
                                VideoPlayerDialog(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp),
                                    steamUrl = steamUrl,
                                    onDismissRequest = {
                                        viewModel.enableExoPlayerDefaults()
                                    },
                                )
                            }
                        }
                    )
                }
                is MoviesUIState.Error -> {
                    Text(
                        text = mState.message,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is MoviesUIState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(
                                    width = 104.dp,
                                    height = 84.dp
                                ),
                            strokeWidth = 4.dp
                        )
                    }
                }
            }
        },
        viewModel = viewModel
    )
}


@Preview(showBackground = true)
@Composable
fun MoviesScreenPreview() {
    MoviesScreen()
}