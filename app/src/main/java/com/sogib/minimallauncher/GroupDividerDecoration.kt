package com.sogib.minimallauncher

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Draws a thin divider only where the alphabet letter changes between two
 * consecutive rows — i.e. once per letter group boundary, not between every app.
 * Only makes visual sense when the list is actually alphabetical.
 */
class GroupDividerDecoration(
    private val adapter: AppDrawerAdapter,
    color: Int,
    private val thicknessPx: Float = 2f,
    private val marginPx: Float = 20f
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        this.color = color
        strokeWidth = thicknessPx
        alpha = 90
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child: View = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position <= 0) continue
            val current = adapter.letterAt(position)
            val previous = adapter.letterAt(position - 1)
            if (current != null && previous != null && current != previous) {
                val y = child.top.toFloat()
                c.drawLine(marginPx, y, parent.width - marginPx, y, paint)
            }
        }
    }
}
