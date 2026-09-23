package com.aurawave.launcher

import android.app.Application

class LauncherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}




// package com.aurawave.launcher

// import android.app.Application
// import com.aurawave.launcher.data.IconPackManager

// class LauncherApplication : Application() {
//     lateinit var iconPackManager: IconPackManager
//         private set

//     override fun onCreate() {
//         super.onCreate()
//         iconPackManager = IconPackManager(this)
//     }
// }
