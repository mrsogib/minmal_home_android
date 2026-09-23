package com.aurawave.launcher.gestures

import android.content.Context
import android.util.Log

/**
 * Executes a resolved GestureAction. Two of these — NOTIFICATIONS and
 * RECENTS/LOCK_SCREEN — reach outside normal app-sandbox permissions, so read
 * the comments before relying on them:
 *
 *  - NOTIFICATIONS: uses the same hidden StatusBarManager reflection trick
 *    every third-party launcher uses. Works on stock Android; some OEM skins
 *    (MIUI, some Samsung builds) block it.
 *  - LOCK_SCREEN: actually locking the screen requires the app to be registered
 *    as a Device Admin (or use Accessibility Service + performGlobalAction
 *    on Android 9+). Left as a clearly marked extension point rather than
 *    silently failing — wire up DevicePolicyManager once you've added the
 *    device admin receiver described in README.md.
 *  - RECENTS: same story — needs an Accessibility Service calling
 *    performGlobalAction(GLOBAL_ACTION_RECENTS) on Android 9+.
 */
object GestureExecutor {

    fun execute(
        context: Context,
        action: GestureAction,
        onOpenDrawer: () -> Unit,
        onOpenSearch: () -> Unit
    ) {
        when (action) {
            GestureAction.APP_DRAWER -> onOpenDrawer()
            GestureAction.SEARCH -> onOpenSearch()
            GestureAction.NOTIFICATIONS -> expandNotifications(context)
            GestureAction.LOCK_SCREEN -> Log.w(
                "GestureExecutor",
                "Lock screen requires a Device Admin receiver — see README.md"
            )
            GestureAction.RECENTS -> Log.w(
                "GestureExecutor",
                "Recents requires an Accessibility Service — see README.md"
            )
            GestureAction.NONE -> {}
        }
    }

    private fun expandNotifications(context: Context) {
        try {
            val service = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method = statusBarManager.getMethod("expandNotificationsPanel")
            method.invoke(service)
        } catch (e: Exception) {
            Log.w("GestureExecutor", "expandNotificationsPanel unavailable on this OEM build", e)
        }
    }
}
