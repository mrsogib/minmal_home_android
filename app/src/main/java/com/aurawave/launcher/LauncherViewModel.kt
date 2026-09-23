// package com.aurawave.launcher

// import android.app.Application
// import androidx.datastore.preferences.core.stringSetPreferencesKey
// import androidx.lifecycle.AndroidViewModel
// import androidx.lifecycle.viewModelScope
// import com.aurawave.launcher.data.AppInfo
// import com.aurawave.launcher.data.AppRepository
// import com.aurawave.launcher.data.ThemePreferences
// import com.aurawave.launcher.data.dataStore
// import androidx.datastore.preferences.core.edit
// import kotlinx.coroutines.flow.MutableStateFlow
// import kotlinx.coroutines.flow.StateFlow
// import kotlinx.coroutines.flow.asStateFlow
// import kotlinx.coroutines.flow.first
// import kotlinx.coroutines.launch

// private val HIDDEN_APPS_KEY = stringSetPreferencesKey("hidden_apps")

// enum class Screen { HOME, DRAWER, SETTINGS }

// class LauncherViewModel(application: Application) : AndroidViewModel(application) {

//     private val appContext = application.applicationContext
//     private val iconPackManager = (application as LauncherApplication).iconPackManager
//     private val repository = AppRepository(appContext, iconPackManager)
//     val themePreferences = ThemePreferences(appContext)

//     private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
//     val visibleApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

//     private val _hiddenPackages = MutableStateFlow<Set<String>>(emptySet())

//     private val _screen = MutableStateFlow(Screen.HOME)
//     val screen: StateFlow<Screen> = _screen.asStateFlow()

//     private val _searchQuery = MutableStateFlow("")
//     val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

//     init {
//         viewModelScope.launch {
//             _hiddenPackages.value = appContext.dataStore.data.first()[HIDDEN_APPS_KEY] ?: emptySet()
//             refreshApps()
//         }
//     }

//     fun refreshApps() {
//         viewModelScope.launch {
//             val icon = themePreferences.iconPack.first()
//             iconPackManager.setIconPack(icon)
//             _allApps.value = repository.loadApps()
//         }
//     }

//     fun homeApps(): List<AppInfo> =
//         visibleApps.value.filter { it.packageName !in _hiddenPackages.value }

//     fun drawerApps(query: String): List<AppInfo> {
//         val apps = visibleApps.value.filter { it.packageName !in _hiddenPackages.value }
//         if (query.isBlank()) return apps
//         return apps.filter { it.label.contains(query, ignoreCase = true) }
//     }

//     fun setScreen(screen: Screen) {
//         _screen.value = screen
//     }

//     fun setSearchQuery(query: String) {
//         _searchQuery.value = query
//     }

//     fun hideApp(app: AppInfo) {
//         viewModelScope.launch {
//             val updated = _hiddenPackages.value + app.packageName
//             _hiddenPackages.value = updated
//             appContext.dataStore.edit { it[HIDDEN_APPS_KEY] = updated }
//         }
//     }

//     fun unhideApp(packageName: String) {
//         viewModelScope.launch {
//             val updated = _hiddenPackages.value - packageName
//             _hiddenPackages.value = updated
//             appContext.dataStore.edit { it[HIDDEN_APPS_KEY] = updated }
//         }
//     }

//     fun hiddenApps(): List<AppInfo> =
//         visibleApps.value.filter { it.packageName in _hiddenPackages.value }

//     fun launch(app: AppInfo) {
//         appContext.startActivity(repository.launchIntentFor(app))
//     }

//     fun openAppInfo(app: AppInfo) {
//         appContext.startActivity(repository.appInfoIntentFor(app))
//     }

//     fun uninstall(app: AppInfo) {
//         appContext.startActivity(repository.uninstallIntentFor(app))
//     }
// }





package com.aurawave.launcher

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aurawave.launcher.data.AppInfo
import com.aurawave.launcher.data.AppRepository
import com.aurawave.launcher.data.ThemePreferences
import com.aurawave.launcher.gestures.GestureAction
import com.aurawave.launcher.gestures.GestureExecutor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = ThemePreferences(application)
    private val repository = AppRepository(application)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps: StateFlow<List<AppInfo>> = _allApps

    private val _favoriteApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val favoriteApps: StateFlow<List<AppInfo>> = _favoriteApps

    private val _availableAlphabets = MutableStateFlow<List<Char>>(emptyList())
    val availableAlphabets: StateFlow<List<Char>> = _availableAlphabets

    private val _currentWallpaperUri = MutableStateFlow<Uri?>(null)
    val currentWallpaperUri: StateFlow<Uri?> = _currentWallpaperUri

    private var wallpaperTimerJob: Job? = null

    init {
        loadApps(application)
        initWallpaperSlideshow()
    }

    fun loadApps(context: Context) {
        viewModelScope.launch {
            val rawApps = repository.getInstalledApps()
            val processedApps = rawApps.map { app ->
                val customLabel = prefs.getCustomLabel(app.packageName)
                if (customLabel != null) app.copy(label = customLabel) else app
            }.filter { !prefs.isAppHidden(it.packageName) }

            _allApps.value = when (prefs.drawerSortMode) {
                1 -> processedApps.sortedByDescending { prefs.getUsageCount(it.packageName) }
                else -> processedApps.sortedBy { it.label.lowercase() }
            }

            _availableAlphabets.value = _allApps.value
                .map { it.label.firstOrNull()?.uppercaseChar() ?: '#' }
                .distinct()
                .sorted()

            val favPackages = prefs.favoriteAppPackages
            _favoriteApps.value = if (favPackages.isNotEmpty()) {
                favPackages.mapNotNull { pkg -> _allApps.value.find { it.packageName == pkg } }
            } else {
                _allApps.value.take(12)
            }
        }
    }

    fun renameApp(packageName: String, newName: String) {
        prefs.setCustomLabel(packageName, newName.ifBlank { null })
        loadApps(getApplication())
    }

    fun launchApp(context: Context, packageName: String) {
        prefs.incrementUsageCount(packageName)
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun getHiddenApps(): List<AppInfo> {
        val rawApps = repository.getInstalledApps()
        return rawApps.filter { prefs.isAppHidden(it.packageName) }
    }

    fun setupWallpaperFolder(folderUriStr: String, intervalMinutes: Long) {
        prefs.wallpaperFolderUri = folderUriStr
        prefs.wallpaperIntervalMinutes = intervalMinutes
        initWallpaperSlideshow()
    }

    private fun initWallpaperSlideshow() {
        wallpaperTimerJob?.cancel()
        val uriStr = prefs.wallpaperFolderUri ?: return
        val minutes = prefs.wallpaperIntervalMinutes

        wallpaperTimerJob = viewModelScope.launch {
            while (isActive) {
                pickRandomWallpaper(uriStr)
                if (minutes <= 0) break
                delay(minutes * 60 * 1000L)
            }
        }
    }

    private fun pickRandomWallpaper(folderUriStr: String) {
        try {
            val context = getApplication<Application>()
            val folderUri = Uri.parse(folderUriStr)
            val dir = DocumentFile.fromTreeUri(context, folderUri)
            val files = dir?.listFiles()?.filter { it.type?.startsWith("image/") == true }
            if (!files.isNullOrEmpty()) {
                _currentWallpaperUri.value = files.random().uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun executeSwipeLeft(executor: GestureExecutor, onSettings: () -> Unit, onHidden: () -> Unit) {
        val target = prefs.swipeLeftApp
        if (target != null) executor.execute(GestureAction.LaunchApp(target), onSettings, onHidden)
    }

    fun executeSwipeRight(executor: GestureExecutor, onSettings: () -> Unit, onHidden: () -> Unit) {
        val target = prefs.swipeRightApp
        if (target != null) executor.execute(GestureAction.LaunchApp(target), onSettings, onHidden)
    }

    fun executeSwipeUp(executor: GestureExecutor, onSettings: () -> Unit, onHidden: () -> Unit) {
        val target = prefs.swipeUpApp
        if (target != null) executor.execute(GestureAction.LaunchApp(target), onSettings, onHidden)
    }
}



