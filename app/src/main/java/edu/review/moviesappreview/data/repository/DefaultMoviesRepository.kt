package edu.review.moviesappreview.data.repository

import android.util.Log
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.data.repository.remote.MovieRemoteSource
import edu.review.moviesappreview.domain.repository.MoviesRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMoviesRepository @Inject constructor (
    private val movieRemoteSource: MovieRemoteSource
) : MoviesRepository {
    override suspend fun getMovies(
        defaultEndPoint: String,
        apiKey: String,
        page: Int
    ): Response<Movies> {
        Log.d("endPoint_repo", "endPoint_repo: $defaultEndPoint")
        return movieRemoteSource.getMovies(defaultEndPoint, apiKey, page)
    }
}