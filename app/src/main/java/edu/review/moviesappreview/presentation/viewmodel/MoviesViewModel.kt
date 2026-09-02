package edu.review.moviesappreview.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.review.moviesappreview.data.movies.Result as MoviesResult
import edu.review.moviesappreview.BuildConfig
import edu.review.moviesappreview.data.repository.system.MoviesNotifierRepository
import edu.review.moviesappreview.presentation.MoviesUIState
import edu.review.moviesappreview.usecase.GetFastestMovieFeedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor (
    private val moviesNotifier: MoviesNotifierRepository,
    private val getFastestMovieFeedUseCase: GetFastestMovieFeedUseCase,     // n/w data feed
//    private val getLocalDBMoviesAddUseCase: GetLocalDBMoviesAddUseCase,     // local db data feed

) : ViewModel() {
    private val _moviesState = MutableStateFlow<MoviesUIState>(MoviesUIState.Loading)
    val moviesState: StateFlow<MoviesUIState> = _moviesState.asStateFlow()
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

        _moviesState.value = MoviesUIState.Loading
        viewModelScope.launch {
            if (!showMovies) return@launch

            val result: Result<Pair<List<MoviesResult>, String>> =
                getFastestMovieFeedUseCase.execute(
                    defaultCategory = endPoint,
                    apiKey = BuildConfig.API_KEY,
                    page = 5
                )
            result.onSuccess { (movies, winnerEndPoint) ->
                currentEndPoint = winnerEndPoint
                _moviesState.update {
                    MoviesUIState.Success(moviesList = movies, endPoint = winnerEndPoint)
                }
                moviesNotifier.notifyRaceWinnerBroadcast(winnerEndPoint)
            }.onFailure { exception ->
                _moviesState.value = MoviesUIState.Error("Error fetching movies: ${exception.message}")
            }
        }

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