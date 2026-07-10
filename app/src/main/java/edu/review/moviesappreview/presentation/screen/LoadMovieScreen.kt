package edu.review.moviesappreview.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.review.moviesappreview.presentation.MovieUIState
import edu.review.moviesappreview.presentation.MovieViewModel

@Composable
fun LoadMovieScreen(
    modifier: Modifier = Modifier,
    state: MovieUIState,
    onMovieView: @Composable (isPopular: Boolean) -> Unit,
    viewModel: MovieViewModel,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box (
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.showMovies) {
                    onMovieView(viewModel.showMovies)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Spacer(modifier = Modifier.size(2.dp))
                Button(
                    onClick = { viewModel.enableMoviesDataFetching("top_rated") },
                    modifier = Modifier.weight(0.33f)
                ) {
                    Text(text = "Top Rated")
                }
                Spacer(modifier = Modifier.size(4.dp))
                Button(
                    onClick = { viewModel.enableMoviesDataFetching("now_playing") },
                    modifier = Modifier.weight(0.33f)
                ) {
                    Text(text = "Now Playing")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.enableMoviesDataFetching("popular") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Popular")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadMovieScreenPreview() {
    LoadMovieScreen(
        state = MovieUIState.Loading,
        onMovieView = { Text("Popular") },
        viewModel = viewModel(),
    )
}