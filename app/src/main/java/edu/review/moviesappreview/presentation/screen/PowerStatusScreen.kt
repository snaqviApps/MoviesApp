package edu.review.moviesappreview.presentation.screen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import edu.review.moviesappreview.util.createNotificationChannelsAndSetupNotifications

const val POWER_NOTIFICATION_ID : Int = 1002
const val CHANNEL_TITLE = "Power Status Notifications"
@Composable
fun PowerStatusScreen() {
    val context = LocalContext.current
    val channelId = "power_status_channel"
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled if needed
    }

    DisposableEffect(context) {

        val powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (context == null) return

                val message = when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> "🔌 Cable plugged in real-time!"
                    Intent.ACTION_POWER_DISCONNECTED -> "🔋 Cable pulled out real-time!"
                    else -> return
                }

                createNotificationChannelsAndSetupNotifications(
                    context = context,
                    channelId = channelId,
                    message,
                    requestPermissionLauncher = requestPermissionLauncher,
                    contentTitle = CHANNEL_TITLE ,
                    notificationId = POWER_NOTIFICATION_ID
                )
            }
        }

        val filter = IntentFilter()
            .apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }

        context.registerReceiver(
            powerReceiver,
            filter
        )

        onDispose {
            context.unregisterReceiver(powerReceiver)
            println("PowerReceiver unregistered!")
        }

    }

}