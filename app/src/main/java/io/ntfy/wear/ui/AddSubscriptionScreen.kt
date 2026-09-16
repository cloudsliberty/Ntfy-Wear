package io.ntfy.wear.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import io.ntfy.wear.data.Subscription

/**
 * Five optional/required fields, each edited via the Wear text-entry sheet
 * (voice / suggestions / emoji keyboard) rather than an on-screen keyboard.
 */
@Composable
fun AddSubscriptionScreen(
    onSave: (Subscription) -> Unit,
    onCancel: () -> Unit
) {
    var serverUrl by remember { mutableStateOf("https://ntfy.sh") }
    var topic by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    val serverInput = rememberWearTextInputLauncher { serverUrl = it }
    val topicInput = rememberWearTextInputLauncher { topic = it }
    val userInput = rememberWearTextInputLauncher { username = it }
    val passInput = rememberWearTextInputLauncher { password = it }
    val tokenInput = rememberWearTextInputLauncher { token = it }

    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item {
                Chip(
                    onClick = { serverInput.launch(buildTextInputIntent("Server URL")) },
                    label = { Text("Server") },
                    secondaryLabel = { Text(serverUrl) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = { topicInput.launch(buildTextInputIntent("Topic(s)")) },
                    label = { Text("Topic") },
                    secondaryLabel = { Text(topic.ifBlank { "e.g. myalerts" }) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = { userInput.launch(buildTextInputIntent("Username (optional)")) },
                    label = { Text("Username") },
                    secondaryLabel = { Text(username.ifBlank { "optional" }) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = { passInput.launch(buildTextInputIntent("Password (optional)")) },
                    label = { Text("Password") },
                    secondaryLabel = { Text(if (password.isBlank()) "optional" else "••••••") },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = { tokenInput.launch(buildTextInputIntent("Access token (optional)")) },
                    label = { Text("Access token") },
                    secondaryLabel = { Text(if (token.isBlank()) "optional, instead of user/pass" else "••••••") },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = {
                        if (serverUrl.isNotBlank() && topic.isNotBlank()) {
                            onSave(
                                Subscription(
                                    serverUrl = serverUrl.trim(),
                                    topic = topic.trim(),
                                    username = username.ifBlank { null },
                                    password = password.ifBlank { null },
                                    accessToken = token.ifBlank { null }
                                )
                            )
                        }
                    },
                    label = { Text("Save") },
                    colors = ChipDefaults.primaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = onCancel,
                    label = { Text("Cancel") },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
