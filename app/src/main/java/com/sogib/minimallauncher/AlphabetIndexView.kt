package com.sogib.minimallauncher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Thin vertical strip on the right edge. Only draws letters that at least one
 * visible app actually starts with — never a full static A-Z, per the "don't show
 * unused letters" requirement.
 */
class AlphabetIndexView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var letters: List<Char> = emptyList()
        set(value) { field = value; invalidate() }

    var onLetterSelected: ((Char) -> Unit)? = null

    var textSizeSp: Float = 32f
        set(value) { field = value; paint.textSize = value; invalidate() }

    var textColor: Int = android.graphics.Color.WHITE
        set(value) { field = value; paint.color = value; invalidate() }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 32f
        color = android.graphics.Color.WHITE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (letters.isEmpty()) return
        val slot = height.toFloat() / letters.size
        letters.forEachIndexed { i, c ->
            val y = slot * i + slot / 2 + paint.textSize / 3
            canvas.drawText(c.toString(), width / 2f, y, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (letters.isEmpty()) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val slot = height.toFloat() / letters.size
                val index = (event.y / slot).toInt().coerceIn(0, letters.size - 1)
                onLetterSelected?.invoke(letters[index])
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
