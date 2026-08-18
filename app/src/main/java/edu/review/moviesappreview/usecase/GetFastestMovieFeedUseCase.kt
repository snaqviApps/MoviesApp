package edu.review.moviesappreview.usecase

import android.util.Log
import edu.review.moviesappreview.BuildConfig
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.data.repository.remote.MoviesRepository
import edu.review.moviesappreview.data.movies.Result as MoviesResult // Alias prevents collision with kotlin.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import retrofit2.Response
import javax.inject.Inject

class GetFastestMovieFeedUseCase @Inject constructor (
    private val iMoviesRepository: MoviesRepository             // single source of Truth
) {
    suspend operator fun invoke(
        defaultEndPoint: String,
        apiKey: String,
        page: Int
    ) : Result<Pair<List<MoviesResult>, String>> = coroutineScope {
        val startTime = System.currentTimeMillis()

        val popularDeferred: Deferred<Response<Movies>> = async {
            iMoviesRepository.getMovies(
                endPoint = defaultEndPoint,
                apiKey = BuildConfig.API_KEY,
                page = 5
            )
        }
        val topRatedDeferred: Deferred<Response<Movies>> = async {
            iMoviesRepository.getMovies(
                endPoint = "top_rated",
                apiKey = BuildConfig.API_KEY,
                page = 5
            )
        }

        // Race the two deferred results using select
        val (winnerResponse, winnerEndPoint) = select {
            popularDeferred.onAwait { popularResponse ->
                topRatedDeferred.cancel()       // Cancel the losing request
                Pair(popularResponse, defaultEndPoint)
            }
            topRatedDeferred.onAwait { topRatedResponse ->
                popularDeferred.cancel()        // Cancel the losing request
                Pair(topRatedResponse, "top_rated")
            }
        }
        val endTime = System.currentTimeMillis()
        Log.d("winnerTime", "winnerTime: ${endTime - startTime}")

        if (winnerResponse.isSuccessful) {
            val movies : List<MoviesResult> = winnerResponse.body()?.results ?: emptyList()
            Result.success(Pair(movies, winnerEndPoint)) // Now correctly returns List<MovieResult>
        } else {
            Result.failure(Exception("Error fetching movies with code: ${winnerResponse.code()}"))
        }
    }
}

