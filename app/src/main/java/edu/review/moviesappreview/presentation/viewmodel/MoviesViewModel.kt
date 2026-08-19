package edu.review.moviesappreview.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.review.moviesappreview.BuildConfig
import edu.review.moviesappreview.presentation.MovieUIState
import edu.review.moviesappreview.usecase.GetFastestMovieFeedUseCase
import edu.review.moviesappreview.util.MovieBroadcastReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor (
    private val application: Application,
    private val getFastestMovieFeedUseCase: GetFastestMovieFeedUseCase
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

            val result = getFastestMovieFeedUseCase(
                defaultEndPoint = endPoint,
                apiKey = BuildConfig.API_KEY,
                page = 5
            )
            result.onSuccess { (movies, winnerEndPoint) ->
                currentEndPoint = winnerEndPoint
                _moviesState.update {
                    MovieUIState.Success(moviesList = movies, endPoint = winnerEndPoint)
                }
                sendWinnerBroadcast(winnerEndPoint)
            }.onFailure { exception ->
                _moviesState.value = MovieUIState.Error("Error fetching movies: ${exception.message}")
            }
        }

    }


    private fun sendWinnerBroadcast(winnerEndPoint: String) {
        val broadcastIntent = Intent(application, MovieBroadcastReceiver::class.java).apply {
            action = MovieBroadcastReceiver.ACTION_RACE_COMPLETE
            putExtra(MovieBroadcastReceiver.EXTRA_WINNER, winnerEndPoint)
        }
        application.sendBroadcast(broadcastIntent)
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