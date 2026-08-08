package edu.review.moviesappreview.data.repository.remote

import android.util.Log
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.domain.remote.IMoviesRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepository @Inject constructor (
    private val iMoviesRepository: IMoviesRepository
) {
    suspend fun getMovies(
        endPoint: String,
        apiKey: String,
        page: Int
    ): Response<Movies> {
        Log.d("endPoint_repo", "endPoint_repo: $endPoint")
        return iMoviesRepository
            .getMovies(endPoint, apiKey, page)
    }
}

