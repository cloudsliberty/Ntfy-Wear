package io.ntfy.wear.data

import kotlinx.serialization.Serializable

/**
 * Mirrors the JSON ntfy sends on the subscribe streams. Only the fields the
 * watch app actually uses are kept - see https://docs.ntfy.sh/subscribe/api/
 * for the full schema. `event` is one of: open, keepalive, message, poll_request.
 */
@Serializable
data class NtfyMessage(
    val id: String? = null,
    val time: Long? = null,
    val event: String? = null,
    val topic: String? = null,
    val title: String? = null,
    val message: String? = null,
    val priority: Int? = null,
    val tags: List<String>? = null,
    val click: String? = null
)
