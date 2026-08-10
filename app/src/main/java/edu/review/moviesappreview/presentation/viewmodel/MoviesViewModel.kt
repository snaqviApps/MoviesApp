package edu.review.moviesappreview.presentation.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.review.moviesappreview.BuildConfig
import edu.review.moviesappreview.data.movies.Movies
import edu.review.moviesappreview.data.repository.remote.MoviesRepository
import edu.review.moviesappreview.presentation.MovieUIState
import edu.review.moviesappreview.util.MovieBroadcastReceiver
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MoviesViewModel @Inject constructor (
    private val application: Application,
    private val moviesRepository: MoviesRepository
) : ViewModel() {
    private val _moviesState = MutableStateFlow<MovieUIState>(MovieUIState.Loading)
    val moviesState: StateFlow<MovieUIState> = _moviesState.asStateFlow()
    var showMovies by mutableStateOf(false)
        private set

    var currentEndPoint by mutableStateOf("popular")
        private set

    var enableSecurityCamera by mutableStateOf(true)
        private set

    init {
        enableMoviesDataFetching(currentEndPoint)
    }

    fun fetchPopularOrTopRatedMovies(endPoint: String) {
        _moviesState.value = MovieUIState.Loading

        viewModelScope.launch {
            if (!showMovies) return@launch

            // Capture the exact baseline start time
            val startTime = System.currentTimeMillis()

            try {
                coroutineScope {
                    val popularDeferred: Deferred<Response<Movies>> = async {
                        moviesRepository.getMovies(
                            endPoint = endPoint,
                            apiKey = BuildConfig.API_KEY,
                            page = 5
                        )
                    }
                    val topRatedDeferred: Deferred<Response<Movies>> = async {
                        moviesRepository.getMovies(
                            endPoint = "top_rated",
                            apiKey = BuildConfig.API_KEY,
                            page = 5
                        )
                    }

                    val (winnerResponse, winnerEndPoint)  = select {
                        popularDeferred.onAwait { popularResponse ->
                            topRatedDeferred.cancel()
                            Pair(popularResponse, endPoint)
                        }
                        topRatedDeferred.onAwait { topRatedResponse ->
                            popularDeferred.cancel()
                            Pair(topRatedResponse, "top_rated")
                        }
                    }
                    val endTime = System.currentTimeMillis()
                    Log.d("winnerTime", "winnerTime: ${endTime - startTime}")

                    if (winnerResponse.isSuccessful) {
                        currentEndPoint = winnerEndPoint
                        _moviesState.update {
                            val movies = winnerResponse.body()?.results ?: emptyList()
                            MovieUIState.Success(moviesList = movies, endPoint = winnerEndPoint)
                        }
                        sendWinnerBroadcast(application, winnerEndPoint)
                    } else {
                        _moviesState.value =
                            MovieUIState.Error("Error fetching movies with code: ${winnerResponse.code()}")
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("MoviesViewModel", "Error fetching movies: ${e.message}", e)
                _moviesState.value = MovieUIState.Error("Error fetching movies: ${e.message}")
           }
        }

    }


    private fun sendWinnerBroadcast(appContext: Application, winnerEndPoint: String) {
        val broadcastIntent = Intent(appContext, MovieBroadcastReceiver::class.java).apply {
            action = MovieBroadcastReceiver.ACTION_RACE_COMPLETE
            putExtra(MovieBroadcastReceiver.EXTRA_WINNER, winnerEndPoint)
        }
        appContext.sendBroadcast(broadcastIntent)
    }

    fun enableMoviesDataFetching(endPoint: String): String {
        if (!showMovies || currentEndPoint != endPoint) {
            showMovies = true
            currentEndPoint = endPoint
            fetchPopularOrTopRatedMovies(endPoint)
        }
        return endPoint
    }

    fun enableExoPlayerDefaults() {
        enableSecurityCamera = !enableSecurityCamera
    }

}