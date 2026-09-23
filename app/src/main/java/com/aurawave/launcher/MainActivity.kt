package com.aurawave.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aurawave.launcher.data.IconPackEntry
import com.aurawave.launcher.gestures.GestureAction
import com.aurawave.launcher.gestures.GestureExecutor
import com.aurawave.launcher.ui.AppDrawerScreen
import com.aurawave.launcher.ui.HomeScreen
import com.aurawave.launcher.ui.SettingsScreen
import com.aurawave.launcher.ui.theme.WaveLauncherTheme
import com.aurawave.launcher.widget.WidgetHostManager
import com.aurawave.launcher.widget.WidgetPickerActivity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()
    private lateinit var widgetHostManager: WidgetHostManager

    private val addWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* widget id is already persisted by WidgetPickerActivity */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetHostManager = WidgetHostManager(this)

        setContent {
            val darkTheme by viewModel.themePreferences.darkTheme.collectAsState(initial = true)
            val accentColor by viewModel.themePreferences.accentColor.collectAsState(initial = 0xFF6C63FF.toInt())
            val iconPack by viewModel.themePreferences.iconPack.collectAsState(initial = null)
            val swipeUpId by viewModel.themePreferences.swipeUpAction.collectAsState(initial = "APP_DRAWER")
            val swipeDownId by viewModel.themePreferences.swipeDownAction.collectAsState(initial = "NOTIFICATIONS")
            val doubleTapId by viewModel.themePreferences.doubleTapAction.collectAsState(initial = "LOCK_SCREEN")

            val screen by viewModel.screen.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val allApps by viewModel.visibleApps.collectAsState()

            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            // Re-read apps whenever the icon pack changes so icons refresh live.
            LaunchedEffect(iconPack) { viewModel.refreshApps() }

            WaveLauncherTheme(darkTheme = darkTheme, accentColor = Color(accentColor)) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            apps = viewModel.homeApps(),
                            onLaunch = { viewModel.launch(it) },
                            onHide = { viewModel.hideApp(it) },
                            onAppInfo = { viewModel.openAppInfo(it) },
                            onUninstall = { viewModel.uninstall(it) },
                            onSwipeUp = {
                                GestureExecutor.execute(
                                    context, GestureAction.fromId(swipeUpId),
                                    onOpenDrawer = { viewModel.setScreen(Screen.DRAWER) },
                                    onOpenSearch = { viewModel.setScreen(Screen.DRAWER) }
                                )
                            },
                            onSwipeDown = {
                                GestureExecutor.execute(
                                    context, GestureAction.fromId(swipeDownId),
                                    onOpenDrawer = { viewModel.setScreen(Screen.DRAWER) },
                                    onOpenSearch = { viewModel.setScreen(Screen.DRAWER) }
                                )
                            },
                            onDoubleTap = {
                                GestureExecutor.execute(
                                    context, GestureAction.fromId(doubleTapId),
                                    onOpenDrawer = { viewModel.setScreen(Screen.DRAWER) },
                                    onOpenSearch = { viewModel.setScreen(Screen.DRAWER) }
                                )
                            },
                            onLongPressBackground = { viewModel.setScreen(Screen.SETTINGS) }
                        )

                        Screen.DRAWER -> AppDrawerScreen(
                            apps = viewModel.drawerApps(searchQuery),
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            onLaunch = { viewModel.launch(it); viewModel.setScreen(Screen.HOME) },
                            onHide = { viewModel.hideApp(it) },
                            onAppInfo = { viewModel.openAppInfo(it) },
                            onUninstall = { viewModel.uninstall(it) }
                        )

                        Screen.SETTINGS -> {
                            var installedPacks by remember { mutableStateOf(emptyList<IconPackEntry>()) }
                            LaunchedEffect(Unit) {
                                installedPacks = (application as LauncherApplication)
                                    .iconPackManager.findInstalledIconPacks()
                            }
                            SettingsScreen(
                                darkTheme = darkTheme,
                                onDarkThemeChange = { scope.launch { viewModel.themePreferences.setDarkTheme(it) } },
                                accentColor = accentColor,
                                onAccentColorChange = { scope.launch { viewModel.themePreferences.setAccentColor(it) } },
                                installedIconPacks = installedPacks,
                                currentIconPack = iconPack,
                                onIconPackChange = { scope.launch { viewModel.themePreferences.setIconPack(it) } },
                                swipeUp = GestureAction.fromId(swipeUpId),
                                swipeDown = GestureAction.fromId(swipeDownId),
                                doubleTap = GestureAction.fromId(doubleTapId),
                                onSwipeUpChange = { scope.launch { viewModel.themePreferences.setSwipeUpAction(it.id) } },
                                onSwipeDownChange = { scope.launch { viewModel.themePreferences.setSwipeDownAction(it.id) } },
                                onDoubleTapChange = { scope.launch { viewModel.themePreferences.setDoubleTapAction(it.id) } },
                                onAddWidget = {
                                    addWidgetLauncher.launch(Intent(context, WidgetPickerActivity::class.java))
                                },
                                onBack = { viewModel.setScreen(Screen.HOME) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        widgetHostManager.host.startListening()
    }

    override fun onStop() {
        super.onStop()
        widgetHostManager.host.stopListening()
    }

    override fun onResume() {
        super.onResume()
        // Home screens should reflect installs/uninstalls that happened while
        // the launcher was in the background.
        viewModel.refreshApps()
    }

    override fun onBackPressed() {
        // A launcher's home screen should never be "backed out of" — only
        // step back from Drawer/Settings to Home.
        when (viewModel.screen.value) {
            Screen.HOME -> { /* no-op: home has no parent to go back to */ }
            else -> viewModel.setScreen(Screen.HOME)
        }
    }
}
