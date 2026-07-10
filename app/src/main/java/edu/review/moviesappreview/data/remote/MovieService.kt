package edu.review.moviesappreview.data.remote

import android.util.Log
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.domain.remote.MoviesService
import edu.review.moviesappreview.domain.remote.RetrofitObject
import retrofit2.Response

class MoviesRepository: MoviesService {
    override suspend fun getMovies(
        endPoint: String,
        apiKey: String
    ): Response<Movies> {
        Log.d("endPoint_repo", "endPoint_repo: $endPoint")
        return RetrofitObject
            .api
            .getMovies(endPoint, apiKey)
    }
}

