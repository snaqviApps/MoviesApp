package edu.review.moviesappreview.data.mapper

import edu.review.moviesappreview.data.movies.Result as MovieDto
import edu.review.moviesappreview.domain.model.Movie

fun MovieDto.toDomain() : Movie {
    return Movie(
        id = id,
        title = title ?: originalTitle ?: "Unknown",
        originalLanguage = originalLanguage,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
    )
}