package io.ntfy.wear.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import io.ntfy.wear.data.Subscription

@Composable
fun SubscriptionListScreen(
    subscriptions: List<Subscription>,
    onOpenSubscription: (Subscription) -> Unit,
    onAddSubscription: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        if (subscriptions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No subscriptions yet.\nTap Add to subscribe to a topic.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }

        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(subscriptions, key = { it.id }) { sub ->
                Chip(
                    onClick = { onOpenSubscription(sub) },
                    label = { Text(sub.displayTitle) },
                    secondaryLabel = { Text(sub.displaySubtitle) },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Circle,
                            contentDescription = null,
                            tint = if (sub.enabled) Color(0xFF4FD8C4) else Color.Gray
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = onAddSubscription,
                    label = { Text("Add subscription") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    colors = ChipDefaults.primaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Chip(
                    onClick = onOpenAbout,
                    label = { Text("About") },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
