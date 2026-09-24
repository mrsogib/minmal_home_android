# Minimal Launcher

A Niagara-Launcher-inspired, text-only Android home screen. First working
version of the feature set you described: favorites list, gesture navigation,
two clock styles with battery, app drawer with alphabet index and three sort
modes, hide/rename/group apps, folder-based random wallpaper, and an optional
double-tap-to-lock.

## Permissions used (and why — nothing else is requested)

| Permission | Type | What it's for |
|---|---|---|
| `EXPAND_STATUS_BAR` | Normal, auto-granted | Lets swipe-down open the notification shade / quick settings, same trick most third-party launchers use. No data access. |
| `SET_WALLPAPER` | Normal, auto-granted | Lets the app actually apply the wallpaper image you pick. |
| Device admin (optional) | User-approved dialog, only if you turn on "double-tap to lock" in Settings | Used for exactly one call, `lockNow()`. The app never reads or sends any data through it. You can revoke it any time in Android's own Settings → Security → Device admin apps. |

**No `INTERNET`, no location, no `READ_EXTERNAL_STORAGE`.** The wallpaper
folder feature uses Android's Storage Access Framework (the system folder
picker) — you grant access to exactly one folder, and the app can't see
anything else on your storage.

## What's implemented

- Favorites list (home screen "favorite apps"), manually reorderable by drag, rename, remove
- Swipe left / right / up → opens a random app from a pool you pick per direction (first use of each direction asks you to pick apps)
- Swipe down right-half → notification shade; swipe down left-half → quick settings
- Double-tap → lock screen (opt-in, explained above)
- Pinch in → Settings; pinch out → hidden apps list
- Two clock styles (regular; big-hour/small-minute + AM/PM + battery), tappable to open a chosen app
- Calendar line under the clock, also tappable to open a chosen app
- App drawer: alphabetical (skips letters with no apps) / frequency / manual sort, plus a right-edge A-Z index strip that jumps to a letter
- Hide / unhide apps, rename apps (drawer and favorites), group apps into named folders
- Random wallpaper from a folder you pick, rotates daily
- A small curated font list (5 options) applied across text

## Settings screen

Pinch in on the home screen (or open `SettingsActivity` directly) now gives you:

- **Theme:** dark/light toggle, plus optional custom hex colors for background and text that override the theme
- **Text sizes & spacing:** clock size scale, list row text size, row padding, alphabet strip text size — all editable numbers, applied immediately
- **Gesture sensitivity:** swipe distance threshold (dp) and double-tap timing window (ms) — both configurable instead of fixed
- Clock style (regular / big-hour) and font picker
- Wallpaper folder picker, adjustable rotation interval (hourly/6h/12h/daily/weekly), and a "change wallpaper now" button
- Double-tap-to-lock toggle (requests device admin only when turned on)
- Swipe-left / swipe-right / swipe-up app pools — multi-select which apps
  each gesture can randomly open
- Clock-tap and calendar-tap target apps — single-select
- Group management — rename or delete a group and see its members (groups
  are created from an app's long-press menu in the drawer or favorites list)
- A plain-language list of every permission the app uses

## What's stubbed / left for you to extend

- Group "folders" can be created, renamed, deleted and inspected from Settings,
  but there's no dedicated folder-opening UI on the home screen yet — next step
  would be a small popup grid when a group name is tapped.
- No drag-to-reorder in the app drawer's "Manual order" mode yet — only favorites
  support drag reorder right now; the manual drawer order can be set programmatically
  via `PrefsHelper.setManualDrawerOrder()`.
- App icons are intentionally never drawn (text-only), per your "no image assets" requirement.

## Note on the Gradle wrapper

The `gradle-wrapper.jar` binary isn't included in this zip (binaries don't travel
well through this export) — `gradlew`/`gradlew.bat` are here as plain scripts, but
they need that jar to actually run. When you open the project in Android Studio,
it will offer to regenerate the wrapper automatically — just accept, or run
`gradle wrapper` once if you have Gradle installed separately. Either way, first
sync needs internet once to fetch Gradle and dependencies.

## Building on GitHub (Actions)

There's a workflow at `.github/workflows/android-build.yml` that builds a debug
APK automatically on every push — this is what was missing before, which is why
Actions showed zero runs. It sidesteps the missing wrapper jar by installing
Gradle directly on the runner rather than using `./gradlew`. After you push:

1. Go to your repo's **Actions** tab — you should now see "Android Build" show up
   and run (it triggers on a push to any branch, so `claude` will trigger it).
2. Once it finishes, open the run and download the **minimal-launcher-debug-apk**
   artifact from the bottom of the run's summary page — that's an installable APK,
   built entirely in the cloud, no local Android Studio needed.
3. If it fails, open the failed step's log — most likely causes are a typo from
   manual edits or a dependency version mismatch; the error message will name the
   file and line.

## Build & push to GitHub

1. Install [Android Studio](https://developer.android.com/studio) if you don't
   already have it — you need it (or at least the Android SDK + a JDK) to compile
   this into an APK; GitHub only stores the code.
2. Unzip this project, open the folder in Android Studio ("Open an existing project").
3. Let Gradle sync (first sync downloads dependencies, needs internet once).
4. `Run ▶` with your phone connected via USB (enable Developer Options → USB debugging),
   or `Build → Build Bundle(s)/APK(s) → Build APK(s)` to get an installable file.
5. To push to GitHub: in Android Studio, `VCS → Enable Version Control`, or from a
   terminal in the project folder:
   ```
   git init
   git add .
   git commit -m "Initial minimal launcher scaffold"
   git remote add origin <your-repo-url>
   git push -u origin main
   ```
6. To actually use it as your home screen: install the APK, then press the physical
   Home button — Android will ask which launcher to use; pick this one (or set it as
   default in Settings → Apps → Default apps → Home app).

## Project layout

```
app/src/main/java/com/sogib/minimallauncher/
  MainActivity.kt          home screen: clock, favorites, gestures
  AppDrawerActivity.kt      full app list, sorting, alphabet index
  SettingsActivity.kt       clock style, font, wallpaper folder, lock toggle
  AppRepository.kt          reads installed apps from PackageManager
  AppInfo.kt                per-app data model
  PrefsHelper.kt            all persisted settings (SharedPreferences + JSON)
  ClockView.kt              custom-drawn clock (both styles)
  AlphabetIndexView.kt      right-edge A-Z strip
  FavoritesAdapter.kt / AppDrawerAdapter.kt
  FontOptions.kt            the 5 curated fonts
  WallpaperHelper.kt        SAF folder -> random wallpaper, daily rotation
  LauncherDeviceAdminReceiver.kt
```
