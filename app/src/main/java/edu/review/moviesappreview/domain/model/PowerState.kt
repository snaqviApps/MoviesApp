package edu.review.moviesappreview.domain.model

sealed interface PowerState {
    data object PluggedIn : PowerState
    data class PluggedStatusLoading(val isLoading : Boolean = true) : PowerState
    data class PluggedOut(val brightnessIsLow : Boolean = false) : PowerState
}