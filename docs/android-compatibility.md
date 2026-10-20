# Android compatibility

## Why API 28 is the minimum SDK

The target head unit runs Android 9 (API 28). The minimum SDK is fixed at 28
so Vibhor can use APIs that are guaranteed on the device without runtime
version branching for older platforms. In particular:

- `java.time` (clock, formatting, durations) is available from API 26, so no
  core-library desugaring is needed.
- Adaptive launcher icons, `LauncherApps`, platform `MediaSessionManager` /
  `MediaController`, and notification-listener services exist on API 28.
- The app does not need to support Android 8 or older devices.

`minSdk` must not be raised to work around dependency issues. If a future
dependency cannot support API 28, choose a compatible version or replace the
dependency.

## Pinned versions and why

| Component | Version | Reason |
|---|---|---|
| Gradle | 9.6.0 | Committed wrapper version. Required by AGP 9.x. Do not change casually. |
| Android Gradle Plugin | 9.4.1 | Project's generated, working AGP version; supports compileSdk 37. |
| Kotlin (built-in) | 2.2.10 | AGP 9.4.1's bundled Kotlin Gradle plugin version. No separate `kotlin-android` plugin is applied. |
| Compose compiler plugin | 2.2.10 | Must match the Kotlin compiler version used by built-in Kotlin. |
| Compose BOM | 2026.09.00 | Compose 1.12.1. Requires compileSdk 37 and AGP 9. |
| Activity Compose | 1.13.0 | Stable edge-to-edge and Compose activity integration. |
| Lifecycle | 2.11.0 | `collectAsStateWithLifecycle` and ViewModel Compose integration. |
| Core KTX | 1.19.1 | Generated project version; provides `ContextCompat.registerReceiver`. |
| Kotlin Coroutines | 1.10.2 | Stable version compatible with Kotlin 2.2.x. |
| compileSdk / targetSdk | 37 | Needed by Compose 1.12.x. targetSdk only affects behavior on newer devices; the app still runs on API 28. |
| Java source/target | 11 | AGP 9 default; supported by the Android 9 runtime via D8 desugaring of language features. |
| JDK toolchain | 25 | Declared in `gradle/gradle-daemon-jvm.properties`; AGP requires JDK 17+. |

Notes:

- AGP 9 has built-in Kotlin support. Do **not** add
  `org.jetbrains.kotlin.android`; it conflicts with built-in Kotlin.
- The Compose compiler plugin version is tied to the Kotlin version, not to
  the Compose BOM.
- Lint may report newer versions as available. They are not adopted here
  because AGP 9.4.1 bundles Kotlin 2.2.10 and the project intentionally keeps
  the generated toolchain stable.

## Android 9 constraints

- **No home-role API.** `RoleManager` and `RoleManager.ROLE_HOME` were added in
  API 29. On API 28 the launcher is registered with the `HOME` intent filter
  and selected through Settings → Default apps → Home app.
- **Package visibility.** Package visibility filtering starts at API 30. On
  API 28 all packages are visible by default. Because `targetSdk` is 37, the
  app declares narrow `<queries>` entries (launcher `MAIN`/`LAUNCHER`,
  navigation/mapping intent, and the `geo:` URI scheme) so app and navigation
  discovery work on Android 11+ devices as well. `QUERY_ALL_PACKAGES` is not
  used. See the wallpaper section below for the related storage-permission
  constraint.
- **Broadcast receiver flags.** `ContextCompat.registerReceiver` with
  `RECEIVER_NOT_EXPORTED` behaves as a normal receiver below API 33. Protected
  system broadcasts such as `ACTION_TIME_CHANGED` and
  `ACTION_TIMEZONE_CHANGED` are delivered.
- **Background restrictions.** Android 9 restricts background services and
  implicit broadcasts. Vibhor avoids both; it registers the time receiver, the
  volume/brightness observers, and the Wi-Fi/Bluetooth state receivers only
  while their state flows are collected.
- **System brightness access.** Writing `Settings.System.SCREEN_BRIGHTNESS`
  requires the special `WRITE_SETTINGS` app access, which the user grants
  through Settings rather than a runtime-permission dialog. The manifest
  declares `WRITE_SETTINGS`; the Brightness slider explains the access in
  context and opens `ACTION_MANAGE_WRITE_SETTINGS`. When the access is granted,
  brightness is written globally and external changes are observed; otherwise
  the launcher falls back to a window-level override. No system Night Light or
  global dark-mode write is attempted.
- **Wi-Fi and Bluetooth toggles.** On API 28 the manifest declares the legacy
  normal permissions `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`, `BLUETOOTH`, and
  `BLUETOOTH_ADMIN`; these are granted at install time and allow the quick
  tiles to read and change real adapter state. Android 10+ blocks
  `WifiManager.setWifiEnabled`, and Android 12+ requires runtime Bluetooth
  permissions. The implementation catches refusals and publishes the unchanged
  real state instead of an optimistic toggle; unsupported or inaccessible
  radios are shown as unavailable.
- **Media sessions require notification access.** On API 28 an ordinary app
  cannot enumerate other apps' media sessions unless it is an enabled
  notification listener or holds `MEDIA_CONTENT_CONTROL` (a signature
  permission). Vibhor declares a no-op `NotificationListenerService` with
  `BIND_NOTIFICATION_LISTENER_SERVICE` so the user can grant access; the service
  overrides no notification callbacks. Until access is granted, the Music card
  shows an idle state with a shortcut to the system notification-access screen
  instead of pretending a session exists. Session content and transport
  availability are read from `PlaybackState`/`MediaMetadata`, never inferred.
- **System night mode.** Normal apps cannot toggle system Night Light or global
  dark mode on API 28 without privileged access. The Apps page's Night mode is
  therefore a persisted launcher dimming layer scoped to that page, not a fake
  system toggle.
- **No predictive back.** `OnBackPressedDispatcher`/`BackHandler` is the
  supported mechanism; the predictive-back APIs from Android 13+ are not
  assumed.
- **Edge-to-edge.** targetSdk 35+ enforces edge-to-edge on newer devices. The
  app uses `enableEdgeToEdge` and `safeDrawingPadding`, so it renders
  correctly on both API 28 and newer devices.
- **No splash screen API.** The Android 12 splash screen API is not available;
  the app relies on the theme window background for startup. The window
  background is transparent so the system wallpaper shows through on Home; the
  Compose theme supplies the dark background on the Music and Apps pages.
- **Settings persistence.** Launcher settings are stored with
  `SharedPreferences`, which is available on every API level including 28. No
  DataStore dependency and no backup-agent or device-protected storage concerns
  apply; the preferences are internal and non-sensitive.

## Wallpaper strategy and why no storage permission

The Home page shows the current system wallpaper using the window flag
`android:windowShowWallpaper="true"` with a transparent window background. The
window manager renders and crops the wallpaper behind the window. This was
chosen over `WallpaperManager` bitmap retrieval for the following reasons,
verified against API 28 behavior:

- **`WallpaperManager.getDrawable()` / bitmap APIs require
  `READ_EXTERNAL_STORAGE` on API 28.** The wallpaper service enforces this
  permission when reading the wallpaper for a caller, so reading it directly
  would require a broad storage permission. This project deliberately requests
  no runtime permissions. (`WRITE_SETTINGS` for system brightness is a special
  app access, not a runtime permission, and is only used after an explicit
  in-context opt-in.)
- **Newer platforms restrict it further.** From Android 13 the wallpaper
  read APIs throw `SecurityException` for apps without special permissions,
  and from Android 14 they are generally unavailable to normal apps. Using the
  window flag keeps the launcher working unchanged from API 28 through current
  releases with no permission and no version branching.
- **No bitmap is decoded or retained.** Using the window flag means Vibhor
  never allocates a wallpaper bitmap, never re-reads it, and cannot leak it.
  This avoids the main-thread decode and repeated-read costs the milestone
  constraints call out.
- **Fallback.** On a device with no wallpaper the window simply shows the dark
  Compose background (a dark scrim over the transparent window), so the clock
  stays legible.

The window flag is the recommended approach for showing the system wallpaper
behind an app window; reading the wallpaper bitmap is specifically discouraged
for this use case.

## APIs that must not be assumed on API 28

- `android.app.role.RoleManager` and `ROLE_HOME` (API 29+)
- Package visibility `<queries>` semantics and `QUERY_ALL_PACKAGES` behavior
  (API 30+); the narrow `<queries>` declarations are ignored on API 28
- `WallpaperManager` wallpaper-read APIs, which require storage permission on
  API 28 and throw on newer platforms (see the wallpaper section)
- `NotificationManager` permission prompts and notification channel behavior
  changes introduced after Android 9
- Foreground service types introduced in Android 10+ (for example
  `mediaPlayback` type declarations)
- Media session APIs added after API 28; verify availability before use
- `WindowInsets` controller APIs added in Android 11+; use Compose insets or
  compatibility helpers
- Android 12+ splash screen, splash-screen animation, and themed app icons
- Predictive back (`OnBackInvokedCallback`) and Android 13+ back animations
- Bluetooth and location permission changes introduced after Android 9
- `WifiManager.setWifiEnabled` behavior on Android 10+ and the
  `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` runtime permissions introduced in
  Android 12; the API 28 quick toggles must not assume those calls still work
  on newer devices
- System Night Light / global dark-mode writes, which require privileged
  permissions on API 28 and are not attempted by the launcher
- Unrestricted system-settings writes: global brightness is only written after
  the user grants the special `WRITE_SETTINGS` access; otherwise the app keeps
  its window-level fallback

When adding a feature, check the API level of every platform API used and
guard newer APIs with `Build.VERSION.SDK_INT` or use AndroidX compatibility
APIs.
