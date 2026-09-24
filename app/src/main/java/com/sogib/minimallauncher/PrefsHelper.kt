package com.sogib.minimallauncher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Everything the launcher remembers, stored in plain SharedPreferences as JSON strings.
 * No database library, no third-party dependency — just org.json, which ships with Android.
 */
class PrefsHelper(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    // ---- Favorites (home screen apps), manually ordered ----
    fun getFavoriteOrder(): List<String> {
        val raw = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { arr.getString(it) }
    }

    fun setFavoriteOrder(packageNames: List<String>) {
        prefs.edit().putString(KEY_FAVORITES, JSONArray(packageNames).toString()).apply()
    }

    // ---- Hidden apps ----
    fun getHiddenApps(): Set<String> =
        prefs.getStringSet(KEY_HIDDEN, emptySet()) ?: emptySet()

    fun setHiddenApps(set: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN, set).apply()
    }

    // ---- Custom app names: packageName -> label ----
    fun getCustomLabels(): Map<String, String> {
        val raw = prefs.getString(KEY_CUSTOM_LABELS, null) ?: return emptyMap()
        val obj = JSONObject(raw)
        return obj.keys().asSequence().associateWith { obj.getString(it) }
    }

    fun setCustomLabel(packageName: String, label: String?) {
        val current = getCustomLabels().toMutableMap()
        if (label.isNullOrBlank()) current.remove(packageName) else current[packageName] = label
        val obj = JSONObject()
        current.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_CUSTOM_LABELS, obj.toString()).apply()
    }

    // ---- App groups (folders): folderName -> list of packageNames ----
    fun getGroups(): Map<String, List<String>> {
        val raw = prefs.getString(KEY_GROUPS, null) ?: return emptyMap()
        val obj = JSONObject(raw)
        return obj.keys().asSequence().associateWith { key ->
            val arr = obj.getJSONArray(key)
            (0 until arr.length()).map { arr.getString(it) }
        }
    }

    fun setGroups(groups: Map<String, List<String>>) {
        val obj = JSONObject()
        groups.forEach { (name, members) -> obj.put(name, JSONArray(members)) }
        prefs.edit().putString(KEY_GROUPS, obj.toString()).apply()
    }

    // ---- Launch counts, for "frequently used" sort ----
    fun incrementLaunchCount(packageName: String) {
        val counts = getLaunchCounts().toMutableMap()
        counts[packageName] = (counts[packageName] ?: 0) + 1
        val obj = JSONObject()
        counts.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_LAUNCH_COUNTS, obj.toString()).apply()
    }

    fun getLaunchCounts(): Map<String, Int> {
        val raw = prefs.getString(KEY_LAUNCH_COUNTS, null) ?: return emptyMap()
        val obj = JSONObject(raw)
        return obj.keys().asSequence().associateWith { obj.getInt(it) }
    }

    // ---- Drawer sort mode ----
    enum class SortMode { ALPHABETICAL, FREQUENCY, MANUAL }

    fun getSortMode(): SortMode =
        SortMode.valueOf(prefs.getString(KEY_SORT_MODE, SortMode.ALPHABETICAL.name)!!)

    fun setSortMode(mode: SortMode) {
        prefs.edit().putString(KEY_SORT_MODE, mode.name).apply()
    }

    fun getManualDrawerOrder(): List<String> {
        val raw = prefs.getString(KEY_MANUAL_DRAWER_ORDER, null) ?: return emptyList()
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { arr.getString(it) }
    }

    fun setManualDrawerOrder(packageNames: List<String>) {
        prefs.edit().putString(KEY_MANUAL_DRAWER_ORDER, JSONArray(packageNames).toString()).apply()
    }

    // ---- Clock style: 0 = regular, 1 = big-hour/small-minute + battery ----
    fun getClockStyle(): Int = prefs.getInt(KEY_CLOCK_STYLE, 0)
    fun setClockStyle(style: Int) { prefs.edit().putInt(KEY_CLOCK_STYLE, style).apply() }

    // ---- Which app opens when tapping the clock / calendar widget ----
    fun getClockTapApp(): String? = prefs.getString(KEY_CLOCK_TAP_APP, null)
    fun setClockTapApp(packageName: String?) { prefs.edit().putString(KEY_CLOCK_TAP_APP, packageName).apply() }

    fun getCalendarTapApp(): String? = prefs.getString(KEY_CALENDAR_TAP_APP, null)
    fun setCalendarTapApp(packageName: String?) { prefs.edit().putString(KEY_CALENDAR_TAP_APP, packageName).apply() }

    // ---- Swipe left / right / up target apps (random pool per direction) ----
    fun getSwipePool(direction: String): Set<String> =
        prefs.getStringSet("swipe_pool_$direction", emptySet()) ?: emptySet()

    fun setSwipePool(direction: String, packages: Set<String>) {
        prefs.edit().putStringSet("swipe_pool_$direction", packages).apply()
    }

    // ---- Wallpaper folder (SAF tree URI, stored as string) ----
    fun getWallpaperFolderUri(): String? = prefs.getString(KEY_WALLPAPER_FOLDER, null)
    fun setWallpaperFolderUri(uri: String?) { prefs.edit().putString(KEY_WALLPAPER_FOLDER, uri).apply() }

    // ---- Chosen font (index into the small curated list in FontOptions) ----
    fun getFontIndex(): Int = prefs.getInt(KEY_FONT_INDEX, 0)
    fun setFontIndex(index: Int) { prefs.edit().putInt(KEY_FONT_INDEX, index).apply() }

    // ---- Double-tap-to-lock enabled (still requires device-admin grant separately) ----
    fun isLockOnDoubleTapEnabled(): Boolean = prefs.getBoolean(KEY_LOCK_ENABLED, false)
    fun setLockOnDoubleTapEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_LOCK_ENABLED, enabled).apply() }

    // ---- Theme: dark (default) or light, applied to both home and drawer ----
    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK_THEME, true)
    fun setDarkTheme(dark: Boolean) { prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply() }

    /** Background color, resolved from custom override or the current theme default.
     *  Uses a soft off-black/off-white rather than pure #000/#FFF — easier on the eyes,
     *  still reads as a minimalist dark/light theme. */
    fun getBgColor(): Int {
        val custom = prefs.getString(KEY_CUSTOM_BG, null)
        if (custom != null) return android.graphics.Color.parseColor(custom)
        return if (isDarkTheme()) android.graphics.Color.parseColor("#121212")
        else android.graphics.Color.parseColor("#FAFAFA")
    }

    fun getTextColor(): Int {
        val custom = prefs.getString(KEY_CUSTOM_TEXT, null)
        if (custom != null) return android.graphics.Color.parseColor(custom)
        return if (isDarkTheme()) android.graphics.Color.parseColor("#EDEDED")
        else android.graphics.Color.parseColor("#1A1A1A")
    }

    fun setCustomBgColor(hex: String?) { prefs.edit().putString(KEY_CUSTOM_BG, hex).apply() }
    fun setCustomTextColor(hex: String?) { prefs.edit().putString(KEY_CUSTOM_TEXT, hex).apply() }
    fun getCustomBgColorRaw(): String? = prefs.getString(KEY_CUSTOM_BG, null)
    fun getCustomTextColorRaw(): String? = prefs.getString(KEY_CUSTOM_TEXT, null)

    // ---- Text sizes & spacing (all in sp/dp, stored as float) ----
    fun getClockSizeScale(): Float = prefs.getFloat(KEY_CLOCK_SCALE, 1.0f)
    fun setClockSizeScale(scale: Float) { prefs.edit().putFloat(KEY_CLOCK_SCALE, scale).apply() }

    fun getRowTextSizeSp(): Float = prefs.getFloat(KEY_ROW_TEXT_SIZE, 22f)
    fun setRowTextSizeSp(sp: Float) { prefs.edit().putFloat(KEY_ROW_TEXT_SIZE, sp).apply() }

    fun getRowPaddingDp(): Float = prefs.getFloat(KEY_ROW_PADDING, 14f)
    fun setRowPaddingDp(dp: Float) { prefs.edit().putFloat(KEY_ROW_PADDING, dp).apply() }

    fun getAlphabetTextSizeSp(): Float = prefs.getFloat(KEY_ALPHABET_TEXT_SIZE, 32f)
    fun setAlphabetTextSizeSp(sp: Float) { prefs.edit().putFloat(KEY_ALPHABET_TEXT_SIZE, sp).apply() }

    // ---- Gesture sensitivity ----
    fun getSwipeThresholdDp(): Float = prefs.getFloat(KEY_SWIPE_THRESHOLD, 120f)
    fun setSwipeThresholdDp(dp: Float) { prefs.edit().putFloat(KEY_SWIPE_THRESHOLD, dp).apply() }

    fun getDoubleTapWindowMs(): Long = prefs.getLong(KEY_DOUBLE_TAP_WINDOW, 300L)
    fun setDoubleTapWindowMs(ms: Long) { prefs.edit().putLong(KEY_DOUBLE_TAP_WINDOW, ms).apply() }

    // ---- Wallpaper rotation interval, in hours ----
    fun getWallpaperIntervalHours(): Int = prefs.getInt(KEY_WALLPAPER_INTERVAL, 24)
    fun setWallpaperIntervalHours(hours: Int) { prefs.edit().putInt(KEY_WALLPAPER_INTERVAL, hours).apply() }

    // ---- Show app icons (off by default, keeps the "no image assets" minimalism) ----
    fun isShowIcons(): Boolean = prefs.getBoolean(KEY_SHOW_ICONS, false)
    fun setShowIcons(show: Boolean) { prefs.edit().putBoolean(KEY_SHOW_ICONS, show).apply() }

    // ---- Notification preview on favorites (off by default — needs a special,
    //      sensitive system permission the user must grant separately) ----
    fun isNotificationPreviewEnabled(): Boolean = prefs.getBoolean(KEY_NOTIF_PREVIEW, false)
    fun setNotificationPreviewEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_NOTIF_PREVIEW, enabled).apply() }

    // ---- Dynamic accent color extracted from the current wallpaper ----
    fun isDynamicAccentEnabled(): Boolean = prefs.getBoolean(KEY_DYNAMIC_ACCENT, true)
    fun setDynamicAccentEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_DYNAMIC_ACCENT, enabled).apply() }

    fun getAccentColor(): Int? =
        if (prefs.contains(KEY_ACCENT_COLOR)) prefs.getInt(KEY_ACCENT_COLOR, 0) else null

    fun setAccentColor(color: Int) { prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply() }

    companion object {
        private const val KEY_FAVORITES = "favorites_order"
        private const val KEY_HIDDEN = "hidden_apps"
        private const val KEY_CUSTOM_LABELS = "custom_labels"
        private const val KEY_GROUPS = "groups"
        private const val KEY_LAUNCH_COUNTS = "launch_counts"
        private const val KEY_SORT_MODE = "sort_mode"
        private const val KEY_MANUAL_DRAWER_ORDER = "manual_drawer_order"
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_CLOCK_TAP_APP = "clock_tap_app"
        private const val KEY_CALENDAR_TAP_APP = "calendar_tap_app"
        private const val KEY_WALLPAPER_FOLDER = "wallpaper_folder_uri"
        private const val KEY_FONT_INDEX = "font_index"
        private const val KEY_LOCK_ENABLED = "lock_on_double_tap_enabled"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_CUSTOM_BG = "custom_bg_color"
        private const val KEY_CUSTOM_TEXT = "custom_text_color"
        private const val KEY_CLOCK_SCALE = "clock_size_scale"
        private const val KEY_ROW_TEXT_SIZE = "row_text_size"
        private const val KEY_ROW_PADDING = "row_padding"
        private const val KEY_ALPHABET_TEXT_SIZE = "alphabet_text_size"
        private const val KEY_SWIPE_THRESHOLD = "swipe_threshold_dp"
        private const val KEY_DOUBLE_TAP_WINDOW = "double_tap_window_ms"
        private const val KEY_WALLPAPER_INTERVAL = "wallpaper_interval_hours"
        private const val KEY_SHOW_ICONS = "show_icons"
        private const val KEY_NOTIF_PREVIEW = "notification_preview_enabled"
        private const val KEY_DYNAMIC_ACCENT = "dynamic_accent_enabled"
        private const val KEY_ACCENT_COLOR = "accent_color"
    }
}
