package edu.review.moviesappreview.data.repository.remote

import android.util.Log
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.domain.repository.remote.IMoviesRepository
import edu.review.moviesappreview.domain.repository.remote.MoviesApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepository @Inject constructor (
    private val moviesApiService: MoviesApiService
) : IMoviesRepository {
    override suspend fun getMovies(
        defaultEndPoint: String,
        apiKey: String,
        page: Int
    ): Response<Movies> {
        Log.d("endPoint_repo", "endPoint_repo: $defaultEndPoint")
        return moviesApiService.getMovies(defaultEndPoint, apiKey, page)
    }
}

