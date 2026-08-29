package edu.review.moviesappreview.domain.repository

/**
 * Contract for Notifying Movies Race Winner
 */
interface MovieNotifier {
    fun notifyRaceWinnerBroadcast(winnerEndPoint: String)
}