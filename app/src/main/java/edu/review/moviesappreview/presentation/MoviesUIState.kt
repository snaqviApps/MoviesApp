package edu.review.moviesappreview.presentation

import edu.review.moviesappreview.domain.model.Movie


sealed interface MoviesUIState {
    data class Success(val moviesList: List<Movie>, val endPoint: String = "popular") : MoviesUIState
    data class Error(val message: String) : MoviesUIState
    object Loading : MoviesUIState
}
