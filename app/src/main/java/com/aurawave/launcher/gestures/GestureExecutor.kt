package com.aurawave.launcher.gestures

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import java.lang.reflect.Method

// Handles low-level Android system actions triggered by touch gestures
class GestureExecutor(private val context: Context) {

    fun execute(action: GestureAction, onOpenSettings: () -> Unit, onOpenHidden: () -> Unit) {
        when (action) {
            is GestureAction.LaunchApp -> launchApp(action.packageName)
            is GestureAction.ExpandNotifications -> expandStatusBar("expandNotificationsPanel")
            is GestureAction.ExpandQuickSettings -> expandStatusBar("expandSettingsPanel")
            is GestureAction.LockScreen -> lockScreen()
            is GestureAction.OpenSettings -> onOpenSettings()
            is GestureAction.OpenHiddenApps -> onOpenHidden()
            is GestureAction.None -> {}
        }
    }

    private fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    // Uses Java reflection to invoke hidden status bar expand methods
    @SuppressLint("WrongConstant")
    fun expandStatusBar(methodName: String) {
        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method: Method = statusBarManager.getMethod(methodName)
            method.invoke(statusBarService)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Locks device screen using Android Device Policy Administrator
    fun lockScreen() {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, AdminReceiver::class.java)
        if (dpm.isAdminActive(adminComponent)) {
            dpm.lockNow()
        } else {
            // Prompt user to grant Device Admin permission if not yet enabled
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to lock screen on double tap.")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}





// package com.aurawave.launcher.gestures
 
// import android.content.Context
// import android.util.Log

// /**
//  * Executes a resolved GestureAction. Two of these — NOTIFICATIONS and
//  * RECENTS/LOCK_SCREEN — reach outside normal app-sandbox permissions, so read
//  * the comments before relying on them:
//  *
//  *  - NOTIFICATIONS: uses the same hidden StatusBarManager reflection trick
//  *    every third-party launcher uses. Works on stock Android; some OEM skins
//  *    (MIUI, some Samsung builds) block it.
//  *  - LOCK_SCREEN: actually locking the screen requires the app to be registered
//  *    as a Device Admin (or use Accessibility Service + performGlobalAction
//  *    on Android 9+). Left as a clearly marked extension point rather than
//  *    silently failing — wire up DevicePolicyManager once you've added the
//  *    device admin receiver described in README.md.
//  *  - RECENTS: same story — needs an Accessibility Service calling
//  *    performGlobalAction(GLOBAL_ACTION_RECENTS) on Android 9+.
//  */
// object GestureExecutor {

//     fun execute(
//         context: Context,
//         action: GestureAction,
//         onOpenDrawer: () -> Unit,
//         onOpenSearch: () -> Unit
//     ) {
//         when (action) {
//             GestureAction.APP_DRAWER -> onOpenDrawer()
//             GestureAction.SEARCH -> onOpenSearch()
//             GestureAction.NOTIFICATIONS -> expandNotifications(context)
//             GestureAction.LOCK_SCREEN -> Log.w(
//                 "GestureExecutor",
//                 "Lock screen requires a Device Admin receiver — see README.md"
//             )
//             GestureAction.RECENTS -> Log.w(
//                 "GestureExecutor",
//                 "Recents requires an Accessibility Service — see README.md"
//             )
//             GestureAction.NONE -> {}
//         }
//     }

//     private fun expandNotifications(context: Context) {
//         try {
//             val service = context.getSystemService("statusbar")
//             val statusBarManager = Class.forName("android.app.StatusBarManager")
//             val method = statusBarManager.getMethod("expandNotificationsPanel")
//             method.invoke(service)
//         } catch (e: Exception) {
//             Log.w("GestureExecutor", "expandNotificationsPanel unavailable on this OEM build", e)
//         }
//     }
// }






