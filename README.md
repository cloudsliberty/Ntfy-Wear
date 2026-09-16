<div align="center">
  <img src="icon.png" width="128" alt="App Icon">
  <h1>NTFY-WEAR</h1>
</div>

## ntfy for Wear OS

**Developer:** Abdul Jaleel Adenpulan
**GitHub:** [github.com/cloudsliberty](https://github.com/cloudsliberty/ntfy-wear)

**self signed sideloadabel apk:** [ntfy-wear-self_signed v1.0.apk](https://github.com/cloudsliberty/Ntfy-Wear/blob/main/APK/ntfy-wear-self_signed.apk)

A minimal, standalone [ntfy](https://ntfy.sh) client built specifically for
Wear OS (round screens, small battery, no phone required). It is **not** a
port of the [official Android app](https://github.com/binwiederhier/ntfy-android) -
it's a from-scratch client that talks to the same server API, with everything
not needed on a watch removed:

- No Firebase/FCM path (ntfy.sh's own push relay) - only the
  self-hosted-style **direct websocket connection**, which works against
  ntfy.sh itself or any self-hosted instance.
- No attachments, actions, "publish from the app", broadcast receivers for
  other apps, backup/restore UI, or multi-language support.
- No app-level dark/light theme switching, tasker integration, or the split
  phone/wear modules the official app ships (`app`, `app-wear-shared`, etc.)
- One screen flow: list → add → detail. No settings screen beyond per-topic
  Android notification channels (which the system already gives you).

## How it works

1. You add a **subscription**: a server URL (e.g. `https://ntfy.sh` or your
   own `https://ntfy.example.com`) + a topic (or `topic1,topic2` - ntfy
   supports comma-separated topics natively). Optionally a username/password
   or an access token for protected topics.
2. A foreground `Service` (`NtfyConnectionService`) opens one
   `wss://<server>/<topic>/ws` WebSocket per enabled subscription using
   OkHttp, with a ping every 45s and exponential-backoff reconnect on
   failure/disconnect (1s → 2s → 4s ... capped at 60s).
3. Every `event":"message"` frame that arrives is turned into a normal
   Android notification (posted locally on the watch, one channel per
   subscription so you can mute/adjust importance per topic from system
   settings).
4. Subscriptions are persisted in a small JSON blob in Jetpack DataStore -
   there's no database, no WorkManager, no sync adapter.

This matches ntfy's own documented [subscribe API](https://docs.ntfy.sh/subscribe/api/):
`<topic>/ws` returns the same JSON objects as `<topic>/json`, just over a
WebSocket instead of a long-lived HTTP stream, which is a better fit for a
battery-constrained device (built-in ping/pong keep-alive, no reparsing a
chunked HTTP body).

## Wear OS specific choices

- UI is built with `androidx.wear.compose:wear-compose-material`
  (`Scaffold` + `ScalingLazyColumn` + `TimeText` + `PositionIndicator`),
  which automatically insets content correctly on round displays - no manual
  "is this a round screen" branching anywhere in the code.
- Text entry (server URL, topic, credentials) uses the standard Wear OS
  **remote input sheet** (`RemoteInputIntentHelper`) - the same voice/
  handwriting/suggestion picker every other Wear app uses - instead of
  shipping a cramped on-screen QWERTY keyboard.
- `android:name="com.google.android.wearable.standalone" value="true"` -
  the app works with just the watch's own WiFi/LTE/Bluetooth-tethered
  internet, no companion phone app required.
- Foreground service uses `foregroundServiceType="dataSync"` (required on
  API 34+) and a `MIN` importance, silent, ongoing notification, so it
  doesn't compete for the user's attention - only actual ntfy messages do.

## Project layout

```
app/src/main/java/io/ntfy/wear/
├── NtfyApplication.kt           starts the service whenever ≥1 subscription is enabled
├── MainActivity.kt              simple 3-screen state machine (no nav-compose dependency)
├── data/
│   ├── Subscription.kt          the persisted model
│   ├── NtfyMessage.kt           subset of ntfy's JSON message schema
│   └── SubscriptionRepository.kt  DataStore-backed CRUD
├── service/
│   ├── NtfyConnectionService.kt  one reconnecting websocket per subscription
│   └── BootReceiver.kt           restarts the service after device reboot
├── notification/
│   └── NotificationPoster.kt     ntfy message -> Android notification
└── ui/
    ├── SubscriptionListScreen.kt
    ├── AddSubscriptionScreen.kt
    ├── SubscriptionDetailScreen.kt
    ├── WearTextInput.kt          the remote-input helper
    └── theme/Theme.kt
```

## Building

**No prebuilt APK is bundled.** Compiling this needs the Android SDK, Gradle's
distribution server, and Google's/Maven Central's package repos to download
AndroidX, Compose, and OkHttp — none of which this project was assembled in
had network access to, so the source has not actually been compiled or
run yet. Treat it as a solid first draft to build and debug from, not a
verified artifact. Two ways to get an actual `.apk`:

### Option A — GitHub Actions (no local setup)
Push this project to a GitHub repo. `.github/workflows/build.yml` (included)
builds a debug APK on every push using `android-actions/setup-android` +
`gradle/actions/setup-gradle`, and uploads it as a workflow artifact you can
download from the Actions tab. This is the fastest path if you don't already
have Android Studio installed.

### Option B — Android Studio / local Gradle
```bash
# inside the project root, once, to generate the wrapper:
gradle wrapper --gradle-version 8.9

./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Or just open the folder in Android Studio (Koala+) — it will offer to
generate the wrapper and sync automatically. Expect to fix a handful of
small API-surface mismatches on first sync (see "Known limitations" below);
Wear Compose Material's exact parameter names shift a bit between versions.


## Known limitations / things intentionally left out for weight

- **Not yet compiled/run.** Written directly against the documented Wear
  Compose Material / OkHttp / DataStore APIs without a build step available.
  Most likely spots for a first-compile fix: `ChipDefaults`/`ToggleChip`
  parameter names drifting between `wear-compose-material` versions, and the
  exact `wearableExtender {}` builder options in `WearTextInput.kt`.
- No attachment/image download or display.
- No "publish a message" composer (this is a receive-only client).
- No local message history list - notifications ARE the history, same as
  every other Android notification.
- If the watch has no independent internet connection and only reaches the
  network via Bluetooth-tethering to a phone, delivery depends on that
  tethering staying active, same as any other standalone Wear app.
