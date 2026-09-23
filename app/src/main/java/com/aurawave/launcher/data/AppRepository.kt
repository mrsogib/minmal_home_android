package com.aurawave.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads every launchable activity on the device (PackageManager query) and
 * decorates each with an icon — either from the active icon pack (if any
 * mapping exists for that component) or the app's own icon.
 */
class AppRepository(
    private val context: Context,
    private val iconPackManager: IconPackManager
) {
    suspend fun loadApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        resolveInfos.map { ri ->
            val pkg = ri.activityInfo.packageName
            val activity = ri.activityInfo.name
            val label = ri.loadLabel(pm).toString()
            val icon = iconPackManager.getIcon(pkg, activity) ?: ri.loadIcon(pm)
            AppInfo(label = label, packageName = pkg, activityName = activity, icon = icon)
        }.distinctBy { it.componentKey }
         .sortedBy { it.label.lowercase() }
    }

    fun launchIntentFor(app: AppInfo): Intent {
        return Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = android.content.ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun appInfoIntentFor(app: AppInfo): Intent {
        return Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${app.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun uninstallIntentFor(app: AppInfo): Intent {
        return Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.parse("package:${app.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
