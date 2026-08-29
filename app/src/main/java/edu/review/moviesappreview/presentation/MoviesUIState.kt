package edu.review.moviesappreview.presentation

import edu.review.moviesappreview.data.movies.Result


sealed interface MoviesUIState {
    data class Success(val moviesList: List<Result>, val endPoint: String = "popular") : MoviesUIState
    data class Error(val message: String) : MoviesUIState
    object Loading : MoviesUIState
}
