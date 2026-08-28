package edu.review.moviesappreview.data.repository.system

import android.content.Intent
import edu.review.moviesappreview.domain.repository.system.MovieNotifier
import edu.review.moviesappreview.util.MovieBroadcastReceiver
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context

class MoviesNotifierRepository @Inject constructor(
   @param:ApplicationContext private val appContext: Context
) : MovieNotifier {
    override fun notifyRaceWinnerBroadcast(winnerEndPoint: String) {
            val intent = Intent(appContext, MovieBroadcastReceiver::class.java).apply {
                action = MovieBroadcastReceiver.ACTION_RACE_COMPLETE
                putExtra(MovieBroadcastReceiver.EXTRA_WINNER, winnerEndPoint)
            }
            appContext.sendBroadcast(intent)
    }

}