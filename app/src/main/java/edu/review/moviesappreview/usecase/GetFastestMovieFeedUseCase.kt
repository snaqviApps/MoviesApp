package edu.review.moviesappreview.usecase

import android.util.Log
import edu.review.moviesappreview.BuildConfig
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.domain.repository.MoviesRepository
import edu.review.moviesappreview.data.movies.Result as MoviesResult // Alias prevents collision with kotlin.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import retrofit2.Response
import javax.inject.Inject

class GetFastestMovieFeedUseCase @Inject constructor (
    private val moviesRepository: MoviesRepository
) {
    suspend fun execute (
        defaultEndPoint: String,
        apiKey: String,
        page: Int
    ) : Result<Pair<List<MoviesResult>, String>> = coroutineScope {

        val startTime = System.currentTimeMillis()
        val popularDeferred: Deferred<Response<Movies>> = async {
            moviesRepository.getMovies(
                defaultEndPoint = defaultEndPoint,
                apiKey = BuildConfig.API_KEY,
                page = 5
            )
        }
        val topRatedDeferred: Deferred<Response<Movies>> = async {
            moviesRepository.getMovies(
                defaultEndPoint = "top_rated",
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

