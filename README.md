# Wave Launcher (Niagara-style scaffold)

A working Android Studio project for a Niagara-inspired launcher: home
screen is a scrollable alphabetical app list (no icon grid), with an
app drawer, search, gestures, theming, icon-pack support, and a
widget host wired to the real Android AppWidget APIs.

## Opening the project
1. Open Android Studio (Jellyfish or newer).
2. File → Open → select the `WaveLauncher` folder.
3. Let Gradle sync (it will download dependencies — this repo was
   written offline, so the very first sync needs internet access once).
4. Run on a device or emulator running Android 8.0 (API 26) or higher.
5. After installing, go to Settings → Apps → Default apps → Home app
   and pick "Wave Launcher" (or press Home and choose it when prompted).

## What's implemented
- **Home screen**: `ui/HomeScreen.kt` — full alphabetical app list with
  an A-Z fast-scroll rail on the right, like Niagara.
- **App drawer**: `ui/AppDrawerScreen.kt` — auto-focused search field,
  live filtering.
- **Gestures**: `gestures/` — swipe up / swipe down / double-tap are all
  configurable in Settings and dispatch through `GestureExecutor`.
- **Theming**: `ui/theme/` + `data/ThemePreferences.kt` — dark/light
  toggle and 6 accent-color swatches, persisted with DataStore.
- **Icon packs**: `data/IconPackManager.kt` — parses the standard
  `appfilter.xml` convention used by ADW/Nova/Apex-style icon packs on
  the Play Store, and auto-detects installed icon packs.
- **Widgets**: `widget/WidgetHostManager.kt` + `WidgetPickerActivity` —
  real `AppWidgetHost`/`AppWidgetManager` integration; "Add widget" in
  Settings launches the system widget picker.
- **Long-press an app**: hide from home, open app info, or uninstall.
- **Hidden apps**: hidden apps stay searchable-but-hidden — a "Manage
  hidden apps" screen isn't wired up yet (see below), but the data
  layer already tracks them (`LauncherViewModel.hiddenApps()`).

## Known gaps to finish (by design, so this ships as a real starting
point rather than stalling on the hardest 10%)
- **Rendering placed widgets on the home screen**: `WidgetHostManager`
  can create an `AppWidgetHostView` for a saved widget id, but
  `HomeScreen.kt` doesn't yet render a row of these above the app list.
  Wire it up with `AndroidView { widgetHostManager.createWidgetView(id) }`.
- **Lock screen / recent apps gestures**: these need a Device Admin
  receiver or an Accessibility Service respectively — both require
  extra manifest declarations and a user-facing permission grant flow.
  `gestures/GestureExecutor.kt` has clearly marked stubs for both.
- **Icon pack masking** (`<iconback>`/`<iconmask>`/`<iconupon>` and
  calendar icons): only the plain component→drawable mapping is parsed.
- **Manage hidden apps UI**: a settings sub-screen to unhide apps.
- **App icons on a grid / folders**: intentionally left out since
  Niagara's whole identity is the list-not-grid home.

## Package name
Currently `com.aurawave.launcher` — rename it (Android Studio:
right-click package → Refactor → Rename, plus update `applicationId`
and `namespace` in `app/build.gradle.kts`) before publishing.
