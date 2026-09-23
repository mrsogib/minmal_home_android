package com.aurawave.launcher

import android.app.Application
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aurawave.launcher.data.AppInfo
import com.aurawave.launcher.data.AppRepository
import com.aurawave.launcher.data.ThemePreferences
import com.aurawave.launcher.data.dataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val HIDDEN_APPS_KEY = stringSetPreferencesKey("hidden_apps")

enum class Screen { HOME, DRAWER, SETTINGS }

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val iconPackManager = (application as LauncherApplication).iconPackManager
    private val repository = AppRepository(appContext, iconPackManager)
    val themePreferences = ThemePreferences(appContext)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val visibleApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

    private val _hiddenPackages = MutableStateFlow<Set<String>>(emptySet())

    private val _screen = MutableStateFlow(Screen.HOME)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            _hiddenPackages.value = appContext.dataStore.data.first()[HIDDEN_APPS_KEY] ?: emptySet()
            refreshApps()
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            val icon = themePreferences.iconPack.first()
            iconPackManager.setIconPack(icon)
            _allApps.value = repository.loadApps()
        }
    }

    fun homeApps(): List<AppInfo> =
        visibleApps.value.filter { it.packageName !in _hiddenPackages.value }

    fun drawerApps(query: String): List<AppInfo> {
        val apps = visibleApps.value.filter { it.packageName !in _hiddenPackages.value }
        if (query.isBlank()) return apps
        return apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    fun setScreen(screen: Screen) {
        _screen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun hideApp(app: AppInfo) {
        viewModelScope.launch {
            val updated = _hiddenPackages.value + app.packageName
            _hiddenPackages.value = updated
            appContext.dataStore.edit { it[HIDDEN_APPS_KEY] = updated }
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            val updated = _hiddenPackages.value - packageName
            _hiddenPackages.value = updated
            appContext.dataStore.edit { it[HIDDEN_APPS_KEY] = updated }
        }
    }

    fun hiddenApps(): List<AppInfo> =
        visibleApps.value.filter { it.packageName in _hiddenPackages.value }

    fun launch(app: AppInfo) {
        appContext.startActivity(repository.launchIntentFor(app))
    }

    fun openAppInfo(app: AppInfo) {
        appContext.startActivity(repository.appInfoIntentFor(app))
    }

    fun uninstall(app: AppInfo) {
        appContext.startActivity(repository.uninstallIntentFor(app))
    }
}
