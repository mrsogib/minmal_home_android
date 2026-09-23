package com.aurawave.launcher.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurawave.launcher.data.AppInfo
import com.aurawave.launcher.gestures.GestureAction
import com.aurawave.launcher.ui.components.AppListItem
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Niagara-style home: the whole home screen IS a scrollable, alphabetically
 * sorted app list (no icon grid), with an A-Z index rail on the right for
 * fast scrolling — plus swipe-up / swipe-down / double-tap gestures on the
 * empty space above the list.
 */
@Composable
fun HomeScreen(
    apps: List<AppInfo>,
    onLaunch: (AppInfo) -> Unit,
    onHide: (AppInfo) -> Unit,
    onAppInfo: (AppInfo) -> Unit,
    onUninstall: (AppInfo) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPressBackground: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var dragAccum by remember { mutableStateOf(0f) }

    val letters = remember(apps) {
        apps.map { it.label.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
    }
    val sectionStarts = remember(letters) {
        val map = linkedMapOf<String, Int>()
        letters.forEachIndexed { index, letter -> map.putIfAbsent(letter, index) }
        map
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragAccum < -120) onSwipeUp()
                        if (dragAccum > 120) onSwipeDown()
                        dragAccum = 0f
                    }
                ) { _, dragAmount ->
                    dragAccum += dragAmount
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = { onLongPressBackground() }
                )
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(0.92f),
                contentPadding = PaddingValues(top = 64.dp, bottom = 48.dp)
            ) {
                itemsIndexed(apps, key = { _, app -> app.componentKey }) { _, app ->
                    AppListItem(
                        app = app,
                        onClick = { onLaunch(app) },
                        onHide = { onHide(app) },
                        onAppInfo = { onAppInfo(app) },
                        onUninstall = { onUninstall(app) }
                    )
                }
            }

            // A-Z fast-scroll rail
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 64.dp, horizontal = 4.dp)
                    .pointerInput(sectionStarts, letters) {
                        detectTapGestures { offset ->
                            val rows = sectionStarts.keys.size
                            if (rows == 0) return@detectTapGestures
                            val rowHeight = size.height / rows.toFloat()
                            val index = (offset.y / rowHeight).toInt().coerceIn(0, rows - 1)
                            val letter = sectionStarts.keys.elementAt(index)
                            val target = sectionStarts[letter] ?: return@detectTapGestures
                            scope.launch { listState.scrollToItem(target) }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                sectionStarts.keys.forEach { letter ->
                    Text(
                        text = letter,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
