package com.sogib.minimallauncher

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette

/**
 * Reads whatever wallpaper is currently set (no special permission needed for a
 * launcher to peek its own device's wallpaper) and picks a vibrant swatch from it
 * to use as an accent color across the UI. Falls back to null if there's no
 * wallpaper drawable available (e.g. a live wallpaper), in which case callers
 * should keep using the plain theme color.
 */
object AccentColorHelper {

    fun extractAccentColor(context: Context): Int? {
        return try {
            val wm = WallpaperManager.getInstance(context)
            val drawable = wm.drawable ?: return null
            val bitmap: Bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                drawable.toBitmap(width = 200, height = 200)
            }
            val palette = Palette.from(bitmap).apply { if (bitmap.width > 400) resizeBitmapArea(400 * 400) }.generate()
            palette.vibrantSwatch?.rgb
                ?: palette.lightVibrantSwatch?.rgb
                ?: palette.mutedSwatch?.rgb
                ?: palette.dominantSwatch?.rgb
        } catch (e: Exception) {
            null
        }
    }

    /** A readable text/foreground color against the given accent, for use as a highlight. */
    fun readableForeground(accent: Int): Int {
        val luminance = (0.299 * Color.red(accent) + 0.587 * Color.green(accent) + 0.114 * Color.blue(accent)) / 255
        return if (luminance > 0.6) Color.BLACK else Color.WHITE
    }
}
