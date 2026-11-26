package com.example.klepsydra

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.View
import kotlin.math.min

class HourglassDreamService : DreamService() {

    private val handler = Handler(Looper.getMainLooper())
    private var hourglassView: HourglassView? = null
    private var durationMillis: Long = 60_000L
    private var startTimeMillis: Long = 0L

    private val tickRunnable = object : Runnable {
        override fun run() {
            hourglassView?.updateProgress(getProgress())
            hourglassView?.invalidate()
            handler.postDelayed(this, 33L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        setScreenBright(false)

        val prefs = getSharedPreferences("klepsydra_prefs", Context.MODE_PRIVATE)
        val minutes = prefs.getInt("duration_minutes", 1).coerceIn(1, 120)
        durationMillis = minutes * 60_000L
        startTimeMillis = System.currentTimeMillis()

        val view = HourglassView(this)
        hourglassView = view
        setContentView(view)
        handler.post(tickRunnable)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(tickRunnable)
        super.onDetachedFromWindow()
    }

    private fun getProgress(): Float {
        if (durationMillis <= 0L) return 1f
        val elapsed = System.currentTimeMillis() - startTimeMillis
        if (elapsed >= durationMillis) {
            startTimeMillis = System.currentTimeMillis()
            return 0f
        }
        return (elapsed.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)
    }

    private class HourglassView(context: Context) : View(context) {
        private var progress: Float = 0f

        private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        private val sandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F59E0B")
            style = Paint.Style.FILL
        }

        private val streamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBBF24")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        fun updateProgress(value: Float) {
            progress = value
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(Color.parseColor("#090D16"))

            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return

            val cx = w / 2f
            val cy = h / 2f
            val halfW = min(w, h) * 0.24f
            val halfH = min(w, h) * 0.36f

            val topY = cy - halfH
            val bottomY = cy + halfH

            val outlinePath = Path().apply {
                moveTo(cx - halfW, topY)
                lineTo(cx + halfW, topY)
                lineTo(cx, cy)
                lineTo(cx + halfW, bottomY)
                lineTo(cx - halfW, bottomY)
                lineTo(cx, cy)
                close()
            }

            val remaining = (1f - progress).coerceIn(0f, 1f)
            if (remaining > 0.005f) {
                val topSandH = halfH * remaining
                val topSandLeft = cx - halfW * remaining
                val topSandRight = cx + halfW * remaining
                val topSandPath = Path().apply {
                    moveTo(topSandLeft, cy - topSandH)
                    lineTo(topSandRight, cy - topSandH)
                    lineTo(cx, cy)
                    close()
                }
                canvas.drawPath(topSandPath, sandPaint)
            }

            if (progress > 0.005f) {
                val bottomSandH = halfH * progress
                val bottomSandLeft = cx - halfW * progress
                val bottomSandRight = cx + halfW * progress
                val bottomSandPath = Path().apply {
                    moveTo(cx, bottomY - bottomSandH)
                    lineTo(bottomSandRight, bottomY)
                    lineTo(bottomSandLeft, bottomY)
                    close()
                }
                canvas.drawPath(bottomSandPath, sandPaint)
            }

            if (progress > 0.001f && progress < 0.999f) {
                canvas.drawLine(cx, cy, cx, bottomY, streamPaint)
            }

            canvas.drawPath(outlinePath, glassPaint)
        }
    }
}
