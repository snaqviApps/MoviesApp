package edu.review.moviesappreview.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

fun checkAndRequestNotificationPermission (
    context: Context,
    requestPermissionLauncher: ActivityResultLauncher<String>?
) : Boolean {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    return true
}

fun createNotificationChannelsAndSetupNotifications(
    context: Context,
    channelId: String,
    message: String,
    requestPermissionLauncher: ActivityResultLauncher<String>?,
    contentTitle: String,
    notificationId: Int
) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            contentTitle,
              if (contentTitle == "Power Status Notifications") NotificationManager.IMPORTANCE_DEFAULT else  NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    // setup Notification, AFTER channel creation
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(contentTitle)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    checkAndRequestNotificationPermission (
        context,
        requestPermissionLauncher = requestPermissionLauncher
    )

    notificationManager.notify(notificationId, notification)
}



