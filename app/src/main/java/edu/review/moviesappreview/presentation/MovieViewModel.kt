package edu.review.moviesappreview.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.review.moviesappreview.BuildConfig.API_KEY
import edu.review.moviesappreview.data.remote.MoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieViewModel(
    private val movieRepository: MoviesRepository = MoviesRepository()
) : ViewModel() {
    private val _moviesState = MutableStateFlow<MovieUIState>(MovieUIState.Loading)
    val moviesState: StateFlow<MovieUIState> = _moviesState.asStateFlow()
    var showMovies by mutableStateOf<Boolean>(false)
        private set
    var isLoading by mutableStateOf<Boolean>(true)
        private set
    var currentEndPoint by mutableStateOf<String>("popular")
        private set


    init {
        enableMoviesDataFetching()
    }

    fun fetchPopularOrTopRatedMovies(endPoint: String) {
        _moviesState.value = MovieUIState.Loading

        viewModelScope.launch {
            try {
                if(showMovies) {
                    Log.d("endPoint", "endPoint: $endPoint")
                    val moviesResult = movieRepository.getMovies(endPoint, API_KEY)
                    if (moviesResult.isSuccessful) {
                        moviesResult.body()?.let { movie ->
                            _moviesState.value = MovieUIState.Success(movie.results)
                        }
                    }
                }

            } catch (e: Exception) {
                _moviesState.value = MovieUIState.Error(e.message ?: "Unknown error")
                Log.d("viewModel_e", "Error: ${e.message}")
            }
        }
    }

    fun enableMoviesDataFetching(endPoint: String="popular") : String {
        if (!showMovies || currentEndPoint != endPoint ) {
            showMovies = true
            currentEndPoint = endPoint
            isLoading = false
            fetchPopularOrTopRatedMovies(endPoint)
        }

        return endPoint
    }

}