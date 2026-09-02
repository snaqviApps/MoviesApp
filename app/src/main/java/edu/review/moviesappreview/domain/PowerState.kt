package edu.review.moviesappreview.domain

sealed interface PowerState {
    data object PluggedIn : PowerState
    data class PluggedStatusLoading(val isLoading : Boolean = true) : PowerState
    data class PluggedOut(val brightnessIsLow : Boolean = false) : PowerState
}