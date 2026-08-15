package edu.review.moviesappreview.domain

sealed interface PowerState {
    object PluggedIn : PowerState

    class PluggedStatusLoading(val isLoading : Boolean = true) : PowerState
    class PluggedOut(val brightnessIsLow : Boolean = false) : PowerState
}