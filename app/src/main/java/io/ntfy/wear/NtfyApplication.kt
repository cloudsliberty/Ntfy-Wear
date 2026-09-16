package io.ntfy.wear

import android.app.Application
import io.ntfy.wear.data.SubscriptionRepository
import io.ntfy.wear.service.NtfyConnectionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NtfyApplication : Application() {

    val repository by lazy { SubscriptionRepository(this) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // The service itself is cheap/idle with zero subscriptions, but there's
        // no reason to keep a foreground notification around for nothing.
        scope.launch {
            repository.subscriptions.collect { subs ->
                if (subs.any { it.enabled }) {
                    NtfyConnectionService.start(this@NtfyApplication)
                }
            }
        }
    }
}
