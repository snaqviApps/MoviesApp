package edu.review.moviesappreview.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.review.moviesappreview.presentation.MovieUIState
import edu.review.moviesappreview.presentation.MovieViewModel

@Composable
fun MoviesScreen(
    // FIX 1: Pass the screen-level modifier to the root component where it belongs
    modifier: Modifier = Modifier,
    viewModel: MovieViewModel = viewModel()
) {
    val moviesState by viewModel.moviesState.collectAsStateWithLifecycle()

    LoadMovieScreen(
        modifier = modifier.fillMaxSize(),
        state = moviesState,
        onMovieView = {
            when (val mState = moviesState) {
                is MovieUIState.Success -> {
                    MovieCard(
                        // FIX 2: Use a clean, fresh Modifier here so it fills the Box perfectly!
                        modifier = Modifier.fillMaxSize(),
                        endPoint = viewModel.currentEndPoint,
                        mState = mState
                    )
                }
                is MovieUIState.Error -> {
                    Text(
                        text = mState.message,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is MovieUIState.Loading -> {
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