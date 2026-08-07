package edu.review.moviesappreview.domain

sealed interface PowerState {
    object PluggedIn : PowerState
    class PluggedOut(val brightness : Boolean) : PowerState
}