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



package com.aurawave.launcher.data

data class AppInfo(
    val label: String,
    val packageName: String
)
