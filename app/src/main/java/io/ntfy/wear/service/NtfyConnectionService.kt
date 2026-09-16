package io.ntfy.wear.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.ntfy.wear.MainActivity
import io.ntfy.wear.R
import io.ntfy.wear.data.NtfyMessage
import io.ntfy.wear.data.Subscription
import io.ntfy.wear.data.SubscriptionRepository
import io.ntfy.wear.notification.NotificationPoster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow

/**
 * Keeps one live websocket connection per enabled subscription open for as
 * long as the app has something to listen for. Runs as a foreground service
 * so Doze / background network restrictions don't kill the sockets.
 */
class NtfyConnectionService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: SubscriptionRepository

    private val client = OkHttpClient.Builder()
        .pingInterval(45, TimeUnit.SECONDS) // keeps the socket alive through most NAT/carrier timeouts
        .retryOnConnectionFailure(true)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    // subscription id -> live connection handle
    private val connections = mutableMapOf<String, Connection>()

    override fun onCreate() {
        super.onCreate()
        repository = SubscriptionRepository(applicationContext)
        startForeground(FOREGROUND_ID, buildForegroundNotification())

        serviceScope.launch {
            repository.subscriptions.collect { subs ->
                reconcile(subs)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        connections.values.forEach { it.close() }
        connections.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    /** Opens sockets for newly-enabled subscriptions, closes ones that were removed/disabled. */
    private fun reconcile(subs: List<Subscription>) {
        val wanted = subs.filter { it.enabled }.associateBy { it.id }

        // Drop connections whose subscription disappeared or was disabled.
        val toRemove = connections.keys - wanted.keys
        toRemove.forEach { id ->
            connections.remove(id)?.close()
        }

        // Start any newly-enabled subscriptions.
        wanted.values.forEach { sub ->
            val existing = connections[sub.id]
            if (existing == null || existing.subscription != sub) {
                existing?.close()
                connections[sub.id] = Connection(sub).also { it.start() }
            }
        }

        if (wanted.isEmpty()) {
            stopSelf()
        }
    }

    private fun buildForegroundNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            getString(R.string.service_channel_name),
            NotificationManager.IMPORTANCE_MIN
        )
        nm.createNotificationChannel(channel)

        val openApp = android.app.PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.service_notification_text))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    /** One reconnecting websocket for a single subscription. */
    private inner class Connection(val subscription: Subscription) {
        private var socket: WebSocket? = null
        private var attempt = 0
        private var job: Job? = null
        private var stopped = false

        fun start() {
            job = serviceScope.launch { connect() }
        }

        private suspend fun connect() {
            if (stopped) return
            val wsUrl = subscription.baseUrl
                .replaceFirst("https://", "wss://")
                .replaceFirst("http://", "ws://") + "/${subscription.topic}/ws"

            val requestBuilder = Request.Builder().url(wsUrl)
            when {
                !subscription.accessToken.isNullOrBlank() ->
                    requestBuilder.addHeader("Authorization", "Bearer ${subscription.accessToken}")
                !subscription.username.isNullOrBlank() ->
                    requestBuilder.addHeader(
                        "Authorization",
                        Credentials.basic(subscription.username, subscription.password.orEmpty())
                    )
            }

            socket = client.newWebSocket(requestBuilder.build(), object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    attempt = 0
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val msg = runCatching { json.decodeFromString<NtfyMessage>(text) }.getOrNull()
                        ?: return
                    NotificationPoster.post(applicationContext, subscription, msg)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, reason)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    scheduleReconnect()
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (!stopped) scheduleReconnect()
                }
            })
        }

        private fun scheduleReconnect() {
            if (stopped) return
            attempt++
            val backoffSeconds = min(60.0, 2.0.pow(min(attempt, 6))).toLong()
            serviceScope.launch {
                delay(TimeUnit.SECONDS.toMillis(backoffSeconds))
                connect()
            }
        }

        fun close() {
            stopped = true
            job?.cancel()
            socket?.close(1000, "subscription removed")
            socket = null
        }
    }

    companion object {
        private const val FOREGROUND_ID = 1
        private const val SERVICE_CHANNEL_ID = "ntfy_service"

        fun start(context: Context) {
            val intent = Intent(context, NtfyConnectionService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NtfyConnectionService::class.java))
        }
    }
}
