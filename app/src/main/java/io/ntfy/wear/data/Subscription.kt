package io.ntfy.wear.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * One subscription = one ntfy server + one (or comma-separated multiple) topic(s).
 * Deliberately flat / small: this is all that's needed to open a websocket
 * connection to `${serverUrl}/${topic}/ws`.
 */
@Serializable
data class Subscription(
    val id: String = UUID.randomUUID().toString(),
    val serverUrl: String,
    val topic: String,
    val username: String? = null,
    val password: String? = null,
    val accessToken: String? = null,
    val enabled: Boolean = true
) {
    /** Normalizes "https://ntfy.sh/" + "mytopic" -> "https://ntfy.sh" */
    val baseUrl: String get() = serverUrl.trimEnd('/')

    val displayTitle: String get() = topic

    val displaySubtitle: String get() = baseUrl.removePrefix("https://").removePrefix("http://")
}
