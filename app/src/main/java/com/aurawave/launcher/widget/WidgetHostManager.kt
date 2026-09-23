// package com.aurawave.launcher.widget

// import android.appwidget.AppWidgetHost
// import android.appwidget.AppWidgetHostView
// import android.appwidget.AppWidgetManager
// import android.content.Context
// import androidx.datastore.preferences.core.edit
// import androidx.datastore.preferences.core.stringSetPreferencesKey
// import com.aurawave.launcher.data.dataStore
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.map

// private const val HOST_ID = 1024
// private val WIDGET_IDS_KEY = stringSetPreferencesKey("widget_ids")

// /**
//  * Thin wrapper around AppWidgetHost/AppWidgetManager — the two system
//  * classes any launcher needs to actually embed other apps' widgets.
//  *
//  * Usage from an Activity:
//  *   val manager = WidgetHostManager(context)
//  *   override fun onStart() { manager.host.startListening() }
//  *   override fun onStop()  { manager.host.stopListening() }
//  *
//  * Picking a widget is a two-step OS dance (ACTION_APPWIDGET_PICK, then
//  * possibly ACTION_APPWIDGET_CONFIGURE) — see WidgetPickerActivity /
//  * WidgetConfigureActivity for that flow.
//  */
// class WidgetHostManager(private val context: Context) {

//     val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
//     val host: AppWidgetHost = AppWidgetHost(context, HOST_ID)

//     fun allocateWidgetId(): Int = host.allocateAppWidgetId()

//     fun deleteWidgetId(id: Int) {
//         host.deleteAppWidgetId(id)
//     }

//     fun createWidgetView(widgetId: Int): AppWidgetHostView? {
//         val info = appWidgetManager.getAppWidgetInfo(widgetId) ?: return null
//         return host.createView(context, widgetId, info)
//     }

//     /** Persisted set of widget ids currently placed on the home screen. */
//     val savedWidgetIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
//         prefs[WIDGET_IDS_KEY]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
//     }

//     suspend fun saveWidgetId(id: Int) {
//         context.dataStore.edit { prefs ->
//             val current = prefs[WIDGET_IDS_KEY] ?: emptySet()
//             prefs[WIDGET_IDS_KEY] = current + id.toString()
//         }
//     }

//     suspend fun removeWidgetId(id: Int) {
//         context.dataStore.edit { prefs ->
//             val current = prefs[WIDGET_IDS_KEY] ?: emptySet()
//             prefs[WIDGET_IDS_KEY] = current - id.toString()
//         }
//         deleteWidgetId(id)
//     }
// }










package com.aurawave.launcher.widget

import android.content.Context

class WidgetHostManager(private val context: Context)
