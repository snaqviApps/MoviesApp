package edu.review.moviesappreview.presentation.screen

import androidx.compose.foundation.background
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
import edu.review.moviesappreview.util.imageUrl


@Composable
fun MovieCard(
    modifier: Modifier = Modifier,
    mState: MoviesUIState.Success,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp, start = 6.dp, end = 6.dp),
        colors = cardColors(containerColor = Color.Transparent)
    ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(4.dp),
                ) {
                    items(mState.moviesList) { result ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()

                                // FIX 2 & 3: Clip first so the image inherits rounded corners, then apply background
                                .clip(MaterialTheme.shapes.medium)
                                .background(color = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.5f))
                        ) {
                            result.title?.let {
                                Text(
                                    modifier = Modifier
                                        .padding(start = 6.dp, top = 4.dp, bottom = 6.dp),
                                    text = it,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.primaryFixed.copy(
                                            alpha = 2.5f,
                                            red = 4.2f,
                                            blue = 1.5f,
                                            green = 1.5f
                                        )
                                    )
                                )
                            }
                            result.backdropPath?.let { path ->
                                AsyncImage(
                                    model = "$imageUrl$path",
                                    contentDescription = "Thumbnail",
                                    placeholder = painterResource(R.drawable.outline_movie_24),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(2.dp),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            result.originalLanguage?.let { text ->
                                Text(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    text = "Language: $text",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            result.overview?.let { text ->
                                Text(
                                    modifier = Modifier.padding(horizontal = 4.dp,
                                        vertical = 3.dp
                                    ),
                                    text = text,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.primaryFixed.copy(
                                            alpha = 2.5f,
                                            red = 4.2f,
                                            blue = 1.5f,
                                            green = 1.5f
                                        )
                                    ),
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                }
            }

}

@Preview(showBackground = true)
@Composable
fun PreviewMovieCard() {
    MovieCard(
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
                    overview = "Overview, Dummy Data, color composition",
                    popularity = 0.0,
                    posterPath = null,
                    releaseDate = null,
                    softcore = false,
                    video = false,
                    voteAverage = 0.0,
                    voteCount = 0
                )
            )
        )
    )
}
