package edu.review.moviesappreview.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import edu.review.moviesappreview.R
import edu.review.moviesappreview.data.movies.Result
import edu.review.moviesappreview.presentation.MoviesUIState
import androidx.compose.ui.text.font.FontFamily


@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    endPoint: String,
    mState: MoviesUIState.Success
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp, start = 6.dp, end = 6.dp),
        colors = cardColors(containerColor = Color.Transparent)
    ) {
        Text(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 14.dp)
                .align(Alignment.CenterHorizontally),
            text = endPoint.replace("_", " ").split(" ")
                .joinToString(" ") { ch -> ch.replaceFirstChar { it.uppercase() } },
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(4.dp),
                    // FIX 2: This replaces manual column spacers! It builds a perfect 16dp gap BETWEEN items.d
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(mState.moviesList) { result ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // FIX 2 & 3: Clip first so the image inherits rounded corners, then apply background
                        .clip(MaterialTheme.shapes.medium)
                        .background(color = MaterialTheme.colorScheme.surface)

                ) {
                    result.backdropPath?.let { path ->
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w780$path",
                            contentDescription = "Thumbnail",
                            placeholder = painterResource(R.drawable.outline_movie_24),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp),

                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    result.title?.let {
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            text = it,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    result.originalLanguage?.let { text ->
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            text = "Language: $text",
                            style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    result.overview?.let { text ->
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            text = text,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMovieCard() {
    MovieCard(
        endPoint = "popular",
        mState = MoviesUIState.Success(
            moviesList = listOf(
                Result(
                    adult = false,
                    backdropPath = "/sample.jpg",
                    genreIds = emptyList(),
                    id = 1,
                    title = "Sample Movie",
                    originalLanguage = "en",
                    originalTitle = "Sample Movie",
                    overview = "Overview",
                    popularity = 0.0,
                    posterPath = null,
                    releaseDate = null,
                    softcore = false,
                    video = false,
                    voteAverage = 0.0,
                    voteCount = 0
                )
            )
        ),
    )
}
