package edu.review.moviesappreview.presentation.screen

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.review.moviesappreview.data.remote.MoviesRepository

import edu.review.moviesappreview.presentation.MovieUIState
import edu.review.moviesappreview.presentation.MoviesViewModel

@Composable
fun MoviesScreen(
    modifier: Modifier = Modifier
) {
    // 1. Grab the application context safely inside the Composable body first
    val appContext: Application = LocalContext.current.applicationContext as Application

    val viewModel: MoviesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MoviesViewModel(
                    application = appContext,
                    moviesRepository = MoviesRepository()
                ) as T
            }
        }
    )

    val moviesState by viewModel.moviesState.collectAsStateWithLifecycle()

    LoadMovieScreen(
        modifier = modifier.fillMaxSize(),
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