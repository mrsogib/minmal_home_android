package com.sogib.minimallauncher

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile

/**
 * Sets a random wallpaper from a folder the user picked via the system file picker
 * (Storage Access Framework). SAF means we never need READ_EXTERNAL_STORAGE or any
 * other storage permission — the picker itself grants access only to that one folder.
 */
object WallpaperHelper {

    private const val IMAGE_MIME_PREFIX = "image/"

    fun setRandomWallpaperFromFolder(context: Context, treeUriString: String): Boolean {
        val treeUri = Uri.parse(treeUriString)
        val dir = DocumentFile.fromTreeUri(context, treeUri) ?: return false
        val images = dir.listFiles().filter {
            it.isFile && (it.type?.startsWith(IMAGE_MIME_PREFIX) == true)
        }
        if (images.isEmpty()) return false

        val chosen = images.random()
        return try {
            context.contentResolver.openInputStream(chosen.uri)?.use { stream ->
                WallpaperManager.getInstance(context).setStream(stream)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Reschedules the rotation alarm at the given interval; cancels any previous one first. */
    fun scheduleRotation(context: Context, intervalHours: Int) {
        cancelRotation(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WallpaperRotationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val intervalMs = intervalHours * AlarmManager.INTERVAL_HOUR
        am.setInexactRepeating(
            AlarmManager.RTC,
            System.currentTimeMillis() + intervalMs,
            intervalMs,
            pending
        )
    }

    fun cancelRotation(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WallpaperRotationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pending)
    }
}

class WallpaperRotationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PrefsHelper(context)
        val folder = prefs.getWallpaperFolderUri() ?: return
        WallpaperHelper.setRandomWallpaperFromFolder(context, folder)
    }
}
