package com.sogib.minimallauncher

import android.graphics.Typeface

/**
 * A small, deliberately short list of good-looking system fonts — "some, not a lot"
 * per the brief. Uses Typeface.create with system font families so no font files
 * need to be bundled (keeps the app light).
 */
object FontOptions {
    val names = listOf("Sans (default)", "Sans Light", "Serif", "Serif Medium", "Monospace")

    fun get(index: Int): Typeface = when (index) {
        1 -> Typeface.create("sans-serif-light", Typeface.NORMAL)
        2 -> Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        3 -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
        4 -> Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        else -> Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
}
