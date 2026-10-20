# Vibhor

Vibhor is a standalone Android home launcher built for an aftermarket car
stereo. It is a normal Android application that registers itself as a home
app; it is not an Android Auto, Android Automotive, or Car App Library
application.

This milestone turns the foundation into a three-screen automotive launcher.
The screens are laid out on a horizontal pager: **Home** (system wallpaper and
a large clock), **Cockpit** (clock rail plus sound and brightness controls), and
**Apps** (a unified page with fixed quick controls beside the installed-app
launcher). The system wallpaper is rendered by Android behind the window, so no
storage permission is required and no wallpaper bitmap is ever decoded. A
**Settings** overlay is reachable from the Cockpit rail and the Apps quick
controls, and persists every tunable option — including the launcher-wide
accent color and surface background — through `SharedPreferences`.

## Target device

| Item | Value |
|---|---|
| Head unit | Blaupunkt FT Lauderdale 900 |
| Vehicle | Hyundai Creta 2024 base model |
| Target OS | Android 9 (API 28) |
| Reported memory | 4 GB RAM |
| Reported storage | 32 GB |
| Primary orientation | Landscape |
| Application ID | `com.pratap.vibhor` |

## Technology stack

- Kotlin (AGP 9.4.1 built-in Kotlin, Kotlin 2.2.10)
- Jetpack Compose (BOM 2026.09.00, Compose 1.12.1) with Material 3
- AndroidX Activity Compose, Lifecycle ViewModel and runtime Compose
- Kotlin Coroutines and Flow
- Gradle 9.6.0 with Kotlin DSL and a version catalog
- compileSdk 37, targetSdk 37, **minSdk 28**
- Java 11 bytecode target; JDK 25 toolchain (Gradle daemon JVM)

See [docs/android-compatibility.md](docs/android-compatibility.md) for the
reasoning behind the pinned versions.

## Prerequisites

- Android Studio (or a command-line Android SDK) with:
  - Android SDK Platform 37
  - Android SDK Build Tools 36.0.0 or newer
  - Platform Tools (`adb`)
- JDK 17 or newer. The project's `gradle/gradle-daemon-jvm.properties`
  requests a Java 25 toolchain, which the Gradle foojay resolver can
  provision. Android Studio's bundled JBR 25 works.
- The Gradle wrapper is committed; no separate Gradle install is required.

If `java` is not on your `PATH`, point `JAVA_HOME` at your JDK, for example:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

## Build a debug APK

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Other useful checks:

```bash
./gradlew test                    # JVM unit tests
./gradlew lint                    # Android lint
./gradlew connectedDebugAndroidTest  # UI tests; requires a device/emulator
```

## Install the APK

With the head unit (or an emulator) connected over ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Select Vibhor as the default launcher

Vibhor never changes the system launcher on its own. Choose it through the
device's supported system UI:

- **Android 9:** Settings → Apps & notifications → Default apps → Home app →
  **Vibhor**
- Some head units expose this as Settings → Home or a launcher picker the
  first time Home is pressed.

After selection, pressing Home opens Vibhor. Pressing Home again while Vibhor
is visible returns to the home screen instead of creating another activity.

For development, the activity can be launched directly without making it the
default launcher:

```bash
adb shell am start -n com.pratap.vibhor/.MainActivity
```

## Launcher screens and gestures

The launcher is a three-page horizontal pager. Page order is **Cockpit → Home →
Apps**, with Home as the default (middle) page.

| Page | Content |
|---|---|
| **Home** (default) | System wallpaper + large clock + date, with a small **Quick +** button in the bottom-right corner that opens favourite apps. No other branding, cards, buttons, or dock. |
| **Cockpit** | Clock rail with Settings entry, plus two wide vertical sliders: media volume and screen brightness. No fabricated navigation or media state. |
| **Apps** | Unified left-swipe screen: a fixed quick-control panel at roughly 30% width (compact left-aligned clock/date with the Settings gear on the clock line, labelled Volume and Brightness pills, and Bluetooth / Wi-Fi / Night mode tiles) beside the app launcher. The launcher keeps **Favourites** and **All** tabs, a Material 3 filled search field, and an adaptive grid of tonal app cards; tapping launches an app and long-pressing toggles a favourite. Search filters the active tab by label. |

Navigation gestures:

- **Swipe left** (finger left) → unified Apps page.
- **Swipe right** (finger right) → Cockpit.
- Paging does **not wrap** past the Apps or Cockpit edge.
- **Back** from Cockpit or Apps returns to Home; Back on Home is left to the system.
- Snap transitions target ~180 ms.
- Pressing **Home** (launcher reset) always returns to the Home page, even from another page or after resume. This behavior is preserved unchanged from the foundation milestone.

The unified Apps page divides the landscape into a control rail and an app
area. The rail stays fixed while the app grid scrolls independently. It follows
the same Material 3 styling as Settings: a compact, left-aligned digital clock,
labelled Volume and Brightness sliders, a filled Material 3 search field,
primary Favourites/All tabs, and tonal Material 3 app cards with the standard
ripple. The default appearance is a vivid blue accent on a true-black surface;
both sliders use the active accent color. Quick tiles are accent-tinted
when off and fill solid with the accent when active. The app columns and tile
sizes adapt to the available width and the persisted app-icon-size setting;
labels stay on one line and ellipsize when needed. The page background is
exactly the persisted launcher background at its configured opacity, with no
decorative gradient; at low visibility it shares the Home wallpaper. The app
grid uses compact cards sized for the right-hand panel, with large icons and
centered labels.

Sound and brightness read and write the same real device channels as Cockpit.
Volume changes made with hardware keys or by another app are reflected live;
brightness observes the global system setting. On first use the Brightness
slider offers to grant the special **Modify system settings** access so it can
write system brightness and stay in sync. If that access is declined, the
slider falls back to the previous window-level override and continues to work
only while Vibhor is visible. Wi-Fi and Bluetooth reflect actual adapter state
and request real state changes; unsupported radios are shown as unavailable
rather than fabricated. Long-pressing either connectivity tile opens the
matching Bluetooth or Wi-Fi page in Android Settings without changing the
adapter state. On Android 9 the legacy normal permissions allow those
toggles. Night mode is a persisted launcher-page dimming layer, because toggling
the system Night Light requires privileged access.

Settings opens from the Cockpit rail and from the compact gear beside the
Apps clock.

## Settings

A full-screen **Settings** overlay (not a pager page) opens from the gear
button in the Cockpit side rail or from the gear beside the Apps clock. It
contains:

| Setting | Range | Default | Effect |
|---|---|---|---|
| Clock size | 50%–200% (5% steps) | 100% | Multiplier on the computed Home clock size |
| Clock face | Classic / Split / Minimal / Bold / Elegant / Analog | Classic | Which clock face renders on Home (see below) |
| Time format | System / 12-hour / 24-hour | System | 12/24-hour format for the Home clock, the Cockpit rail clock, and the Apps control clock |
| Clock backdrop: Blur | Off, or 1–20 | 8 | Blurs the wallpaper behind the clock (Android 12+); darkness still applies on older versions |
| Clock backdrop: Darkness | 0%–100% | 35% | Full-screen dim layer over the wallpaper |
| Show status bar | On / Off | On | Hides the system status bar for a cleaner Home |
| Accent color | Blue / Teal / Green / Purple / Orange / Pink | Blue | Primary accent for controls, both sliders, active quick tiles, selection states, and app highlights |
| Launcher background | Black / Midnight / Slate / Graphite / Navy / Ocean | Black | Base surface color for the unified Apps page, Cockpit, and Settings; Home keeps the wallpaper |
| Background visibility | 0%–100% (5% steps) | 100% | Opacity of the launcher background; 0% is transparent and lets the wallpaper show through |
| Quick + button size | 50%–200% (5% steps) | 100% | Multiplier on the Home Quick + button |
| App icon size | 50%–200% (5% steps) | 100% | Multiplier on the Apps grid icon, cell, and label sizes |

Settings are grouped into five accent-marked cards: **Clock** (size, face,
format), **Clock backdrop** (blur, darkness), **Launcher background** (color,
visibility), **Appearance** (accent color, status bar), and **Home & apps**
(Quick + button size, app icon size). On wide landscape displays the Clock
card spans the top, Clock backdrop / Launcher background / Appearance share a
row, and Home & apps runs full width with its two sliders side by side; narrow
screens stack every card. Clock face previews form a single row in the Clock
card and scroll horizontally only when space is tight.

The launcher background applies to the unified Apps page, Cockpit, and Settings
surfaces. Home continues to show the system wallpaper with its own
clock-backdrop blur and darkness. A transparent launcher background lets the
same wallpaper show through the other pages; while Settings is open, the pager
is hidden so the overlay shows the wallpaper instead of the page underneath.

Every slider on this screen uses the same thick pill track as the Cockpit
volume and brightness controls: tap or drag anywhere on the pill, with values
snapping to the supported stops.

Clock faces (each sizes itself from the same computed clock size, so **Clock
size** and **Reset** apply to all of them):

- **Classic** — large serif time with the date below (the default look).
- **Split** — hour stacked above minutes with a small meridiem, plus the date.
- **Minimal** — thin sans-serif time, no date, smaller footprint.
- **Bold** — heavy sans-serif time with a compact date.
- **Elegant** — serif hour and minute separated by an accent divider.
- **Analog** — a Canvas-drawn face (tick marks, hour/minute hands, accent
  second hand) with the date below. The second hand keeps real time while
  Home is visible.

- **Reset to defaults** restores every setting, including the accent color and
  launcher background.
- Values persist in `SharedPreferences` (`vibhor_launcher_settings`) and
  survive process restarts.
- The accent color is read from the Compose theme (`VibhorThemeColors.accent`),
  never hardcoded in a screen, so new UI automatically follows the choice. Both
  Apps sliders, active quick tiles, and primary controls use that accent.
- Scales are multipliers on sizes computed per screen, so the same settings
  work across densities and orientations.
- **Back** (or the close button) dismisses Settings without leaving Home.

## Known limitations

- The system wallpaper is shown behind the Home page via the window flag
  (`windowShowWallpaper`). This requires no storage permission and never decodes
  a wallpaper bitmap. On devices where no wallpaper is available the screen
  falls back to a dark background.
- Clock blur requires Android 12+; on Android 9 the darkness layer still works.
- Cockpit volume drives the media stream through `AudioManager`; both sliders
  follow external system changes through live platform observers. Brightness
  writes the global `Settings.System.SCREEN_BRIGHTNESS` value when the user
  grants the special **Modify system settings** access, which is requested in
  context from the Brightness slider on the Apps page. Without that access it
  falls back to a window-level override for the current activity, so it resets
  once the window is gone. First-party hard keys stay in sync through a runtime
  volume-change receiver. The unified Apps page uses the same controls.
- Wi-Fi and Bluetooth quick controls use the legacy API 28 normal permissions.
  Their state comes from the actual system services and changes are never
  applied optimistically. Newer Android versions restrict Wi-Fi enabling and
  require runtime Bluetooth permissions, so a refused request leaves the real
  state unchanged and may show the radio as unavailable.
- Night mode on the Apps page is a persisted launcher dimming layer, not the
  system Night Light. Android 9 does not grant normal apps the privileged access
  needed to toggle the system setting.
- There is no turn-by-turn navigation, media session, weather, location,
  vehicle, or CAN bus integration.
- No boot receiver, background service, database, networking, or analytics.
- The clock updates once per minute while the screen is visible and reacts to
  system time and time-zone changes. It does not run in the background.
- The app adapts to portrait and landscape but does not force an orientation.
- The launcher has been built and tested against an API 28 emulator; it has
  not yet been profiled on the target head unit hardware.

## Project layout

```text
app/src/main/java/com/pratap/vibhor/
├── MainActivity.kt              # Launcher entry point, edge-to-edge, home-reset, accent theme host
├── VibhorApp.kt                 # Pager host (Apps/Home/Cockpit) + Settings overlay
├── core/
│   ├── platform/                # App discovery, launcher, clock ticker, settings store, accent/device controls
│   ├── time/                    # Clock abstraction and formatting (incl. split-time pieces)
│   └── ui/
│       ├── clock/               # Six stateless clock-face composables (shared by Home + Settings)
│       ├── navigation/          # LauncherPage enum (page order)
│       └── theme/               # Colors, typography, spacing, shapes, accent CompositionLocal
└── feature/
    ├── home/                    # Wallpaper + clock + Quick + favourites (HomeViewModel, screen)
    ├── cockpit/                 # Clock rail + volume/brightness sliders (CockpitViewModel, screen)
    ├── apps/                    # Favourites/All tabs over the installed-app grid (AppDrawerViewModel, screen)
    └── settings/                # Every persisted launcher setting (SettingsViewModel, screen)
```

## Documentation

- [Architecture](docs/architecture.md)
- [Android compatibility](docs/android-compatibility.md)
