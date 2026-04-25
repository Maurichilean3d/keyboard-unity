package com.keyboardunity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class TrackpadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var onMouseMove: ((dx: Int, dy: Int) -> Unit)? = null
    var onScroll: ((delta: Int) -> Unit)? = null

    var sensitivity = 1.5f

    private var lastX = 0f
    private var lastY = 0f
    private var lastTwoFingerY = 0f
    private var isTwoFinger = false

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF181825.toInt() }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF6C7086.toInt() }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF585B70.toInt()
        textAlign = Paint.Align.CENTER
    }
    private val fingerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF89DCEB.toInt() }

    private var fingerX = -1f
    private var fingerY = -1f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        labelPaint.textSize = h * 0.05f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(bgPaint.color)

        // Dot grid hint
        val spacing = 32f
        var gx = spacing
        while (gx < width) {
            var gy = spacing
            while (gy < height) {
                canvas.drawCircle(gx, gy, 2f, dotPaint)
                gy += spacing
            }
            gx += spacing
        }

        // Hint text
        if (fingerX < 0) {
            canvas.drawText("Touch to move cursor", width / 2f, height / 2f, labelPaint)
            canvas.drawText("Two fingers to scroll", width / 2f, height / 2f + labelPaint.textSize * 1.5f, labelPaint)
        }

        // Finger indicator
        if (fingerX >= 0) {
            canvas.drawCircle(fingerX, fingerY, 28f, fingerPaint.apply { alpha = 120 })
            canvas.drawCircle(fingerX, fingerY, 6f, fingerPaint.apply { alpha = 255 })
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                fingerX = event.x
                fingerY = event.y
                isTwoFinger = false
                invalidate()
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    isTwoFinger = true
                    lastTwoFingerY = (event.getY(0) + event.getY(1)) / 2f
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTwoFinger && event.pointerCount >= 2) {
                    val avgY = (event.getY(0) + event.getY(1)) / 2f
                    val delta = avgY - lastTwoFingerY
                    lastTwoFingerY = avgY
                    if (abs(delta) > 1f) {
                        val scroll = -(delta / 8f).toInt()
                        onScroll?.invoke(scroll)
                    }
                } else if (!isTwoFinger) {
                    val dx = (event.x - lastX) * sensitivity
                    val dy = (event.y - lastY) * sensitivity
                    lastX = event.x
                    lastY = event.y
                    fingerX = event.x
                    fingerY = event.y
                    val idx = dx.toInt()
                    val idy = dy.toInt()
                    if (idx != 0 || idy != 0) {
                        onMouseMove?.invoke(idx, idy)
                    }
                    invalidate()
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount <= 2) {
                    isTwoFinger = false
                    // Update last position so we don't jump on next single-finger move
                    val remaining = if (event.actionIndex == 0) 1 else 0
                    lastX = event.getX(remaining)
                    lastY = event.getY(remaining)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                fingerX = -1f
                fingerY = -1f
                isTwoFinger = false
                invalidate()
            }
        }
        return true
    }
}
