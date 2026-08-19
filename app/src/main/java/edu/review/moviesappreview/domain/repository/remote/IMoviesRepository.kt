package edu.review.moviesappreview.domain.repository.remote

import edu.review.moviesappreview.data.movies.Movies
import retrofit2.Response

/**
 * Domain layer repository
 * Defines the app's business contract for fetching movie data, hiding network details from Use Cases.
 */
interface IMoviesRepository {
    suspend fun getMovies(
        defaultEndPoint: String,
        apiKey: String,
        page: Int
    ): Response<Movies>
}