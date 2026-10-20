# Architecture

Vibhor is a single-module Android app with feature-oriented packages. The
architecture is deliberately small: MVVM with a unidirectional state flow, no
dependency injection framework, no database, and no navigation library. The
launcher UI is a three-page horizontal pager (Cockpit, Home, Apps); the Apps
page combines fixed quick controls with the installed-app launcher.

## Package responsibilities

| Package | Responsibility |
|---|---|
| `com.pratap.vibhor` | `MainActivity` (launcher entry point, edge-to-edge, home-reset, accent theme host) and `VibhorApp` (horizontal pager host + Settings overlay). |
| `core.ui.theme` | Single source of truth for colors, typography, spacing, and shapes. The active accent lives in a `CompositionLocal` driven by the persisted setting. |
| `core.ui.navigation` | `LauncherPage` enum defining the pager page order (Cockpit, Home, Apps). |
| `core.time` | `ClockTicker` abstraction and pure date/time formatting functions (including split-time pieces for the Split clock face). |
| `core.platform` | Android system integrations: `SystemClockTicker`, installed-app discovery (`InstalledAppsSource`), app launching (`AppLauncher`), device controls (`DeviceControls`), quick connectivity controls (`QuickControls`), and settings persistence (`SettingsStore` + `SharedPreferencesSettingsStore`). Also the `ClockFace`, `ClockFormat`, `AccentColor`, and `LauncherBackground` enums. |
| `core.ui.clock` | Six stateless clock-face composables (`ClassicClockFace`, `SplitClockFace`, `MinimalClockFace`, `BoldClockFace`, `ElegantClockFace`, `AnalogClockFace`) shared by Home and the Settings preview cards. |
| `feature.home` | `HomeUiState`, `HomeViewModel`, and `HomeScreen` (wallpaper + clock + Quick + favourites). |
| `feature.cockpit` | `CockpitUiState`, `CockpitViewModel`, and `CockpitScreen` (clock rail, Settings entry point, custom sound and brightness sliders). |
| `feature.apps` | `AppDrawerUiState`, `AppDrawerViewModel`, `AppDrawerScreen` (standalone drawer components), and `UnifiedAppsScreen` (fixed quick-control rail plus app launcher for the left-swipe page). |
| `feature.settings` | `SettingsUiState`, `SettingsViewModel`, and `SettingsScreen` (every persisted launcher setting). |

UI code does not access Android system services directly. All system
integrations live in `core.platform` behind small interfaces (`ClockTicker`,
`InstalledAppsSource`, `AppLauncher`, `DeviceControls`, `QuickControls`), so
they can be faked in unit tests.

## Pager and screen host

`VibhorApp` hosts a `HorizontalPager` with page order **Cockpit (0) → Home (1) →
Apps (2)**. Home is the default page. The pager does not wrap past the
first or last page. Snap transitions target ~180 ms.

```text
VibhorApp (HorizontalPager)
├── LauncherPage.Cockpit → CockpitScreen + CockpitViewModel
├── LauncherPage.Home    → HomeScreen + HomeViewModel (unchanged minimal wallpaper page)
└── LauncherPage.Apps    → UnifiedAppsScreen + AppDrawerViewModel + CockpitViewModel
       ├── fixed quick-control rail (clock + settings gear, sound, brightness, radio/night tiles)
       └── embedded AppDrawerContent (search, tabs, app grid)

SettingsScreen + SettingsViewModel (overlay) opens from the Cockpit gear
or the Apps clock gear.
```

- **Swipe left** (finger left) opens the unified Apps page; **swipe right**
  opens Cockpit. This follows standard pager index semantics for the chosen
  page order.
- **Back** from Cockpit or Apps animates back to Home. Back on Home is disabled
  so the system default applies.
- A repeated **Home press** delivers `onNewIntent`; `MainActivity` bumps
  `homeRequestToken`, and a `LaunchedEffect` in `VibhorApp` scrolls the pager
  back to Home. This launcher-reset behavior is unchanged from the foundation.
- **Settings** is an overlay (not a pager page) toggled from the Cockpit side
  rail or the Apps clock gear. `settingsVisible` is held in
  `rememberSaveable` inside `VibhorApp`, and a dedicated `BackHandler` closes
  Settings before the pager's own Back handling applies.
- The Apps route reuses the same `AppDrawerViewModel` and `CockpitViewModel`
  instances as the other pages. The left control panel therefore shares real
  volume/brightness state with Cockpit while the app grid keeps its own search,
  tab, and scroll state.

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

The persisted **Launcher background** uses the same window wallpaper. Apps,
Cockpit, and Settings draw the selected background color at the configured
visibility on top of the window; at 0% the color is fully transparent and the
wallpaper shows through. Home deliberately keeps the wallpaper and its clock
backdrop controls. While the Settings overlay is visible, the pager is drawn
at zero alpha so a transparent background shows the wallpaper rather than the
launcher page underneath.

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
            ├──────────────► AppDrawerViewModel  ──  stateIn(WhileSubscribed(5s))
            │                        ▼
            │                StateFlow<AppDrawerUiState>  ──►  UnifiedAppsScreen
            │                        │                        (AppDrawerContent)
            └──────────────► HomeViewModel  ──  favourites = installed ∩ persisted
                                     ▼
                             Quick + popup on HomeScreen
```

- Icons are decoded off the main thread once per package set and reused; they
  are not decoded on every recomposition. Bitmaps are small (scaled down) and
  only for the current package list.
- Launching goes through `AppLauncher` (interface) implemented by
  `AndroidAppLauncher`; the presentation layer never calls `startActivity`.
- Favourites are stored by package name in `LauncherSettings` and joined with
  the live installed-app list, so uninstalled apps disappear without leaving a
  stale entry. The drawer tab (`Favourites`/`All`) and the search query live in
  `AppDrawerViewModel`. Home and the drawer share the same source of truth.
- `AppDrawerViewModel` filters the active tab by label (case-insensitive, after
  trimming the query) and exposes the result as `displayedApps`; the persisted
  `appIconScale` still drives icon, label, and column sizing. `AppDrawerScreen`
  keeps the Material 3 header, filled search field, primary tab row, and tonal
  Material 3 app cards with the standard ripple, with every color sourced from
  the theme and persisted accent.
- `AppDrawerContent` is the reusable, background-free part of that screen. The
  standalone `AppDrawerScreen` wraps it for previews/tests, while
  `UnifiedAppsScreen` embeds it in the right-hand panel with compact tile
  sizing (large icon, centered single-line label, adaptive column count).

### Cockpit device controls

`CockpitViewModel` combines the clock ticker, settings, `DeviceControls`, and
`QuickControls`; the two platform interfaces are implemented by
`AndroidDeviceControls` and `AndroidQuickControls`:

- **Volume** reads and writes `AudioManager.STREAM_MUSIC`. Slider fraction →
  index mapping is a pure function (`DeviceLevels`) and is unit tested. A
  runtime receiver for `android.media.VOLUME_CHANGED_ACTION` plus a lightweight
  fallback poll keeps the UI in sync when hardware keys or another app change
  the stream.
- **Brightness** writes the global `Settings.System.SCREEN_BRIGHTNESS` value
  when the user has granted the special **Modify system settings** access. On a
  successful global write the activity's window override is cleared so system
  brightness remains authoritative; if the write is refused, the launcher
  applies a window-scoped override instead. A `ContentObserver` on the
  brightness setting plus the same fallback poll reflects external changes.
  The Apps page asks for that access in context the first time the Brightness
  slider is used; declining leaves the fallback in place.
- The sliders are custom Canvas composables (`VerticalControlSlider`) with
  `detectDragGestures`: the top of the track is the maximum, the bottom the
  minimum. No third-party slider library is used.

### Unified Apps quick controls

`UnifiedAppsScreen` splits the left-swipe page into a fixed control rail and
the embedded `AppDrawerContent`. The rail is about 30% of the usable landscape
width (with a 38% fallback in portrait), separated from the app grid by a
one-pixel gradient divider. The launcher grid scrolls independently; the rail
does not.

- The rail renders a compact left-aligned clock and date from `CockpitUiState`,
  with the Settings gear sharing the clock's line, the same real
  media-volume and system-brightness values as Cockpit with visible **Volume**
  and **Brightness** labels, a Bluetooth/Wi-Fi row, and a full-width Night mode
  tile.
- Tapping a slider uses the existing device-control callbacks. Hardware volume
  changes still arrive through the shared `CockpitViewModel`, so the left rail
  and Cockpit stay consistent.
- Both pill sliders use the active accent color. Inactive quick tiles use an
  accent-tinted surface, border, icon, and status text; active tiles fill with
  the solid accent and switch their content to the accent-on color.
- The page background is exactly the persisted launcher background at the
  configured opacity, with no decorative backdrop gradient. At low opacity the
  system wallpaper shows through.
- `QuickControls` exposes `state()` plus legacy Wi-Fi/Bluetooth setters and an
  `observeChanges()` broadcast flow for `WIFI_STATE_CHANGED_ACTION` and
  `BluetoothAdapter.ACTION_STATE_CHANGED`. `AndroidQuickControls` uses the API 28
  normal permissions and wraps platform calls in `runCatching`; refusals leave
  the actual adapter state unchanged. Unsupported hardware is disabled and
  labelled **Unavailable**.
- Toggles are never optimistic. After a request, `CockpitViewModel` requests a
  fresh real-state read and the broadcast receiver publishes subsequent
  transitions.
- **Night mode** is a persisted launcher-page dimming layer (a black overlay)
  because Android 9 does not allow a normal app to toggle system
  Night Light or global dark mode. It applies only to the unified Apps page so
  Home and Cockpit remain unchanged.
- The active accent drives both sliders, the radio tiles, the Night-mode
  highlight, search, tabs, and app highlights.

### Accent color and theming

`MainActivity` obtains the process-wide `SharedPreferencesSettingsStore`,
collects `LauncherSettings`, and passes `accentColor` into `VibhorTheme`.
`VibhorTheme` builds the Material `darkColorScheme` from that accent and
provides it through `LocalAccentColor`.

```text
LauncherSettings.accentColor (persisted id)
            │
            ▼
MainActivity ── VibhorTheme(accentColor) ── MaterialTheme.primary
                                        └── LocalAccentColor
                                                  │
                                                  ▼
                            VibhorThemeColors.accent (all feature UI)
```

Screens must read accents from `VibhorThemeColors.accent` (or
`MaterialTheme.colorScheme.primary`); hardcoded accent literals are not used
anywhere in feature code. A new accent option is added once in `AccentColor`,
once in the theme palette, and once in the Settings swatch row.

The shipped defaults are `AccentColor.BLUE` and `LauncherBackground.BLACK`.
The accent palette uses bright Material tones that stay legible on black, while
the background palette remains dark and low-chroma for the same reason.
Material surfaces are neutral rather than tinted, so a pure-black launcher
surface keeps clean, readable card separation.

### Settings persistence

```text
SettingsScreen (sliders commit on release; swatches immediately)
        │  setters + resetToDefaults
        ▼
SettingsViewModel  ──►  StateFlow<LauncherSettings>
        ▲                       │ collectAsStateWithLifecycle()
        │                       ▼
SettingsStore (core.platform interface)
        │
        ├── SharedPreferencesSettingsStore  (production, process singleton)
        └── InMemorySettingsStore           (tests and previews)
```

- `LauncherSettings` holds the clamped scale multipliers (`clockScale`,
  `appIconScale`, `quickButtonScale`), the selected `clockFace`,
  `clockFormat`, `accentColor`, and `background`, the launcher
  `backgroundOpacity` (0–1), the launcher-only `nightMode` flag, the
  clock-backdrop values (`clockBlurRadius` 0–20, `backgroundDarkness` 0–1),
  `showStatusBar`, and the favourite package-name set.
- The store exposes a `StateFlow<LauncherSettings>` plus plain (non-suspend)
  setter functions, so no coroutine or repository layer is needed.
- `SharedPreferencesSettingsStore.get(context)` is a process-wide singleton
  over the `vibhor_launcher_settings` preferences file. Keys include
  `clock_scale`, `app_icon_scale`, `clock_face`, `clock_format`,
  `clock_blur_radius`, `background_darkness`, `show_status_bar`,
  `quick_button_scale`, `accent_color`, `background_color`,
  `background_opacity`, `night_mode`, and `favourite_package_names`. The
  in-memory `StateFlow` is updated synchronously and the preference write is
  flushed with `apply()`. Enum keys store the stable string id and fall back to
  the default on unknown values.
- Both `HomeViewModel` and `CockpitViewModel` combine `settingsStore.settings`
  so the clock format applies to the Home clock, the Cockpit rail clock, and
  the unified Apps control clock. `ClockFormat.SYSTEM` consults
  `DateFormat.is24HourFormat`; `H12`/`H24` override it regardless of the device
  setting.
- `CockpitViewModel` also combines `QuickControls.observeChanges()` so the
  radio tiles publish real adapter state. Both the Apps route and Cockpit route
  resolve the same activity-scoped ViewModel instance.
- `HomeViewModel` starts the app list with an empty-emission `onStart` so the
  clock never waits for the installed-app scan; favourites update when
  discovery completes.
- Scales multiply sizes computed per screen, so a single setting works across
  densities, orientations, and future content changes.
- ViewModels receive a `SettingsStore` through their factories and expose
  derived state in their own `UiState`; composables stay stateless.
- Every Settings slider uses the shared `PillSlider` (`core.ui.components`),
  the horizontal counterpart of the Cockpit track: a 48dp fully rounded pill,
  accent fill, translucent track, tap or drag to change, and step snapping via
  the pure, unit-tested `snapToSteps` helper.
- `SettingsScreen` groups its controls into accent-marked cards (`Clock`,
  `Clock backdrop`, `Launcher background`, `Appearance`, `Home & apps`). A
  `BoxWithConstraints` breakpoint at 900dp switches from a stacked layout to a
  landscape dashboard: full-width Clock, Clock backdrop / Launcher background /
  Appearance side by side, then full-width Home & apps. The clock-face row and
  both swatch grids adapt to their own available width instead of fixed screen
  sizes.

### Clock faces (Home)

Home renders one of six clock faces based on `settings.clockFace`. Each face
is a stateless composable in `core.ui.clock` that sizes itself from one
`baseSize` budget, so the same composable renders full-size on Home and as a
miniature static preview on the Settings screen:

```text
HomeScreen ── when (state.clockFace)
├── CLASSIC → ClassicClockFace   (serif time + date)
├── SPLIT   → SplitClockFace     (hour above minutes + meridiem + date)
├── MINIMAL → MinimalClockFace   (thin sans-serif time, no date)
├── BOLD    → BoldClockFace      (heavy time + compact date)
├── ELEGANT → ElegantClockFace   (serif split time + accent divider)
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
  behind a small interface, with presentation in a new `feature.media` package.
  Media playback itself stays outside the launcher. Do not simulate playback
  metadata.
- **New controls:** extend `DeviceControls` or `QuickControls` (core.platform)
  and surface them on the existing Cockpit screen or unified Apps rail; keep
  Android service access behind the interface so unit tests use a fake.
- **Settings / preferences:** new preferences should be added as fields of
  `LauncherSettings` with clamped defaults, persisted with a stable key, and
  surfaced on the existing `SettingsScreen` unless they are intentionally
  controlled from a quick-control tile. New UI must take its primary accent
  from `VibhorThemeColors.accent`, and any fixed secondary highlight should be
  defined in the theme palette rather than inline literals.
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
