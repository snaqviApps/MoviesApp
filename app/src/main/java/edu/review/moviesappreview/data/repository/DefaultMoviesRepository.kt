package edu.review.moviesappreview.data.repository

import edu.review.moviesappreview.data.mapper.toDmain
import edu.review.moviesappreview.data.repository.remote.MovieRemoteSource
import edu.review.moviesappreview.domain.data.Movie
import edu.review.moviesappreview.domain.repository.MoviesRepository
import edu.review.moviesappreview.util.toResult
import javax.inject.Inject

class DefaultMoviesRepository @Inject constructor (
    private val movieRemoteSource: MovieRemoteSource
) : MoviesRepository {
    override suspend fun getMovies(
        defaultCategory: String,
        apiKey: String,
        page: Int
//    ): Result<Movies> {
    ): Result<List<Movie>> {
        val networkResult = movieRemoteSource.getMovies(
            endPoint = defaultCategory,
            apiKey = apiKey,
            page = page
        ).toResult()

        // Map DTOs to Domain Models
        return networkResult.map {movies ->
            movies.results.map { it.toDmain() }
        }
    }

}