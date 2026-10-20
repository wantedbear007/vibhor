# Vibhor

Vibhor is a standalone Android home launcher built for an aftermarket car
stereo. It is a normal Android application that registers itself as a home
app; it is not an Android Auto, Android Automotive, or Car App Library
application.

This milestone turns the foundation into a three-screen automotive launcher.
The screens are laid out on a horizontal pager: **Music** (astronaut stage, the
real active media session, and a volume slider), **Home** (system
wallpaper and a large clock), and **Apps** (a unified page with fixed quick
controls beside the installed-app launcher). The system wallpaper is rendered by
Android behind the window, so no storage permission is required and no wallpaper
bitmap is ever decoded. A **Settings** overlay is reachable from the Apps quick
controls, and persists every tunable option — including the launcher-wide accent
color and surface background — through `SharedPreferences`.

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

The launcher is a three-page horizontal pager. Page order is **Music → Home →
Apps**, with the **Clock** screen as the default. The Home button target is
configurable in **Settings → Home button**; choosing **Player** makes Music the
screen the Home button returns to, including when the launcher starts from the
Home button.

| Page | Content |
|---|---|
| **Home** (default clock) | System wallpaper + large clock + date, with a compact **Favourites** button in the bottom-right corner that opens favourite apps. No other branding, cards, buttons, or dock. |
| **Music** | A lightweight Canvas astronaut on a moon above the real active media session in a rounded player card that uses the available width, with a compact tall volume pill on the right. Never shows fabricated tracks or playback state. |
| **Apps** | Unified left-swipe screen: a fixed quick-control panel at roughly 30% width (compact left-aligned clock/date with the Settings gear beside the clock block, labelled Volume and Brightness pills, and Bluetooth / Wi-Fi / Night mode tiles) beside the app launcher. The launcher keeps a pill-shaped **Favourites**/**All** segmented control, a Material 3 filled search field, and an adaptive grid of tonal app cards; tapping launches an app and long-pressing toggles a favourite. Search filters the active tab by label. |

Navigation gestures:

- **Swipe left** (finger left) → unified Apps page.
- **Swipe right** (finger right) → Music page.
- Paging does **not wrap** past the Apps or Music edge.
- **Back** from Music or Apps returns to Home; Back on Home is left to the system.
- Snap transitions target ~260 ms and use the selected **swipe animation
  style** (Settings → **Swipe animation**).
- Pressing **Home** returns to the screen selected under Settings → **Home
  button** (Clock by default, or Player). The same target applies when the
  launcher is started from the Home button.

Swipe animation styles (all apply to every pager transition, in both
directions, and respect the system animation setting):

| Style | Effect |
|---|---|
| **Smooth Slide** (default) | The incoming screen slides into view with a subtle fade. |
| **Depth** | The incoming screen scales gently forward while the outgoing screen recedes, with a soft fade. |
| **Soft Fade** | The outgoing screen fades out while the incoming one fades in with minimal movement. |
| **Parallax** | The incoming and outgoing screens move at slightly different rates for a restrained layered effect. |
| **Card Stack** | Pages separate into rounded cards with a gentle tilt and soft shadow while the pager slide is damped mid-swipe. |
| **Tilt Cards** | The card look with a stronger tilt and larger corner radius, livelier but still readable. |
| **Cube** | Pages rotate in 3D around the edge that faces the centre, like two faces of a cube. |

The selected style is saved and applied immediately; the **Swipe animation**
settings card shows a compact preview for each option, and tapping the selected
preview replays it.

The Music page keeps the reference hierarchy: the astronaut stage above the
player card, and the compact tall volume pill on the right. Album art, title,
artist, position, duration, and transport availability all come from the
active session. The player card fills at least half the stage in landscape and
gives the thumbnail the left 30% at full content height, with matching 20 dp
insets on the left, top, and bottom, stacking metadata, progress, and transport
controls in the remaining 70%. The astronaut bounces on the beat, sways,
alternates toe-out dance steps, and pumps its arms while the stars twinkle —
only while a real session is playing and the page is visible; when playback
pauses the scene settles to a still pose and the equalizer stops animating. The
transport uses large, rounded iOS-style glyphs sized for tablet touch inside a
rounded-square play/pause surface. The page is a plain Canvas plus Compose
shapes — no 3D scene, no blur pass, no audio capture. The player card shows an
honest idle state when no session can be read, and an **Open settings** action
when Vibhor is missing notification access.

The unified Apps page divides the landscape into a control rail and an app
area. The rail stays fixed while the app grid scrolls independently. It follows
the same Material 3 styling as Settings: a compact, left-aligned digital clock,
labelled Volume and Brightness sliders, a filled Material 3 search field,
a pill-shaped Favourites/All segmented control with rounded ends and a divider,
and tonal Material 3 app cards with the standard
ripple. The default appearance is a vivid blue accent on a true-black surface;
both sliders use the active accent color. Quick tiles are accent-tinted
when off and fill solid with the accent when active. The app columns and tile
sizes adapt to the available width and the persisted app-icon-size setting;
labels stay on one line and ellipsize when needed. The page background is
exactly the persisted launcher background at its configured opacity, with no
decorative gradient; at low visibility it shares the Home wallpaper. The app
grid uses compact cards sized for the right-hand panel, with large icons and
centered labels.

Sound and brightness read and write the same real device channels as the Music
volume control.
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

Settings opens from the compact gear beside the Apps clock.

## Settings

A full-screen **Settings** overlay (not a pager page) opens from the gear
button beside the Apps clock. It contains:

| Setting | Range | Default | Effect |
|---|---|---|---|
| Clock size | 50%–200% (5% steps) | 100% | Multiplier on the computed Home clock size |
| Clock face | Classic / Split / Minimal / Bold / Elegant / Analog | Classic | Which clock face renders on Home (see below) |
| Time format | System / 12-hour / 24-hour | System | 12/24-hour format for the Home clock and the Apps control clock |
| Clock backdrop: Blur | Off, or 1–20 | 8 | Blurs the wallpaper behind the clock (Android 12+); darkness still applies on older versions |
| Clock backdrop: Darkness | 0%–100% | 35% | Full-screen dim layer over the wallpaper |
| Show status bar | On / Off | On | Hides the system status bar for a cleaner Home |
| Accent color | Blue / Teal / Green / Purple / Orange / Pink | Blue | Primary accent for controls, both sliders, active quick tiles, selection states, and app highlights |
| Launcher background | Black / Midnight / Slate / Graphite / Navy / Ocean | Black | Base surface color for the Music page, unified Apps page, and Settings; Home keeps the wallpaper |
| Background visibility | 0%–100% (5% steps) | 100% | Opacity of the launcher background; 0% is transparent and lets the wallpaper show through |
| Favourites button size | 50%–200% (5% steps) | 100% | Multiplier on the Home Favourites button |
| App icon size | 50%–200% (5% steps) | 100% | Multiplier on the Apps grid icon, cell, and label sizes |
| Home button default screen | Clock / Player | Clock | Screen the Home button returns to, including when launching from Home |
| Swipe animation | Smooth Slide / Depth / Soft Fade / Parallax / Card Stack / Tilt Cards / Cube | Smooth Slide | Transition effect used when swiping between Music, Home, and Apps; each option shows a compact preview |

Settings are grouped into seven accent-marked cards: **Clock** (size, face,
format), **Clock backdrop** (blur, darkness), **Launcher background** (color,
visibility), **Appearance** (accent color, status bar), **Home & apps**
(Favourites button size, app icon size), **Home button** (default screen), and
**Swipe animation** (seven selectable preview cards, four per row on wide
screens and two per row on narrow ones). On wide
landscape displays the Clock card spans the top, Clock backdrop / Launcher
background / Appearance share a row, and Home & apps / Home button / Swipe
animation run full width; narrow screens stack every card. Clock face previews
form a single row in the Clock card and scroll horizontally only when space is
tight.

The launcher background applies to the Music page, the unified Apps page, and
Settings surfaces. Home continues to show the system wallpaper with its own
clock-backdrop blur and darkness. A transparent launcher background lets the
same wallpaper show through the other pages; while Settings is open, the pager
is hidden so the overlay shows the wallpaper instead of the page underneath.

Every slider on this screen uses the same thick pill track as the Music
volume control: tap or drag anywhere on the pill, with values
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

- **Reset to defaults** restores every setting, including the accent color,
  launcher background, swipe animation, and Home button target.
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
- The Music page and the Apps page share the same real volume control. Volume
  drives the media stream through `AudioManager`; both sliders follow external
  system changes through live platform observers. Brightness is available on
  the Apps rail and writes the global `Settings.System.SCREEN_BRIGHTNESS` value
  when the user grants the special **Modify system settings** access, which is
  requested in context from the Brightness slider on the Apps page. Without
  that access it falls back to a window-level override for the current
  activity, so it resets once the window is gone. First-party hard keys stay in
  sync through a runtime volume-change receiver. The unified Apps page uses the
  same controls.
- Wi-Fi and Bluetooth quick controls use the legacy API 28 normal permissions.
  Their state comes from the actual system services and changes are never
  applied optimistically. Newer Android versions restrict Wi-Fi enabling and
  require runtime Bluetooth permissions, so a refused request leaves the real
  state unchanged and may show the radio as unavailable.
- Night mode on the Apps page is a persisted launcher dimming layer, not the
  system Night Light. Android 9 does not grant normal apps the privileged access
  needed to toggle the system setting.
- Media sessions on stock Android 9 are only readable by apps the user has
  granted **notification access**. Vibhor ships a notification-listener service
  for that single purpose; it never reads or stores notifications. Until access
  is granted the Music card shows its idle state with an **Open settings**
  action, and nothing is faked. Session metadata, art, duration, and transport
  availability depend entirely on what the playing app publishes; unsupported
  actions stay disabled and unknown fields fall back to neutral labels.
- The Music progress bar is a real seek control when the session advertises
  seek support; dragging updates the visible position and the seek is sent on
  release. Otherwise it stays a readout. Horizontal drags inside the slider
  stay on the slider, so they do not move the pager.
- There is no turn-by-turn navigation, weather, location, vehicle, or CAN bus
  integration.
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
├── VibhorApp.kt                 # Pager host (Music/Home/Apps) + Settings overlay
├── core/
│   ├── platform/                # App discovery, launcher, clock ticker, settings store, accent/device controls
│   │   └── media/               # Real media-session reader + notification-listener access
│   ├── time/                    # Clock abstraction, clock and playback-time formatting
│   └── ui/
│       ├── clock/               # Six stateless clock-face composables (shared by Home + Settings)
│       ├── components/          # Shared rounded pill slider (Apps + Music)
│       ├── navigation/          # LauncherPage enum (page order)
│       └── theme/               # Colors, typography, spacing, shapes, accent CompositionLocal
└── feature/
    ├── home/                    # Wallpaper + clock + Favourites button (HomeViewModel, screen)
    ├── music/                   # Media player + astronaut stage (MusicViewModel, screen)
    ├── cockpit/                 # Device-control view model shared by Music and Apps
    ├── apps/                    # Favourites/All tabs over the installed-app grid (AppDrawerViewModel, screen)
    └── settings/                # Every persisted launcher setting (SettingsViewModel, screen)
```

## Documentation

- [Architecture](docs/architecture.md)
- [Android compatibility](docs/android-compatibility.md)
