// package com.aurawave.launcher.ui.components

// import androidx.compose.foundation.combinedClickable
// import androidx.compose.foundation.layout.Row
// import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.size
// import androidx.compose.foundation.layout.width
// import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.material3.DropdownMenu
// import androidx.compose.material3.DropdownMenuItem
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.asImageBitmap
// import androidx.compose.ui.graphics.drawscope.draw
// import androidx.compose.ui.graphics.toArgb
// import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.unit.dp
// import androidx.compose.foundation.Image
// import androidx.compose.ui.graphics.painter.BitmapPainter
// import android.graphics.Bitmap
// import android.graphics.Canvas
// import android.graphics.drawable.Drawable
// import com.aurawave.launcher.data.AppInfo

// private fun Drawable.toBitmap(): Bitmap {
//     val width = if (intrinsicWidth > 0) intrinsicWidth else 96
//     val height = if (intrinsicHeight > 0) intrinsicHeight else 96
//     val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//     val canvas = Canvas(bitmap)
//     setBounds(0, 0, canvas.width, canvas.height)
//     draw(canvas)
//     return bitmap
// }

// @Composable
// fun AppListItem(
//     app: AppInfo,
//     showIcon: Boolean = true,
//     onClick: () -> Unit,
//     onHide: () -> Unit,
//     onUninstall: () -> Unit,
//     onAppInfo: () -> Unit
// ) {
//     var menuOpen by remember { mutableStateOf(false) }

//     Row(
//         modifier = Modifier
//             .combinedClickable(
//                 onClick = onClick,
//                 onLongClick = { menuOpen = true }
//             )
//             .padding(horizontal = 24.dp, vertical = 10.dp),
//         verticalAlignment = Alignment.CenterVertically
//     ) {
//         if (showIcon) {
//             val bitmap = remember(app.componentKey) { app.icon.toBitmap().asImageBitmap() }
//             Image(
//                 painter = BitmapPainter(bitmap),
//                 contentDescription = app.label,
//                 modifier = Modifier.size(32.dp)
//             )
//             Row(modifier = Modifier.width(16.dp)) {}
//         }
//         Text(text = app.label, style = MaterialTheme.typography.bodyLarge)

//         DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
//             DropdownMenuItem(text = { Text("Hide from home") }, onClick = { menuOpen = false; onHide() })
//             DropdownMenuItem(text = { Text("App info") }, onClick = { menuOpen = false; onAppInfo() })
//             DropdownMenuItem(text = { Text("Uninstall") }, onClick = { menuOpen = false; onUninstall() })
//         }
//     }
// }










package com.aurawave.launcher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurawave.launcher.data.AppInfo

@Composable
fun AppListItem(
    app: AppInfo,
    onClick: () -> Unit
) {
    Text(
        text = app.label,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    )
}
