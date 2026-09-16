package io.ntfy.wear.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ntfy.wear.data.SubscriptionRepository
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val hasEnabled = SubscriptionRepository(context.applicationContext)
                .subscriptions.first().any { it.enabled }
            if (hasEnabled) {
                NtfyConnectionService.start(context.applicationContext)
            }
            pending.finish()
        }
    }
}
