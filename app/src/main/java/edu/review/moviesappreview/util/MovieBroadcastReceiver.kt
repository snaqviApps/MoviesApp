package edu.review.moviesappreview.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MovieBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "movie_race_channel_v2"
        const val NOTIFICATION_ID = 101
        const val ACTION_RACE_COMPLETE = "edu.review.moviesappreview.RACE_COMPLETE"
        const val EXTRA_WINNER = "WINNING_ENDPOINT"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("MovieReceiver", "Broadcast Received: ${intent?.action}") // Verify reception
        if (intent?.action == ACTION_RACE_COMPLETE) {

            // Extract the winner dynamically from the intent extras
            val winner = intent.getStringExtra(EXTRA_WINNER) ?: "Unknown"

            // Setup Notification Channel (Required for Android 8.0+)
            createNotificationChannelsAndSetupNotifications(
                context = context,
                channelId = CHANNEL_ID,
                message = "The winner is: ${winner.uppercase()}",
                null,
                notificationId = NOTIFICATION_ID,
                contentTitle = "🏁 Race Winner!"
            )
        }
    }
}
