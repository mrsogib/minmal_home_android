// package com.aurawave.launcher.ui.theme

// import androidx.compose.foundation.isSystemInDarkTheme
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.darkColorScheme
// import androidx.compose.material3.lightColorScheme
// import androidx.compose.runtime.Composable
// import androidx.compose.ui.graphics.Color

// @Composable
// fun WaveLauncherTheme(
//     darkTheme: Boolean = isSystemInDarkTheme(),
//     accentColor: Color,
//     content: @Composable () -> Unit
// ) {
//     val colorScheme = if (darkTheme) {
//         darkColorScheme(
//             primary = accentColor,
//             background = SurfaceDark,
//             surface = SurfaceDark,
//             onBackground = OnSurfaceDark,
//             onSurface = OnSurfaceDark
//         )
//     } else {
//         lightColorScheme(
//             primary = accentColor,
//             background = SurfaceLight,
//             surface = SurfaceLight,
//             onBackground = OnSurfaceLight,
//             onSurface = OnSurfaceLight
//         )
//     }

//     MaterialTheme(
//         colorScheme = colorScheme,
//         typography = Typography,
//         content = content
//     )
// }








package com.aurawave.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryWhite,
    secondary = SecondaryGray,
    background = DarkBackground,
    surface = DarkBackground
)

@Composable
fun WaveLauncherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
