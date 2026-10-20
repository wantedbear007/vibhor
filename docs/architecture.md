# Architecture

Vibhor is a single-module Android app with feature-oriented packages. The
architecture is deliberately small: MVVM with a unidirectional state flow, no
dependency injection framework, no database, and no navigation library. The
launcher UI is a three-page horizontal pager (Apps, Home, Cockpit).

## Package responsibilities

| Package | Responsibility |
|---|---|
| `com.pratap.vibhor` | `MainActivity` (launcher entry point, edge-to-edge, home-reset) and `VibhorApp` (horizontal pager host + Settings overlay). |
| `core.ui.theme` | Single source of truth for colors, typography, spacing, and shapes. |
| `core.ui.navigation` | `LauncherPage` enum defining the pager page order (Apps, Home, Cockpit). |
| `core.time` | `ClockTicker` abstraction and pure date/time formatting functions (including split-time pieces for the Split clock face). |
| `core.platform` | Android system integrations: `SystemClockTicker`, installed-app discovery (`InstalledAppsSource`), navigation-app discovery (`NavigationAppsSource`), app launching (`AppLauncher`), and settings persistence (`SettingsStore` + `SharedPreferencesSettingsStore`). Also the `ClockFace` enum (Classic/Split/Minimal/Analog). |
| `core.ui.clock` | Four stateless clock-face composables (`ClassicClockFace`, `SplitClockFace`, `MinimalClockFace`, `AnalogClockFace`) shared by Home and the Settings preview cards. |
| `feature.home` | `HomeUiState`, `HomeViewModel`, and `HomeScreen` (wallpaper + clock). |
| `feature.cockpit` | `CockpitUiState`, `CockpitViewModel`, and `CockpitScreen` (navigation + media cards, Settings entry point in the side rail). |
| `feature.apps` | `AppDrawerUiState`, `AppDrawerViewModel`, and `AppDrawerScreen` (installed-app grid). |
| `feature.settings` | `SettingsUiState`, `SettingsViewModel`, and `SettingsScreen` (clock-size and app-icon-size sliders). |

UI code does not access Android system services directly. All system
integrations live in `core.platform` behind small interfaces (`ClockTicker`,
`InstalledAppsSource`, `NavigationAppsSource`, `AppLauncher`), so they can be
faked in unit tests.

## Pager and screen host

`VibhorApp` hosts a `HorizontalPager` with page order **Apps (0) → Home (1) →
Cockpit (2)**. Home is the default page. The pager does not wrap past the
first or last page. Snap transitions target ~180 ms.

```text
VibhorApp (HorizontalPager)
├── LauncherPage.Apps    → AppDrawerScreen + AppDrawerViewModel
├── LauncherPage.Home    → HomeScreen + HomeViewModel
└── LauncherPage.Cockpit → CockpitScreen + CockpitViewModel
       └── gear button (side rail) → SettingsScreen + SettingsViewModel (overlay)
```

- **Swipe left** (finger left) advances toward Cockpit; **swipe right** opens
  Apps. This follows standard pager index semantics for the chosen page order.
- **Back** from Cockpit or Apps animates back to Home. Back on Home is disabled
  so the system default applies.
- A repeated **Home press** delivers `onNewIntent`; `MainActivity` bumps
  `homeRequestToken`, and a `LaunchedEffect` in `VibhorApp` scrolls the pager
  back to Home. This launcher-reset behavior is unchanged from the foundation.
- **Settings** is an overlay (not a pager page) toggled from the Cockpit side
  rail. `settingsVisible` is held in `rememberSaveable` inside `VibhorApp`, and
  a dedicated `BackHandler` closes Settings before the pager's own Back
  handling applies.

## Wallpaper strategy (Home)

The Home page shows the current system wallpaper **behind** the window using
the window flag `android:windowShowWallpaper="true"` with a transparent window
background. The window manager renders and crops the wallpaper; Vibhor never
requests a storage permission, never calls `WallpaperManager.getDrawable()`,
and never decodes or retains a wallpaper bitmap. Dark scrim gradients over the
clock keep text legible over light wallpapers and double as the fallback when
no wallpaper is available. See
[android-compatibility.md](android-compatibility.md) for the API-level
reasoning.

## UI state flow

Each feature follows the same MVVM pattern (clock shown here; apps and cockpit
follow the same shape):

```text
System clock / time-change broadcasts
            │
            ▼
SystemClockTicker (core.platform)  ── implements ClockTicker (core.time)
            │  Flow<ZonedDateTime> at minute boundaries
            ▼
HomeViewModel (feature.home)
            │  map to HomeUiState(timeText, dateText)
            │  stateIn(WhileSubscribed(5s))
            ▼
StateFlow<HomeUiState>
            │  collectAsStateWithLifecycle()
            ▼
HomeScreen (stateless composable)
```

- `HomeUiState` is immutable and holds only display-ready strings.
- `HomeViewModel` performs no Android service access itself; it receives a
  `ClockTicker` through its factory.
- Collection stops when the lifecycle is not at least `STARTED`, and the
  upstream flow stops after a five-second grace period. The broadcast receiver
  is unregistered when the ticker flow is cancelled, so no clock work runs in
  the background.
- The ticker aligns to the next minute rather than polling at a high
  frequency. It also listens for `ACTION_TIME_CHANGED` and
  `ACTION_TIMEZONE_CHANGED` to refresh immediately after manual changes.
- Composables only render state. Navigation between pages is a UI concern
  handled by the pager in `VibhorApp`.

### App discovery state flow

```text
PackageManager (MAIN/LAUNCHER query) + package-change broadcasts
            │
            ▼
AndroidInstalledAppsSource (core.platform)  ── implements InstalledAppsSource
            │  Flow<List<InstalledApp>>, re-emitted on package changes
            ▼
AppDrawerViewModel  ──  stateIn(WhileSubscribed(5s))
            ▼
StateFlow<AppDrawerUiState>  ──►  AppDrawerScreen (grid, tap to launch)
```

- Icons are decoded off the main thread once per package set and reused; they
  are not decoded on every recomposition. Bitmaps are small (scaled down) and
  only for the current package list.
- Launching goes through `AppLauncher` (interface) implemented by
  `AndroidAppLauncher`; the presentation layer never calls `startActivity`.

### Navigation-app discovery (Cockpit)

`AndroidNavigationAppsSource` resolves the installed navigation app once (via
`PackageManager` intent queries for the navigation/mapping intent) and exposes
it to `CockpitViewModel`. If none is found, the navigation card shows an
honest empty state. The media card always shows an honest empty state; no
playback state is fabricated.

### Settings persistence

```text
SettingsScreen (sliders, commit on release)
        │  setClockScale / setAppIconScale / resetToDefaults
        ▼
SettingsViewModel  ──►  StateFlow<LauncherSettings>
        ▲                       │ collectAsStateWithLifecycle()
        │                       ▼
SettingsStore (core.platform interface)
        │
        ├── SharedPreferencesSettingsStore  (production, process singleton)
        └── InMemorySettingsStore           (tests and previews)
```

- `LauncherSettings` holds two clamped scale multipliers (`clockScale` and
  `appIconScale`, range 0.5–2.0, default 1.0) and the selected `clockFace`
  (`ClockFace`, default `CLASSIC`).
- The store exposes a `StateFlow<LauncherSettings>` plus plain (non-suspend)
  setter functions, so no coroutine or repository layer is needed.
- `SharedPreferencesSettingsStore.get(context)` is a process-wide singleton
  over the `vibhor_launcher_settings` preferences file (keys `clock_scale`,
  `app_icon_scale`, `clock_face`); the in-memory `StateFlow` is updated
  synchronously and the preference write is flushed with `apply()`. The
  `clock_face` key stores the enum's stable string id and falls back to
  `CLASSIC` on unknown values.
- Scales multiply sizes computed per screen (clock size on Home; icon, cell,
  and label sizes on Apps), so a single pair of settings works across
  densities, orientations, and future content changes.
- ViewModels receive a `SettingsStore` through their factories and expose
  derived scale state in their own `UiState`; composables stay stateless.

### Clock faces (Home)

Home renders one of four clock faces based on `settings.clockFace`. Each face
is a stateless composable in `core.ui.clock` that sizes itself from one
`baseSize` budget, so the same composable renders full-size on Home and as a
miniature static preview on the Settings screen:

```text
HomeScreen ── when (state.clockFace)
├── CLASSIC → ClassicClockFace   (serif time + date)
├── SPLIT   → SplitClockFace     (hour above minutes + meridiem + date)
├── MINIMAL → MinimalClockFace   (thin sans-serif time, no date)
└── ANALOG  → AnalogClockFace    (Canvas face: ticks, hands, accent second hand + date)
```

- `splitClockTime` (in `core.time`) supplies the Split face's hour/minute/
  meridiem pieces; the same ticker feeds all faces.
- The Analog face's Canvas is drawn per composition; its second hand animates
  via a lightweight per-second recomposition only while Home is visible (no
  background work, no bitmap).

## Activity and launcher responsibilities

`MainActivity` is the only activity. It is exported and declares the
following intent filter:

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

- `MAIN` + `HOME` + `DEFAULT` is the standard launcher registration on
  Android 9. No `RoleManager` API is required.
- `android:launchMode="singleTask"` keeps a single launcher task. A repeated
  Home press delivers `onNewIntent`, and `MainActivity` bumps a token that
  scrolls the pager back to the Home page instead of stacking another activity.
- `android:excludeFromRecents="true"` keeps the launcher out of the recents
  list, as expected for a home app.
- The activity is not recreated for orientation changes by custom
  `configChanges`; Compose and `rememberSaveable` handle configuration changes
  and process recreation normally.
- Vibhor does not attempt to set itself as the default launcher. The user
  chooses it through system settings.

## Where future work belongs

- **Media:** a `core.platform` media-session source (for example
  `MediaSessionManager` or a notification listener, subject to permissions)
  behind a small interface, with presentation in the existing media card or a
  new `feature.media` package. Media playback itself stays outside the
  launcher. Do not simulate playback metadata.
- **Turn-by-turn navigation:** extend `NavigationAppsSource` or add a routing
  integration in `core.platform`; keep the presentation in `feature.cockpit`.
  Do not simulate live guidance data.
- **Settings / preferences:** `SettingsStore` (core.platform) with a
  `SharedPreferences` implementation now exists; new preferences should be
  added as fields of `LauncherSettings` with clamped defaults and surfaced as
  sliders or toggles on the existing `SettingsScreen`.
- **More screens:** introduce a navigation library only when deep links,
  nested graphs, or many screens make it pay for itself. The pager is
  sufficient for the three launcher pages.

## Architectural guardrails

- Keep state immutable and explicit.
- Keep system services and persistence out of composables and ViewModels.
- Introduce repositories, use cases, modules, or DI only for a concrete
  requirement.
- Prefer Android/Kotlin standard APIs over new dependencies.
- Avoid background work that is not tied to a visible screen.
- Show honest empty states; never fabricate system data (media, navigation,
  weather, vehicle).
