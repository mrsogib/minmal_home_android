package com.sogib.minimallauncher

/**
 * One installed, launchable app. No icon is cached here on purpose — the whole
 * point of "no image assets" is that we draw everything as text (labels only),
 * which keeps the app's memory/storage footprint small.
 */
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val systemLabel: String,
    var customLabel: String? = null,
    var isHidden: Boolean = false,
    var launchCount: Int = 0
) {
    val displayLabel: String
        get() = customLabel ?: systemLabel

    val sortLetter: Char
        get() = displayLabel.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '#'
}
