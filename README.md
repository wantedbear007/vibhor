# Vibhor

Vibhor is a standalone Android home launcher built for an aftermarket car
stereo. It is a normal Android application that registers itself as a home
app; it is not an Android Auto, Android Automotive, or Car App Library
application.

This milestone turns the foundation into a three-screen automotive launcher.
The screens are laid out on a horizontal pager: **Home** (system wallpaper and
a large clock), **Cockpit** (navigation and media cards), and **Apps** (a grid
of installed launcher apps). The system wallpaper is rendered by Android behind
the window, so no storage permission is required and no wallpaper bitmap is
ever decoded. App discovery, media integration, and navigation integration
beyond launching an installed navigation app are intentionally not included.
A **Settings** overlay (clock size and app-icon size) is reachable from the
Cockpit side rail and persists through `SharedPreferences`.

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

The launcher is a three-page horizontal pager. Page order is **Apps → Home →
Cockpit**, with Home as the default (middle) page.

| Page | Content |
|---|---|
| **Home** (default) | System wallpaper + large clock + date only. No branding, cards, buttons, or dock. |
| **Cockpit** | Navigation card (launches the discovered navigation app, or an honest empty state) + media card (honest empty state). No fabricated playback state. |
| **Apps** | Responsive grid of real installed launcher apps, sorted by label; tapping launches the app. |

Navigation gestures:

- **Swipe left** (finger left) → Cockpit.
- **Swipe right** (finger right) → Apps.
- Paging does **not wrap** past the Apps or Cockpit edge.
- **Back** from Cockpit or Apps returns to Home; Back on Home is left to the system.
- Snap transitions target ~180 ms.
- Pressing **Home** (launcher reset) always returns to the Home page, even from another page or after resume. This behavior is preserved unchanged from the foundation milestone.

## Settings

A full-screen **Settings** overlay (not a pager page) opens from the gear
button in the Cockpit side rail. It contains:

| Setting | Range | Default | Effect |
|---|---|---|---|
| Clock size | 50%–200% (5% steps) | 100% | Multiplier on the computed Home clock size |
| Clock face | Classic / Split / Minimal / Analog | Classic | Which clock face renders on Home (see below) |
| Time format | System / 12-hour / 24-hour | System | 12/24-hour format for the Home clock **and** the Cockpit rail clock |
| App icon size | 50%–200% (5% steps) | 100% | Multiplier on the Apps grid icon, cell, and label sizes |

Clock faces (each sizes itself from the same computed clock size, so **Clock
size** and **Reset** apply to all of them):

- **Classic** — large serif time with the date below (the default look).
- **Split** — hour stacked above minutes with a small meridiem, plus the date.
- **Minimal** — thin sans-serif time, no date, smaller footprint.
- **Analog** — a Canvas-drawn face (tick marks, hour/minute hands, accent
  second hand) with the date below. The second hand keeps real time while
  Home is visible.

- **Reset to defaults** restores both sliders to 100% and the clock face and time format to their defaults.
- Values persist in `SharedPreferences` (`vibhor_launcher_settings`) and
  survive process restarts.
- Scales are multipliers on sizes computed per screen, so the same settings
  work across densities and orientations.
- **Back** (or the close button) dismisses Settings without leaving Home.

## Known limitations

- The system wallpaper is shown behind the Home page via the window flag
  (`windowShowWallpaper`). This requires no storage permission and never decodes
  a wallpaper bitmap. On devices where no wallpaper is available the screen
  falls back to a dark background.
- The navigation card can discover and launch an installed navigation app, but
  there is no turn-by-turn guidance, routing, or live traffic integration.
- The media card shows an honest empty state; there is no media session,
  metadata, or playback control integration.
- No weather, location, vehicle, or CAN bus integration exists.
- No boot receiver, background service, database, networking, or analytics.
- The clock updates once per minute while the screen is visible and reacts to
  system time and time-zone changes. It does not run in the background.
- The app adapts to portrait and landscape but does not force an orientation.
- The launcher has been built and tested against an API 28 emulator; it has
  not yet been profiled on the target head unit hardware.

## Project layout

```text
app/src/main/java/com/pratap/vibhor/
├── MainActivity.kt              # Launcher entry point, edge-to-edge, home-reset
├── VibhorApp.kt                 # Pager host (Apps/Home/Cockpit) + Settings overlay
├── core/
│   ├── platform/                # App/nav discovery, launcher, clock ticker, settings store, ClockFace enum
│   ├── time/                    # Clock abstraction and formatting (incl. split-time pieces)
│   └── ui/
│       ├── clock/               # Four stateless clock-face composables (shared by Home + Settings)
│       ├── navigation/          # LauncherPage enum (page order)
│       └── theme/               # Colors, typography, spacing, shapes
└── feature/
    ├── home/                    # Wallpaper + clock (HomeViewModel, screen)
    ├── cockpit/                 # Nav + media cards + Settings entry (CockpitViewModel, screen)
    ├── apps/                    # Installed-app grid (AppDrawerViewModel, screen)
    └── settings/                # Clock/icon size sliders (SettingsViewModel, screen)
```

## Documentation

- [Architecture](docs/architecture.md)
- [Android compatibility](docs/android-compatibility.md)
