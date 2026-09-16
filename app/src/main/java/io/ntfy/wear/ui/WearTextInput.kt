package io.ntfy.wear.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.RemoteInput
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender

private const val RESULT_KEY = "ntfy_text_input"

/**
 * Builds the intent for the standard Wear OS text entry sheet (voice
 * dictation / handwriting / suggested replies). This is the idiomatic way to
 * collect free text on a watch instead of shipping a full on-screen keyboard.
 */
fun buildTextInputIntent(label: String): Intent {
    val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
    val remoteInputs = listOf(
        RemoteInput.Builder(RESULT_KEY)
            .setLabel(label)
            .wearableExtender { }
            .build()
    )
    RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
    return intent
}

/** Launches [buildTextInputIntent] and hands the typed text back via [onResult]. */
@Composable
fun rememberWearTextInputLauncher(onResult: (String) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.let { RemoteInput.getResultsFromIntent(it) }
                ?.getCharSequence(RESULT_KEY)
                ?.toString()
            if (!text.isNullOrBlank()) onResult(text)
        }
    }
