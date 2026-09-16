package io.ntfy.wear.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import io.ntfy.wear.data.Subscription

@Composable
fun SubscriptionDetailScreen(
    subscription: Subscription,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item {
                Text(subscription.displayTitle)
            }
            item {
                Text(subscription.baseUrl, style = androidx.wear.compose.material.MaterialTheme.typography.caption2)
            }
            item {
                ToggleChip(
                    checked = subscription.enabled,
                    onCheckedChange = onToggleEnabled,
                    label = { Text("Enabled") },
                    toggleControl = { androidx.wear.compose.material.Switch(checked = subscription.enabled) },
                    colors = ToggleChipDefaults.toggleChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = onDelete,
                    label = { Text("Delete") },
                    colors = ChipDefaults.chipColors(backgroundColor = androidx.wear.compose.material.MaterialTheme.colors.error),
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
