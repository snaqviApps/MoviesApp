package edu.review.moviesappreview.domain.model

/**
 * to Map a core principle of Clean Architecture: Separation of Concerns.
 * Instead of domain importing data.movies.Movies (which has GSON network annotations),
 * we create a clean Domain Model
 */
data class Movie(
    val id: Int?,
    val title: String?,
    val originalLanguage : String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
)