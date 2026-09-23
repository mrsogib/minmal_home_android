package com.aurawave.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "wave_launcher_settings")

private object Keys {
    val DARK_THEME = booleanPreferencesKey("dark_theme")
    val ACCENT_COLOR = intPreferencesKey("accent_color")
    val ICON_PACK = stringPreferencesKey("icon_pack")
    val SWIPE_UP_ACTION = stringPreferencesKey("swipe_up_action")
    val SWIPE_DOWN_ACTION = stringPreferencesKey("swipe_down_action")
    val DOUBLE_TAP_ACTION = stringPreferencesKey("double_tap_action")
}

/** Default accent: a soft indigo, close to Niagara's default palette. */
private const val DEFAULT_ACCENT = 0xFF6C63FF.toInt()

class ThemePreferences(private val context: Context) {

    val darkTheme: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.DARK_THEME] ?: true }

    val accentColor: Flow<Int> =
        context.dataStore.data.map { it[Keys.ACCENT_COLOR] ?: DEFAULT_ACCENT }

    val iconPack: Flow<String?> =
        context.dataStore.data.map { it[Keys.ICON_PACK] }

    val swipeUpAction: Flow<String> =
        context.dataStore.data.map { it[Keys.SWIPE_UP_ACTION] ?: "APP_DRAWER" }

    val swipeDownAction: Flow<String> =
        context.dataStore.data.map { it[Keys.SWIPE_DOWN_ACTION] ?: "NOTIFICATIONS" }

    val doubleTapAction: Flow<String> =
        context.dataStore.data.map { it[Keys.DOUBLE_TAP_ACTION] ?: "LOCK_SCREEN" }

    suspend fun setDarkTheme(enabled: Boolean) =
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }

    suspend fun setAccentColor(color: Int) =
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = color }

    suspend fun setIconPack(pkg: String?) =
        context.dataStore.edit {
            if (pkg == null) it.remove(Keys.ICON_PACK) else it[Keys.ICON_PACK] = pkg
        }

    suspend fun setSwipeUpAction(action: String) =
        context.dataStore.edit { it[Keys.SWIPE_UP_ACTION] = action }

    suspend fun setSwipeDownAction(action: String) =
        context.dataStore.edit { it[Keys.SWIPE_DOWN_ACTION] = action }

    suspend fun setDoubleTapAction(action: String) =
        context.dataStore.edit { it[Keys.DOUBLE_TAP_ACTION] = action }
}
