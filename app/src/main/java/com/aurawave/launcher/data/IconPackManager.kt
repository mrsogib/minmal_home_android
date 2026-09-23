// package com.aurawave.launcher.data

// import android.content.Context
// import android.content.Intent
// import android.content.res.XmlResourceParser
// import android.graphics.drawable.Drawable
// import org.xmlpull.v1.XmlPullParser

// /**
//  * Support for third-party icon packs (ADW / Nova / Apex style "appfilter.xml"
//  * convention, which most icon packs on the Play Store still follow).
//  *
//  * Limitations to know about:
//  *  - Only static <item component=".." drawable=".."/> mappings are parsed.
//  *    <calendar> tags (icon packs that swap icons based on the day of month)
//  *    and <iconback>/<iconmask>/<iconupon> masking are not implemented yet —
//  *    that's a reasonable v2 addition once the base app is working.
//  *  - If appfilter.xml is missing/malformed we silently fall back to the
//  *    app's own icon rather than crashing.
//  */
// class IconPackManager(private val context: Context) {

//     private var iconPackPackage: String? = null
//     private val componentToDrawable = mutableMapOf<String, String>()

//     fun currentPack(): String? = iconPackPackage

//     fun setIconPack(packageName: String?) {
//         iconPackPackage = packageName
//         componentToDrawable.clear()
//         if (packageName != null) parseAppFilter(packageName)
//     }

//     private fun parseAppFilter(packageName: String) {
//         try {
//             val res = context.packageManager.getResourcesForApplication(packageName)
//             val id = res.getIdentifier("appfilter", "xml", packageName)
//             if (id == 0) return
//             val parser: XmlResourceParser = res.getXml(id)
//             var eventType = parser.eventType
//             while (eventType != XmlPullParser.END_DOCUMENT) {
//                 if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
//                     val component = parser.getAttributeValue(null, "component")
//                     val drawable = parser.getAttributeValue(null, "drawable")
//                     if (component != null && drawable != null) {
//                         val key = component.removePrefix("ComponentInfo{").removeSuffix("}")
//                         componentToDrawable[key] = drawable
//                     }
//                 }
//                 eventType = parser.next()
//             }
//         } catch (e: Exception) {
//             // Icon pack missing or its appfilter.xml is malformed — fall back to
//             // default app icons for everything instead of crashing the home screen.
//         }
//     }

//     fun getIcon(packageName: String, activityName: String): Drawable? {
//         val pack = iconPackPackage ?: return null
//         val key = "$packageName/$activityName"
//         val drawableName = componentToDrawable[key] ?: return null
//         return try {
//             val res = context.packageManager.getResourcesForApplication(pack)
//             val resId = res.getIdentifier(drawableName, "drawable", pack)
//             if (resId != 0) res.getDrawable(resId, null) else null
//         } catch (e: Exception) {
//             null
//         }
//     }

//     /** Any app on the device that declares itself as an icon pack via the
//      * standard theme-picker intents used by ADW/Nova/Apex/etc. */
//     fun findInstalledIconPacks(): List<IconPackEntry> {
//         val pm = context.packageManager
//         val actions = listOf(
//             "org.adw.launcher.THEMES",
//             "com.novalauncher.THEME",
//             "com.anddoes.launcher.THEME",
//             "com.teslacoilsw.launcher.THEME"
//         )
//         val found = linkedMapOf<String, String>()
//         for (action in actions) {
//             val results = pm.queryIntentActivities(Intent(action), 0)
//             for (r in results) {
//                 found[r.activityInfo.packageName] = r.loadLabel(pm).toString()
//             }
//         }
//         return found.map { IconPackEntry(it.key, it.value) }
//     }
// }

// data class IconPackEntry(val packageName: String, val label: String)




package com.aurawave.launcher.data

import android.content.Context

class IconPackManager(private val context: Context) {
    // Minimalist launcher runs text-only; helper class preserved for future extensions
}

