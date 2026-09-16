package io.ntfy.wear

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import io.ntfy.wear.data.Subscription
import io.ntfy.wear.ui.AboutScreen
import io.ntfy.wear.ui.AddSubscriptionScreen
import io.ntfy.wear.ui.SubscriptionDetailScreen
import io.ntfy.wear.ui.SubscriptionListScreen
import io.ntfy.wear.ui.theme.NtfyTheme
import kotlinx.coroutines.launch

private sealed class Screen {
    data object List : Screen()
    data object Add : Screen()
    data object About : Screen()
    data class Detail(val id: String) : Screen()
}

class MainActivity : ComponentActivity() {

    /** Developer attribution, kept in one place (AndroidManifest meta-data) and read here. */
    private fun manifestMeta(key: String): String {
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData?.getString(key).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as NtfyApplication).repository
        val developerName = manifestMeta("io.ntfy.wear.DEVELOPER_NAME")
        val developerGithub = manifestMeta("io.ntfy.wear.DEVELOPER_GITHUB")
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

        setContent {
            val notificationPermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {}

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val subscriptions by repository.subscriptions.collectAsState(initial = emptyList())
            var screen by remember { mutableStateOf<Screen>(Screen.List) }

            NtfyTheme {
                when (val s = screen) {
                    is Screen.List -> SubscriptionListScreen(
                        subscriptions = subscriptions,
                        onOpenSubscription = { screen = Screen.Detail(it.id) },
                        onAddSubscription = { screen = Screen.Add },
                        onOpenAbout = { screen = Screen.About }
                    )

                    is Screen.Add -> AddSubscriptionScreen(
                        onSave = { sub: Subscription ->
                            lifecycleScope.launch { repository.add(sub) }
                            screen = Screen.List
                        },
                        onCancel = { screen = Screen.List }
                    )

                    is Screen.About -> AboutScreen(
                        appName = getString(R.string.app_name),
                        versionName = versionName,
                        developerName = developerName,
                        developerGithub = developerGithub,
                        onOpenGithub = {
                            startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://$developerGithub"))
                            )
                        },
                        onBack = { screen = Screen.List }
                    )

                    is Screen.Detail -> {
                        val sub = subscriptions.find { it.id == s.id }
                        if (sub == null) {
                            screen = Screen.List
                        } else {
                            SubscriptionDetailScreen(
                                subscription = sub,
                                onToggleEnabled = { enabled ->
                                    lifecycleScope.launch { repository.setEnabled(sub.id, enabled) }
                                },
                                onDelete = {
                                    lifecycleScope.launch { repository.remove(sub.id) }
                                    screen = Screen.List
                                },
                                onBack = { screen = Screen.List }
                            )
                        }
                    }
                }
            }
        }
    }
}
