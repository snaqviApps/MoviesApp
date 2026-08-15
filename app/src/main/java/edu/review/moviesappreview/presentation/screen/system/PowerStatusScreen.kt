package edu.review.moviesappreview.presentation.screen.system

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.review.moviesappreview.domain.PowerState
import edu.review.moviesappreview.presentation.viewmodel.PowerViewModel
import edu.review.moviesappreview.util.createNotificationChannelsAndSetupNotifications

const val POWER_NOTIFICATION_ID: Int = 1002
const val CHANNEL_TITLE = "Power Status Notifications"

@Composable
fun PowerStatusScreen(
    viewModel: PowerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val powerState by viewModel.powerState.collectAsStateWithLifecycle()
    val channelId = "power_status_channel"

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled if needed
    }

    LaunchedEffect(powerState) {
        val message = when (powerState) {
            is PowerState.PluggedStatusLoading -> return@LaunchedEffect
            is PowerState.PluggedIn -> "🔌 Cable plugged in real-time!"
            is PowerState.PluggedOut -> "🔋 Cable pulled out real-time!"
        }

        createNotificationChannelsAndSetupNotifications(
            context = context,
            channelId = channelId,
            message = message,
            requestPermissionLauncher = requestPermissionLauncher,
            contentTitle = CHANNEL_TITLE,
            notificationId = POWER_NOTIFICATION_ID
        )
    }
}
