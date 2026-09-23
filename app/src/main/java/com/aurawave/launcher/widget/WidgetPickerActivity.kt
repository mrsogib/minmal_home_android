// package com.aurawave.launcher.widget

// import android.app.Activity
// import android.appwidget.AppWidgetManager
// import android.content.Intent
// import android.os.Bundle
// import kotlinx.coroutines.runBlocking

// /**
//  * Kicks off the system's built-in widget picker (ACTION_APPWIDGET_PICK).
//  * Launch this with startActivityForResult from MainActivity's "+ Add widget"
//  * button; on RESULT_OK it hands back an EXTRA_APPWIDGET_ID which the caller
//  * should turn into a host view via WidgetHostManager.createWidgetView().
//  *
//  * If the chosen widget needs configuration, the system will separately
//  * launch the widget's own configure Activity for you — you don't need to
//  * do anything extra for that step, Android handles it as part of the
//  * ACTION_APPWIDGET_PICK contract.
//  */
// class WidgetPickerActivity : Activity() {

//     private lateinit var widgetHostManager: WidgetHostManager

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         widgetHostManager = WidgetHostManager(this)
//         val widgetId = widgetHostManager.allocateWidgetId()

//         val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
//             putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
//         }
//         startActivityForResult(pickIntent, REQUEST_PICK_WIDGET)
//     }

//     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//         super.onActivityResult(requestCode, resultCode, data)
//         if (requestCode == REQUEST_PICK_WIDGET) {
//             if (resultCode == RESULT_OK && data != null) {
//                 val widgetId = data.getIntExtra(
//                     AppWidgetManager.EXTRA_APPWIDGET_ID,
//                     AppWidgetManager.INVALID_APPWIDGET_ID
//                 )
//                 runBlocking { widgetHostManager.saveWidgetId(widgetId) }
//                 setResult(RESULT_OK, data)
//             } else {
//                 setResult(RESULT_CANCELED)
//             }
//         }
//         finish()
//     }

//     companion object {
//         private const val REQUEST_PICK_WIDGET = 9001
//     }
// }








package com.aurawave.launcher.widget

import android.os.Bundle
import androidx.activity.ComponentActivity

class WidgetPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
