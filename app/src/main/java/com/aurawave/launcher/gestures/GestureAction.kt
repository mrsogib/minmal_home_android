// package com.aurawave.launcher.gestures

// /** The set of actions a swipe or tap can be bound to in Settings. */
// enum class GestureAction(val id: String, val label: String) {
//     APP_DRAWER("APP_DRAWER", "Open app drawer"),
//     SEARCH("SEARCH", "Open search"),
//     NOTIFICATIONS("NOTIFICATIONS", "Expand notifications"),
//     LOCK_SCREEN("LOCK_SCREEN", "Lock screen"),
//     RECENTS("RECENTS", "Recent apps"),
//     NONE("NONE", "Nothing");

//     companion object {
//         fun fromId(id: String) = entries.find { it.id == id } ?: NONE
//     }
// }




package com.aurawave.launcher.gestures

// Sealed interface defining all supported gesture actions across the launcher UI
sealed interface GestureAction {
    data class LaunchApp(val packageName: String) : GestureAction
    object ExpandNotifications : GestureAction
    object ExpandQuickSettings : GestureAction
    object LockScreen : GestureAction
    object OpenSettings : GestureAction
    object OpenHiddenApps : GestureAction
    object None : GestureAction
}
