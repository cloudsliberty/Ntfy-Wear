package io.ntfy.wear.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText

@Composable
fun AboutScreen(
    appName: String,
    versionName: String,
    developerName: String,
    developerGithub: String,
    onOpenGithub: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item { Text(appName, style = MaterialTheme.typography.title3) }
            item { Text("v$versionName", style = MaterialTheme.typography.caption2) }
            item { Text("Developed by", style = MaterialTheme.typography.caption1) }
            item { Text(developerName, style = MaterialTheme.typography.body2) }
            item {
                Chip(
                    onClick = onOpenGithub,
                    label = { Text("GitHub") },
                    secondaryLabel = { Text(developerGithub) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = onBack,
                    label = { Text("Back") },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
