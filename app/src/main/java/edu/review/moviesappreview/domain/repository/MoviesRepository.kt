package edu.review.moviesappreview.domain.repository

import edu.review.moviesappreview.data.movies.Movies


/**
 * Domain layer repository
 * This is the Business contract. It is the Single Source of Truth for the Use Cases.
 * Defines the app's business contract for fetching movie data, hiding network details
 * from Use Cases.
 * it just promises to provide Movies.
 *
 */
interface MoviesRepository<out T : Movies> : DataSource<T> {
    suspend fun getMovies(
        defaultCategory: String,
        apiKey: String,
        page: Int
    ): Result<Movies>
}