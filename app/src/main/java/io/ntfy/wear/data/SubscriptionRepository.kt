package io.ntfy.wear.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "ntfy_subscriptions")
private val SUBSCRIPTIONS_KEY = stringPreferencesKey("subscriptions_json")

private val json = Json { ignoreUnknownKeys = true }

/**
 * Single small JSON blob holding the list of subscriptions - there will only
 * ever be a handful of these on a watch, so a full key/value table is overkill.
 */
class SubscriptionRepository(private val context: Context) {

    val subscriptions: Flow<List<Subscription>> = context.dataStore.data.map { prefs ->
        prefs[SUBSCRIPTIONS_KEY]?.let {
            runCatching { json.decodeFromString<List<Subscription>>(it) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun add(subscription: Subscription) = update { it + subscription }

    suspend fun remove(id: String) = update { list -> list.filterNot { it.id == id } }

    suspend fun setEnabled(id: String, enabled: Boolean) = update { list ->
        list.map { if (it.id == id) it.copy(enabled = enabled) else it }
    }

    private suspend fun update(transform: (List<Subscription>) -> List<Subscription>) {
        context.dataStore.edit { prefs ->
            val current = prefs[SUBSCRIPTIONS_KEY]?.let {
                runCatching { json.decodeFromString<List<Subscription>>(it) }.getOrNull()
            } ?: emptyList()
            prefs[SUBSCRIPTIONS_KEY] = json.encodeToString(transform(current))
        }
    }
}
