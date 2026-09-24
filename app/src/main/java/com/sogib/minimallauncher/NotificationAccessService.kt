package com.sogib.minimallauncher

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.concurrent.ConcurrentHashMap

/**
 * Powers the "notification preview under favorite apps" feature. This is entirely
 * opt-in: the service only receives anything once the user explicitly grants
 * notification access in Android's own Settings (Settings.ACTION_NOTIFICATION_
 * LISTENER_SETTINGS) — there is no manifest permission that grants this silently.
 *
 * Previews are kept in memory only (never written to disk) and only the latest
 * per-app text is retained, not a history.
 */
class NotificationAccessService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            activeNotifications?.forEach { store(it) }
        } catch (e: Exception) {
            // ignore — some OEMs restrict this until first post
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        store(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Only clear if it was the most recent one for that package.
        if (previews[sbn.packageName] != null) {
            val extras = sbn.notification.extras
            val text = extras.getCharSequence("android.text")?.toString()
            if (text == null || previews[sbn.packageName] == text) {
                previews.remove(sbn.packageName)
            }
        }
    }

    private fun store(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val combined = listOf(title, text).filter { it.isNotBlank() }.joinToString(": ")
        if (combined.isNotBlank()) {
            previews[sbn.packageName] = combined
        }
    }

    companion object {
        private val previews = ConcurrentHashMap<String, String>()

        fun getPreview(packageName: String): String? = previews[packageName]
    }
}
