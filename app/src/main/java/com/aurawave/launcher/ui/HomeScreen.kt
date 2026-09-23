// package com.aurawave.launcher.ui

// import androidx.compose.foundation.gestures.detectVerticalDragGestures
// import androidx.compose.foundation.layout.Box
// import androidx.compose.foundation.layout.Column
// import androidx.compose.foundation.layout.PaddingValues
// import androidx.compose.foundation.layout.Row
// import androidx.compose.foundation.layout.fillMaxHeight
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.lazy.LazyColumn
// import androidx.compose.foundation.lazy.itemsIndexed
// import androidx.compose.foundation.lazy.rememberLazyListState
// import androidx.compose.foundation.text.detectTapGestures
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember 
// import androidx.compose.runtime.rememberCoroutineScope
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.input.pointer.pointerInput
// import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp
// import com.aurawave.launcher.data.AppInfo
// import com.aurawave.launcher.gestures.GestureAction
// import com.aurawave.launcher.ui.components.AppListItem
// import kotlinx.coroutines.launch
// import kotlin.math.abs

// /**
//  * Niagara-style home: the whole home screen IS a scrollable, alphabetically
//  * sorted app list (no icon grid), with an A-Z index rail on the right for
//  * fast scrolling — plus swipe-up / swipe-down / double-tap gestures on the
//  * empty space above the list.
//  */
// @Composable
// fun HomeScreen(
//     apps: List<AppInfo>,
//     onLaunch: (AppInfo) -> Unit,
//     onHide: (AppInfo) -> Unit,
//     onAppInfo: (AppInfo) -> Unit,
//     onUninstall: (AppInfo) -> Unit,
//     onSwipeUp: () -> Unit,
//     onSwipeDown: () -> Unit,
//     onDoubleTap: () -> Unit,
//     onLongPressBackground: () -> Unit
// ) {
//     val listState = rememberLazyListState()
//     val scope = rememberCoroutineScope()
//     var dragAccum by remember { mutableStateOf(0f) }

//     val letters = remember(apps) {
//         apps.map { it.label.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
//     }
//     val sectionStarts = remember(letters) {
//         val map = linkedMapOf<String, Int>()
//         letters.forEachIndexed { index, letter -> map.putIfAbsent(letter, index) }
//         map
//     }

//     Box(
//         modifier = Modifier
//             .fillMaxSize()
//             .pointerInput(Unit) {
//                 detectVerticalDragGestures(
//                     onDragEnd = {
//                         if (dragAccum < -120) onSwipeUp()
//                         if (dragAccum > 120) onSwipeDown()
//                         dragAccum = 0f
//                     }
//                 ) { _, dragAmount ->
//                     dragAccum += dragAmount
//                 }
//             }
//             .pointerInput(Unit) {
//                 detectTapGestures(
//                     onDoubleTap = { onDoubleTap() },
//                     onLongPress = { onLongPressBackground() }
//                 )
//             }
//     ) {
//         Row(modifier = Modifier.fillMaxSize()) {
//             LazyColumn(
//                 state = listState,
//                 modifier = Modifier.fillMaxWidth(0.92f),
//                 contentPadding = PaddingValues(top = 64.dp, bottom = 48.dp)
//             ) {
//                 itemsIndexed(apps, key = { _, app -> app.componentKey }) { _, app ->
//                     AppListItem(
//                         app = app,
//                         onClick = { onLaunch(app) },
//                         onHide = { onHide(app) },
//                         onAppInfo = { onAppInfo(app) },
//                         onUninstall = { onUninstall(app) }
//                     )
//                 }
//             }

//             // A-Z fast-scroll rail
//             Column(
//                 modifier = Modifier
//                     .fillMaxHeight()
//                     .padding(vertical = 64.dp, horizontal = 4.dp)
//                     .pointerInput(sectionStarts, letters) {
//                         detectTapGestures { offset ->
//                             val rows = sectionStarts.keys.size
//                             if (rows == 0) return@detectTapGestures
//                             val rowHeight = size.height / rows.toFloat()
//                             val index = (offset.y / rowHeight).toInt().coerceIn(0, rows - 1)
//                             val letter = sectionStarts.keys.elementAt(index)
//                             val target = sectionStarts[letter] ?: return@detectTapGestures
//                             scope.launch { listState.scrollToItem(target) }
//                         }
//                     },
//                 horizontalAlignment = Alignment.CenterHorizontally
//             ) {
//                 sectionStarts.keys.forEach { letter ->
//                     Text(
//                         text = letter,
//                         fontSize = 10.sp,
//                         color = MaterialTheme.colorScheme.onBackground,
//                         modifier = Modifier.padding(vertical = 1.dp)
//                     )
//                 }
//             }
//         }
//     }
// }









package com.aurawave.launcher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aurawave.launcher.data.AppInfo
import com.aurawave.launcher.data.ThemePreferences
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    favoriteApps: List<AppInfo>,
    prefs: ThemePreferences,
    wallpaperUri: Uri?,
    onAppClick: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHidden: () -> Unit,
    onLeftSwipe: () -> Unit,
    onRightSwipe: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDownLeft: () -> Unit,
    onSwipeDownRight: () -> Unit,
    onDoubleTap: () -> Unit,
    onAlphabetSelect: (Char) -> Unit,
    availableAlphabets: List<Char>
) {
    val context = LocalContext.current
    var batteryPercentage by remember { mutableStateOf(100) }
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var activeAlphabetIndex by remember { mutableStateOf<Int?>(null) }

    // Listen to system intents for battery level updates and clock ticks
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    batteryPercentage = (level * 100) / scale
                }
                currentTime = Calendar.getInstance()
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED).apply {
            addAction(Intent.ACTION_TIME_TICK)
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Render local custom wallpaper if Uri is available
        wallpaperUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gesture Detection Overlay Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Double tap gesture detector
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onDoubleTap() }
                    )
                }
                // Pinch in / Pinch out gesture detector
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom < 0.85f) onOpenSettings() // Pinch in -> Settings
                        else if (zoom > 1.15f) onOpenHidden() // Pinch out -> Hidden Apps
                    }
                }
                // Swipe gesture detector
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val (x, y) = dragAmount
                        if (kotlin.math.abs(x) > kotlin.math.abs(y)) {
                            if (x > 50) onRightSwipe() else if (x < -50) onLeftSwipe()
                        } else {
                            if (y < -50) onSwipeUp()
                            else if (y > 50) {
                                // Left half swipe down = Quick Settings, Right half swipe down = Notifications
                                if (change.position.x < size.width / 2) onSwipeDownLeft() else onSwipeDownRight()
                            }
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 48.dp)
            ) {
                // Top Widget Section
                ClockWidget(
                    currentTime = currentTime,
                    battery = batteryPercentage,
                    clockStyle = prefs.clockStyle,
                    onClockClick = { prefs.clockTargetApp?.let(onAppClick) },
                    onCalendarClick = { prefs.calendarTargetApp?.let(onAppClick) }
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "FAVORITES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Favorite Apps List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(favoriteApps.take(15)) { app ->
                        Text(
                            text = app.label,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppClick(app.packageName) }
                        )
                    }
                }
            }

            // Right-side Niagara Alphabet Rail with Dynamic Wave Scaling Animation
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .pointerInput(availableAlphabets) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val idx = (offset.y / (size.height / availableAlphabets.size.coerceAtLeast(1))).toInt()
                                if (idx in availableAlphabets.indices) {
                                    activeAlphabetIndex = idx
                                    onAlphabetSelect(availableAlphabets[idx])
                                }
                            },
                            onDragEnd = { activeAlphabetIndex = null },
                            onDragCancel = { activeAlphabetIndex = null },
                            onDrag = { change, _ ->
                                val idx = (change.position.y / (size.height / availableAlphabets.size.coerceAtLeast(1))).toInt()
                                if (idx in availableAlphabets.indices && idx != activeAlphabetIndex) {
                                    activeAlphabetIndex = idx
                                    onAlphabetSelect(availableAlphabets[idx])
                                }
                            }
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                availableAlphabets.forEachIndexed { index, char ->
                    val distance = activeAlphabetIndex?.let { kotlin.math.abs(it - index) } ?: 99
                    
                    // Wave magnification scale factor based on distance from finger position
                    val scaleFactor by animateFloatAsState(
                        targetValue = when (distance) {
                            0 -> 2.2f // Active letter magnification
                            1 -> 1.5f // Adjacent letter slight magnification
                            2 -> 1.2f
                            else -> 1.0f
                        },
                        label = "wave_scale"
                    )

                    Text(
                        text = char.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (distance == 0) Color.White else Color.LightGray.copy(alpha = 0.8f),
                        modifier = Modifier
                            .scale(scaleFactor)
                            .clickable { onAlphabetSelect(char) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// Clock Widget Composable supporting both standard and Niagara stacked time displays
@Composable
fun ClockWidget(
    currentTime: Calendar,
    battery: Int,
    clockStyle: Int,
    onClockClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(currentTime.time)

    Column {
        if (clockStyle == 0) {
            // Style 0: Standard inline clock layout
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(currentTime.time)
            Text(
                text = timeStr,
                fontSize = 42.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                color = Color.White,
                modifier = Modifier.clickable { onClockClick() }
            )
        } else {
            // Style 1: Niagara Stacked clock layout (Large hour, small minutes above AM/PM)
            val hourStr = SimpleDateFormat("hh", Locale.getDefault()).format(currentTime.time)
            val minStr = SimpleDateFormat("mm", Locale.getDefault()).format(currentTime.time)
            val amPmStr = SimpleDateFormat("a", Locale.getDefault()).format(currentTime.time)

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.clickable { onClockClick() }
            ) {
                Text(
                    text = hourStr,
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = minStr,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = amPmStr,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.LightGray
                    )
                }
            }
        }

        // Date & Battery percentage indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = dateStr,
                fontSize = 14.sp,
                color = Color.LightGray,
                modifier = Modifier.clickable { onCalendarClick() }
            )
            Text(
                text = " • $battery%",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
