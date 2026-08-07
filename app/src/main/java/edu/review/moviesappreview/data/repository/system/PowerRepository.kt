package edu.review.moviesappreview.data.repository.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    @param:ApplicationContext private val context: Context
) : IPowerRepository {

    override fun getPowerState(): Flow<PowerState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        trySend(PowerState.PluggedIn)
                    }

                    Intent.ACTION_POWER_DISCONNECTED -> {
                        // Defaulting brightness to false for PluggedOut state
                        trySend(PowerState.PluggedOut(brightness = false))
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        context.registerReceiver(receiver, filter)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}