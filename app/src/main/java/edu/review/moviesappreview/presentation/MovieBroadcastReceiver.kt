package edu.review.moviesappreview.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

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

            // Format the endpoint name cleanly for display (e.g., "top_rated" -> "Top Rated")
            val cleanWinnerName = winner.replace("_", " ").replaceFirstChar { it.uppercase() }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Setup Notification Channel (Required for Android 8.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Movie Race Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    enableLights(true)
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                manager.createNotificationChannel(channel)
            }

            // 2. Build and display the notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)                            // Standard icon
                .setContentTitle("🏁 Race Winner!")
                .setContentText("The winner is: ${winner.uppercase()}")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Match importance
                .setAutoCancel(true)
                .build()

            manager.notify(NOTIFICATION_ID, notification)
        }
    }
}