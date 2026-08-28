package edu.review.moviesappreview.domain.repository.system

/**
 * Contract for Notifying Movies Race Winner
 */
interface MovieNotifier {
    fun notifyRaceWinnerBroadcast(winnerEndPoint: String)
}