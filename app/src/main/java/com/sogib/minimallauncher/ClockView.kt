package com.sogib.minimallauncher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.BatteryManager
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Draws the clock entirely with Canvas/Paint text — no bitmaps, no drawable assets.
 * Style 0: plain "HH:mm" in a regular weight.
 * Style 1: big hour digits, smaller minutes top-aligned with the hour, AM/PM under the
 *          minutes, and battery percentage below that — the Niagara-style layout.
 */
class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var style: Int = 0
        set(value) { field = value; invalidate() }

    var typeface_: Typeface = Typeface.DEFAULT
        set(value) { field = value; hourPaint.typeface = value; minPaint.typeface = value; smallPaint.typeface = value; invalidate() }

    /** Multiplies all clock text sizes; 1.0 = default. */
    var sizeScale: Float = 1.0f
        set(value) { field = value; invalidate() }

    var textColor: Int = android.graphics.Color.WHITE
        set(value) { field = value; hourPaint.color = value; minPaint.color = value; smallPaint.color = value; invalidate() }

    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.LEFT; color = android.graphics.Color.WHITE }
    private val minPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.LEFT; color = android.graphics.Color.WHITE }
    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.LEFT; color = android.graphics.Color.WHITE }

    private var batteryPct: Int = -1

    private val tickRunnable = object : Runnable {
        override fun run() {
            invalidate()
            postDelayed(this, 1000L * 30)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(tickRunnable)
        refreshBattery()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(tickRunnable)
        super.onDetachedFromWindow()
    }

    private fun refreshBattery() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val status: Intent? = context.registerReceiver(null, filter)
        val level = status?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = status?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = Calendar.getInstance()
        val hourStr = SimpleDateFormat("h", Locale.getDefault()).format(now.time)
        val minStr = SimpleDateFormat("mm", Locale.getDefault()).format(now.time)
        val ampm = SimpleDateFormat("a", Locale.getDefault()).format(now.time)

        val startX = paddingLeft.toFloat()
        var y = paddingTop.toFloat()

        if (style == 0) {
            hourPaint.textSize = 96f * sizeScale
            y += hourPaint.textSize
            canvas.drawText("$hourStr:$minStr", startX, y, hourPaint)
        } else {
            hourPaint.textSize = 140f * sizeScale
            minPaint.textSize = 64f * sizeScale
            smallPaint.textSize = 28f * sizeScale

            val hourWidth = hourPaint.measureText(hourStr)
            y += hourPaint.textSize
            canvas.drawText(hourStr, startX, y, hourPaint)

            // Minutes start where the hour text ends, top-aligned with the hour's top.
            val minX = startX + hourWidth + 12f
            val minY = paddingTop + minPaint.textSize
            canvas.drawText(minStr, minX, minY, minPaint)

            // AM/PM directly under the minutes.
            val ampmY = minY + smallPaint.textSize + 6f
            canvas.drawText(ampm, minX, ampmY, smallPaint)

            // Battery percentage below everything else, Niagara-style.
            if (batteryPct >= 0) {
                val battY = y + smallPaint.textSize + 16f
                canvas.drawText("$batteryPct%", startX, battY, smallPaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        refreshBattery()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
