package com.aurawave.launcher.data

import android.graphics.drawable.Drawable

// Data class representing a single installed application entity
data class AppInfo(
    val label: String,          // Display name of the app (e.g., "Camera")
    val packageName: String,    // Unique package identifier (e.g., "com.android.camera")
    val icon: Drawable? = null  // App icon graphic loaded from the system
)



// package com.aurawave.launcher.data

// import android.graphics.drawable.Drawable

// data class AppInfo(
//     val label: String,
//     val packageName: String,
//     val activityName: String,
//     val icon: Drawable,
//     val isHidden: Boolean = false
// ) {
//     val componentKey: String get() = "$packageName/$activityName"
// }
