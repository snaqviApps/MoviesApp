package edu.review.moviesappreview.data.repository

import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.data.repository.remote.MovieRemoteSource
import edu.review.moviesappreview.domain.repository.MoviesRepository
import edu.review.moviesappreview.util.toResult
import javax.inject.Inject

class DefaultMoviesRepository @Inject constructor (
    private val movieRemoteSource: MovieRemoteSource
) : MoviesRepository<Movies> {
    override suspend fun getMovies(
        defaultCategory: String,
        apiKey: String,
        page: Int
    ): Result<Movies> {
        return movieRemoteSource.getMovies(
            endPoint = defaultCategory,
            apiKey = apiKey,
            page = page
        ).toResult()
    }

}