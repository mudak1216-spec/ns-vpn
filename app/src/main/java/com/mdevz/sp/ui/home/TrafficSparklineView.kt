package com.mdevz.sp.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mdevz.sp.R

class TrafficSparklineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {
    private val samples =
        ArrayDeque<Long>()

    private val linePaint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.displayMetrics.density * 2.2f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

    private val fillPaint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            style = Paint.Style.FILL
        }

    private val path = Path()
    private val fillPath = Path()

    fun addSample(value: Long) {
        if (samples.size >= MAX_SAMPLES) {
            samples.removeFirst()
        }
        samples.addLast(value.coerceAtLeast(0L))
        invalidate()
    }

    fun clearSamples() {
        if (samples.isEmpty()) return
        samples.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val values = samples.toList()
        if (values.size < 2) return

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        if (contentWidth <= 0 || contentHeight <= 0) return

        val maximum = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
        val step = contentWidth.toFloat() / (values.size - 1)

        val isUpload = id == R.id.uploadChart
        val neonColor = ContextCompat.getColor(
            context,
            if (isUpload) R.color.neon_violet else R.color.neon_cyan
        )
        val fadeColor = if (isUpload) 0x058B5CF6 else 0x0500F0FF

        linePaint.color = neonColor
        fillPaint.shader = LinearGradient(
            0f,
            paddingTop.toFloat(),
            0f,
            (paddingTop + contentHeight).toFloat(),
            (neonColor and 0x00FFFFFF) or 0x44000000,
            fadeColor,
            Shader.TileMode.CLAMP
        )

        path.reset()
        fillPath.reset()

        values.forEachIndexed { index, value ->
            val x = paddingLeft + step * index
            val ratio = value.toFloat() / maximum.toFloat()
            val y = paddingTop + contentHeight * (1f - ratio)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, (paddingTop + contentHeight).toFloat())
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == values.lastIndex) {
                fillPath.lineTo(x, (paddingTop + contentHeight).toFloat())
                fillPath.close()
            }
        }

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)
    }

    private companion object {
        const val MAX_SAMPLES = 24
    }
}
