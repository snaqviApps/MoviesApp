package edu.review.moviesappreview.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    viewModel: MoviesViewModel = hiltViewModel(),
    innerPadding: PaddingValues
) {
    val moviesState by viewModel.moviesState.collectAsStateWithLifecycle()
    LoadMovieScreen(
        innerPadding = innerPadding,
        endPoint = viewModel.currentEndPoint,
        modifier = modifier.fillMaxSize(),
        onMovieView = {
            when (val mState = moviesState) {
                is MoviesUIState.Success -> {
                    MovieCard(
                        // FIX 2: Use a clean, fresh Modifier here so it fills the Box perfectly!
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
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
                    Box(
                        modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        Column(
                            modifier
                                .fillMaxWidth()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(modifier = Modifier.fillMaxSize(),
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color = Color.Red,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("Error:\n\n")
                                    }
                                    if (mState.message.contains("api.themoviedb.org")) {
                                        withStyle(
                                            style = SpanStyle(
                                                color = Color.Blue,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append("remote Host is not reachable")
                                        }
                                    } else {
                                        append(mState.message)
                                    }
                                }
                            )
                        }
                    }
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