package edu.review.moviesappreview.data.repository

import edu.review.moviesappreview.data.mapper.toDomain
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.data.repository.remote.MovieRemoteSource
import edu.review.moviesappreview.domain.model.Movie
import edu.review.moviesappreview.domain.repository.MoviesRepository
import edu.review.moviesappreview.util.toResult
import javax.inject.Inject

/**
 * same as 'would be' MoviesRepositoryImpl, as
 * it is implementing
 */
class DefaultMoviesRepository @Inject constructor (
    private val movieRemoteSource: MovieRemoteSource
) : MoviesRepository {
    override suspend fun getMovies (
        defaultCategory: String,
        apiKey: String,
        page: Int
    ): Result<List<Movie>> {
        return try {
            val networkResult: Result<Movies> = movieRemoteSource.getMovies(
                endPoint = defaultCategory,
                apiKey = apiKey,
                page = page
            ).toResult()

            // Map DTOs to Domain Models
            networkResult.map { movies ->
                movies.results.map { it.toDomain() }
            }
        } catch (e: Exception) {
            // 🛡️ Catches UnknownHostException, SocketTimeoutException, etc.
            Result.failure(e)
        }
    }

}


