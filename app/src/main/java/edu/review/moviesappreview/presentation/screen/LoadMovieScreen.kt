package edu.review.moviesappreview.presentation.screen

import androidx.compose.animation.EnterTransition.Companion.None
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import edu.review.moviesappreview.presentation.viewmodel.MoviesViewModel

@Composable
fun LoadMovieScreen(
    modifier: Modifier = Modifier,
    onMovieView: @Composable (isPopular: Boolean) -> Unit,
    viewModel: MoviesViewModel,
    innerPadding: PaddingValues,
    endPoint: String
) {
    Scaffold(
        topBar = {
            Text(
                modifier = Modifier
                    .padding(
                        start = 32.dp,
                        top = innerPadding.calculateTopPadding() + 24.dp
                    ),
                text = endPoint.replace("_", " ").split(" ")
                    .joinToString(" ") { ch -> ch.replaceFirstChar { it.uppercase() } },
                textAlign = TextAlign.Center,
                style = TextStyle(
//                    textAlign = TextAlign.End,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier.padding(scaffoldPadding),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
//            contentAlignment = Alignment.CenterStart

                ) {
                    if (viewModel.showMovies) {
                        onMovieView(viewModel.showMovies)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 18.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Spacer(modifier = Modifier.size(2.dp))
                    Button(
                        onClick = { viewModel.enableMoviesDataFetching("top_rated") },
                        modifier = Modifier.weight(0.33f)

                    ) {
                        Text(text = "Top Rated")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
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
                        .padding(start = 6.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.enableMoviesDataFetching("popular") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Popular")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end=16.dp, bottom = 8.dp),
                ) {
                    Button(
                        onClick = { viewModel.enableExoPlayerDefaults() },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Launch ExoPlayer")
                    }
                }
            }

        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoadMovieScreenPreview() {
    LoadMovieScreen(
        onMovieView = { Text("Popular") },
        viewModel = hiltViewModel(),
        innerPadding = None as PaddingValues,
        endPoint = "Dummy End Point"
    )
}