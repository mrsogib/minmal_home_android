// package com.aurawave.launcher.data

// import android.content.Context
// import androidx.datastore.preferences.core.booleanPreferencesKey
// import androidx.datastore.preferences.core.edit
// import androidx.datastore.preferences.core.intPreferencesKey
// import androidx.datastore.preferences.core.stringPreferencesKey
// import androidx.datastore.preferences.preferencesDataStore
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.map

// val Context.dataStore by preferencesDataStore(name = "wave_launcher_settings")

// private object Keys {
//     val DARK_THEME = booleanPreferencesKey("dark_theme")
//     val ACCENT_COLOR = intPreferencesKey("accent_color")
//     val ICON_PACK = stringPreferencesKey("icon_pack")
//     val SWIPE_UP_ACTION = stringPreferencesKey("swipe_up_action")
//     val SWIPE_DOWN_ACTION = stringPreferencesKey("swipe_down_action")
//     val DOUBLE_TAP_ACTION = stringPreferencesKey("double_tap_action")
// }

// /** Default accent: a soft indigo, close to Niagara's default palette. */
// private const val DEFAULT_ACCENT = 0xFF6C63FF.toInt()

// class ThemePreferences(private val context: Context) {

//     val darkTheme: Flow<Boolean> =
//         context.dataStore.data.map { it[Keys.DARK_THEME] ?: true }

//     val accentColor: Flow<Int> =
//         context.dataStore.data.map { it[Keys.ACCENT_COLOR] ?: DEFAULT_ACCENT }

//     val iconPack: Flow<String?> =
//         context.dataStore.data.map { it[Keys.ICON_PACK] }

//     val swipeUpAction: Flow<String> =
//         context.dataStore.data.map { it[Keys.SWIPE_UP_ACTION] ?: "APP_DRAWER" }

//     val swipeDownAction: Flow<String> =
//         context.dataStore.data.map { it[Keys.SWIPE_DOWN_ACTION] ?: "NOTIFICATIONS" }

//     val doubleTapAction: Flow<String> =
//         context.dataStore.data.map { it[Keys.DOUBLE_TAP_ACTION] ?: "LOCK_SCREEN" }

//     suspend fun setDarkTheme(enabled: Boolean) =
//         context.dataStore.edit { it[Keys.DARK_THEME] = enabled }

//     suspend fun setAccentColor(color: Int) =
//         context.dataStore.edit { it[Keys.ACCENT_COLOR] = color }

//     suspend fun setIconPack(pkg: String?) =
//         context.dataStore.edit {
//             if (pkg == null) it.remove(Keys.ICON_PACK) else it[Keys.ICON_PACK] = pkg
//         }

//     suspend fun setSwipeUpAction(action: String) =
//         context.dataStore.edit { it[Keys.SWIPE_UP_ACTION] = action }

//     suspend fun setSwipeDownAction(action: String) =
//         context.dataStore.edit { it[Keys.SWIPE_DOWN_ACTION] = action }

//     suspend fun setDoubleTapAction(action: String) =
//         context.dataStore.edit { it[Keys.DOUBLE_TAP_ACTION] = action }
// }






package com.aurawave.launcher.data

import android.content.Context
import androidx.core.content.edit

// Key-Value persistent storage helper class using Android SharedPreferences
class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("wave_launcher_prefs", Context.MODE_PRIVATE)

    // Clock display face (0 = Standard, 1 = Niagara Stacked)
    var clockStyle: Int
        get() = prefs.getInt("clock_style", 0)
        set(value) = prefs.edit { putInt("clock_style", value) }

    var clockTargetApp: String?
        get() = prefs.getString("clock_target_app", null)
        set(value) = prefs.edit { putString("clock_target_app", value) }

    var calendarTargetApp: String?
        get() = prefs.getString("calendar_target_app", null)
        set(value) = prefs.edit { putString("calendar_target_app", value) }

    var wallpaperFolderUri: String?
        get() = prefs.getString("wallpaper_folder_uri", null)
        set(value) = prefs.edit { putString("wallpaper_folder_uri", value) }

    var wallpaperIntervalMinutes: Long
        get() = prefs.getLong("wallpaper_interval_minutes", 60L)
        set(value) = prefs.edit { putLong("wallpaper_interval_minutes", value) }

    var swipeLeftApp: String?
        get() = prefs.getString("swipe_left_app", null)
        set(value) = prefs.edit { putString("swipe_left_app", value) }

    var swipeRightApp: String?
        get() = prefs.getString("swipe_right_app", null)
        set(value) = prefs.edit { putString("swipe_right_app", value) }

    var swipeUpApp: String?
        get() = prefs.getString("swipe_up_app", null)
        set(value) = prefs.edit { putString("swipe_up_app", value) }

    var drawerSortMode: Int
        get() = prefs.getInt("drawer_sort_mode", 0)
        set(value) = prefs.edit { putInt("drawer_sort_mode", value) }

    var favoriteAppPackages: List<String>
        get() = prefs.getString("favorite_packages", "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        set(value) = prefs.edit { putString("favorite_packages", value.joinToString(",")) }

    // Custom global app labels
    fun getCustomLabel(packageName: String): String? {
        return prefs.getString("label_$packageName", null)
    }

    fun setCustomLabel(packageName: String, label: String?) {
        prefs.edit { putString("label_$packageName", label) }
    }

    // App hiding state
    fun isAppHidden(packageName: String): Boolean {
        return prefs.getBoolean("hide_$packageName", false)
    }

    fun setAppHidden(packageName: String, hidden: Boolean) {
        prefs.edit { putBoolean("hide_$packageName", hidden) }
    }

    // App launch counts for "Frequently Used" sorting
    fun getUsageCount(packageName: String): Int {
        return prefs.getInt("usage_$packageName", 0)
    }

    fun incrementUsageCount(packageName: String) {
        val current = getUsageCount(packageName)
        prefs.edit { putInt("usage_$packageName", current + 1) }
    }
}
