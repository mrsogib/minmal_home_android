package com.aurawave.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aurawave.launcher.data.IconPackEntry
import com.aurawave.launcher.gestures.GestureAction

private val ACCENT_SWATCHES = listOf(
    0xFF6C63FF.toInt(), // indigo (default)
    0xFFFF6584.toInt(), // coral
    0xFF00BFA6.toInt(), // teal
    0xFFFFB300.toInt(), // amber
    0xFF4FC3F7.toInt(), // sky
    0xFFEF5350.toInt()  // red
)

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    accentColor: Int,
    onAccentColorChange: (Int) -> Unit,
    installedIconPacks: List<IconPackEntry>,
    currentIconPack: String?,
    onIconPackChange: (String?) -> Unit,
    swipeUp: GestureAction,
    swipeDown: GestureAction,
    doubleTap: GestureAction,
    onSwipeUpChange: (GestureAction) -> Unit,
    onSwipeDownChange: (GestureAction) -> Unit,
    onDoubleTapChange: (GestureAction) -> Unit,
    onAddWidget: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        TextButton(onClick = onBack) { Text("← Back to home") }

        SectionTitle("Appearance")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark theme")
            Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
        }

        Text("Accent color", modifier = Modifier.padding(top = 12.dp))
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            ACCENT_SWATCHES.forEach { swatch ->
                ColorSwatch(
                    color = swatch,
                    selected = swatch == accentColor,
                    onClick = { onAccentColorChange(swatch) }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        SectionTitle("Icon pack")
        IconPackDropdown(
            installedIconPacks = installedIconPacks,
            currentIconPack = currentIconPack,
            onIconPackChange = onIconPackChange
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        SectionTitle("Gestures")
        GestureDropdown("Swipe up", swipeUp, onSwipeUpChange)
        GestureDropdown("Swipe down", swipeDown, onSwipeDownChange)
        GestureDropdown("Double tap", doubleTap, onDoubleTapChange)

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        SectionTitle("Widgets")
        TextButton(onClick = onAddWidget) { Text("+ Add a widget to home") }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun ColorSwatch(color: Int, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(if (selected) 36.dp else 28.dp)
            .background(Color(color), shape = androidx.compose.foundation.shape.CircleShape)
            .then(
                if (selected) Modifier.border(
                    2.dp, MaterialTheme.colorScheme.onBackground, androidx.compose.foundation.shape.CircleShape
                ) else Modifier
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun IconPackDropdown(
    installedIconPacks: List<IconPackEntry>,
    currentIconPack: String?,
    onIconPackChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = installedIconPacks.find { it.packageName == currentIconPack }?.label ?: "Default icons"

    Row {
        TextButton(onClick = { expanded = true }) { Text(label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Default icons") }, onClick = {
                expanded = false; onIconPackChange(null)
            })
            installedIconPacks.forEach { pack ->
                DropdownMenuItem(text = { Text(pack.label) }, onClick = {
                    expanded = false; onIconPackChange(pack.packageName)
                })
            }
            if (installedIconPacks.isEmpty()) {
                DropdownMenuItem(text = { Text("No icon packs installed") }, onClick = { expanded = false }, enabled = false)
            }
        }
    }
}

@Composable
private fun GestureDropdown(label: String, current: GestureAction, onChange: (GestureAction) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        TextButton(onClick = { expanded = true }) { Text(current.label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GestureAction.entries.forEach { action ->
                DropdownMenuItem(text = { Text(action.label) }, onClick = {
                    expanded = false; onChange(action)
                })
            }
        }
    }
}
