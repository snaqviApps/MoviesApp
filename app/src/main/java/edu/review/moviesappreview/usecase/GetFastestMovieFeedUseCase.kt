package edu.review.moviesappreview.usecase

import android.util.Log
import edu.review.moviesappreview.BuildConfig
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.domain.repository.MoviesRepository
import edu.review.moviesappreview.data.movies.Result as MoviesResult    // Alias prevents collision with kotlin.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import javax.inject.Inject
import kotlin.collections.emptyList

class GetFastestMovieFeedUseCase @Inject constructor(
    private val moviesRepository: MoviesRepository<Movies>
) {
    suspend fun execute(
        defaultCategory: String,
        apiKey: String,
        page: Int
    ): Result<Pair<List<MoviesResult>, String>> = coroutineScope {

        val startTime = System.currentTimeMillis()
        val popularDeferred: Deferred<Result<Movies>> = async {
            moviesRepository.getMovies(
                defaultCategory = defaultCategory,
                apiKey = BuildConfig.API_KEY,
                page = 5
            )
        }
        val topRatedDeferred: Deferred<Result<Movies>> = async {
            moviesRepository.getMovies(
                defaultCategory = "top_rated",
                apiKey = BuildConfig.API_KEY,
                page = 5
            )
        }

        // Race the two deferred results using select
        val (winnerResponse, winnerEndPoint) = select {
            popularDeferred.onAwait { popularResponse ->
                topRatedDeferred.cancel()       // Cancel the losing request
                Pair(popularResponse, defaultCategory)
            }
            topRatedDeferred.onAwait { topRatedResponse ->
                popularDeferred.cancel()        // Cancel the losing request
                Pair(topRatedResponse, "top_rated")
            }
        }
        val endTime = System.currentTimeMillis()
        Log.d("winnerTime", "winnerTime: ${endTime - startTime}")

        // Maps correctly returns List<MovieResult>
        winnerResponse.map { movies ->
            Pair(movies.results ?: emptyList<MoviesResult>(), winnerEndPoint)
        }
    }
}

