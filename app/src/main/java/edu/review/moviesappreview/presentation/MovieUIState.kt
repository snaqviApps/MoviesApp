package edu.review.moviesappreview.presentation

import edu.review.moviesappreview.data.movies.Result


sealed interface MovieUIState {
    data class Success(val moviesList: List<Result>, val endPoint: String = "popular") : MovieUIState
    data class Error(val message: String) : MovieUIState
    object Loading : MovieUIState
}
