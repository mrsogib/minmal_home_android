package com.sogib.minimallauncher

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.LruCache

/**
 * Icons are loaded from PackageManager only when the "show icons" setting is on,
 * and cached here so scrolling the drawer doesn't reload the same icon repeatedly.
 * Nothing is stored on disk or bundled with the app.
 */
object IconCache {
    private val cache = LruCache<String, Drawable>(200)

    fun get(context: Context, packageName: String): Drawable? {
        cache.get(packageName)?.let { return it }
        return try {
            val icon = context.packageManager.getApplicationIcon(packageName)
            cache.put(packageName, icon)
            icon
        } catch (e: Exception) {
            null
        }
    }

    fun clear() = cache.evictAll()
}
