package com.sogib.minimallauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Single source of truth for "what apps exist and what do we know about them".
 * Reads straight from PackageManager each time — no local app database, so there's
 * nothing to keep in sync when the user installs/uninstalls something.
 */
class AppRepository(private val context: Context) {

    private val prefs = PrefsHelper(context)

    fun getAllApps(includeHidden: Boolean = false): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        val hidden = prefs.getHiddenApps()
        val customLabels = prefs.getCustomLabels()
        val launchCounts = prefs.getLaunchCounts()

        return resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { ri ->
                val pkg = ri.activityInfo.packageName
                AppInfo(
                    packageName = pkg,
                    activityName = ri.activityInfo.name,
                    systemLabel = ri.loadLabel(pm).toString(),
                    customLabel = customLabels[pkg],
                    isHidden = hidden.contains(pkg),
                    launchCount = launchCounts[pkg] ?: 0
                )
            }
            .filter { includeHidden || !it.isHidden }
            .distinctBy { it.packageName }
    }

    fun launchApp(app: AppInfo) {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(app.packageName)
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            prefs.incrementLaunchCount(app.packageName)
        }
    }

    fun sortedForDrawer(apps: List<AppInfo>): List<AppInfo> {
        return when (prefs.getSortMode()) {
            PrefsHelper.SortMode.FREQUENCY ->
                apps.sortedByDescending { it.launchCount }
            PrefsHelper.SortMode.MANUAL -> {
                val order = prefs.getManualDrawerOrder()
                val indexOf = order.withIndex().associate { (i, pkg) -> pkg to i }
                apps.sortedBy { indexOf[it.packageName] ?: Int.MAX_VALUE }
            }
            PrefsHelper.SortMode.ALPHABETICAL ->
                apps.sortedBy { it.displayLabel.lowercase() }
        }
    }

    /** Only the letters actually present, so the index strip never shows an empty letter. */
    fun lettersPresent(apps: List<AppInfo>): List<Char> =
        apps.map { it.sortLetter }.distinct().sorted()
}
