package edu.review.moviesappreview.presentation.screen.system

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    val channelId = "power_status_channel"
    val context = LocalContext.current

    val powerState by viewModel.powerState.collectAsStateWithLifecycle()

    /** to track configuration changes, observed by Compose Snapshot System  */
    var lastNotifiedState by rememberSaveable { mutableStateOf<String?>(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled if needed
    }

    LaunchedEffect(powerState) {
        val currentStateKey = when (powerState) {
            is PowerState.PluggedStatusLoading -> null  // Loading is ignored
            is PowerState.PluggedIn -> "🔌 Cable plugged in real-time!"
            is PowerState.PluggedOut -> "🔋 Cable pulled out real-time!"
        }

        /**
         * lastNotifiedState: String? is serving as a "switch", to notify the user
         * only if in configuration changes, the notification has already not been sent,
         * here for Power-States
         */
        if(currentStateKey != null  && (currentStateKey != lastNotifiedState)) {
                createNotificationChannelsAndSetupNotifications(
                    context = context,
                    channelId = channelId,
                    message = currentStateKey,
                    requestPermissionLauncher = requestPermissionLauncher,
                    contentTitle = CHANNEL_TITLE,
                    notificationId = POWER_NOTIFICATION_ID
                )
                lastNotifiedState = currentStateKey     // update the lastest notification-state
            }

    }
}
