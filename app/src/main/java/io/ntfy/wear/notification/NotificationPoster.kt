package io.ntfy.wear.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.ntfy.wear.R
import io.ntfy.wear.data.NtfyMessage
import io.ntfy.wear.data.Subscription

object NotificationPoster {

    /** One channel per subscription so the user can mute/adjust a single topic from system settings. */
    fun channelId(subscription: Subscription) = "ntfy_topic_${subscription.id}"

    fun ensureChannel(context: Context, subscription: Subscription) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId(subscription),
            subscription.topic,
            NotificationManager.IMPORTANCE_HIGH
        )
        nm.createNotificationChannel(channel)
    }

    fun post(context: Context, subscription: Subscription, msg: NtfyMessage) {
        if (msg.event != "message") return // ignore open/keepalive/poll_request

        ensureChannel(context, subscription)

        val title = msg.title?.takeIf { it.isNotBlank() } ?: subscription.topic
        val body = msg.message.orEmpty()

        val builder = NotificationCompat.Builder(context, channelId(subscription))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(mapPriority(msg.priority))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setLocalOnly(true)
            .apply {
                msg.time?.let { setWhen(it * 1000) }
                msg.click?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    val pending = android.app.PendingIntent.getActivity(
                        context, msg.id.hashCode(), intent,
                        android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    setContentIntent(pending)
                }
            }

        val notificationId = (subscription.id + (msg.id ?: System.currentTimeMillis().toString())).hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    /** ntfy priority: 1=min .. 3=default .. 5=max */
    private fun mapPriority(priority: Int?): Int = when (priority) {
        1 -> NotificationCompat.PRIORITY_MIN
        2 -> NotificationCompat.PRIORITY_LOW
        4 -> NotificationCompat.PRIORITY_HIGH
        5 -> NotificationCompat.PRIORITY_MAX
        else -> NotificationCompat.PRIORITY_DEFAULT
    }
}
