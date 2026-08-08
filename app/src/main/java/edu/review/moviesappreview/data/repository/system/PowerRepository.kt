package edu.review.moviesappreview.data.repository.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.review.moviesappreview.domain.PowerState
import edu.review.moviesappreview.domain.repository.IPowerRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PowerRepository @Inject constructor(
    @param:ApplicationContext private val appContext: Context
) : IPowerRepository {

    override fun getPowerState(): Flow<PowerState> = callbackFlow {
        // Emit current state immediately using a sticky intent, Immediately fetch the sticky intent to see the current status.
        // ACTION_BATTERY_CHANGED: This specific intent is marked by the Android System as "Sticky."
        val batteryStatus: Intent? = appContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        trySend(if (isCharging) PowerState.PluggedIn else PowerState.PluggedOut(brightnessIsLow = true))

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        trySend(PowerState.PluggedIn)
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        trySend(PowerState.PluggedOut(brightnessIsLow = true))
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        appContext.registerReceiver(receiver, filter)

        awaitClose {
            appContext.unregisterReceiver(receiver)
        }
    }
}