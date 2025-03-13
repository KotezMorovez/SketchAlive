package com.morovez.sketchalive.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var colorChangeListener: ((Int?) -> Unit)? = null
    private val colorWheel = Paint()
    private val pointer = Paint()
    private var touchY = 0.0f
    private var touchX = 0.0f
    private var cY = 288.0f
    private var cX = 288.0f
    private var angleOld = 0f

    private var colorPalette = intArrayOf(
        Color.argb(255, 255, 0, 0),
        Color.argb(255, 255, 255, 0),
        Color.argb(255, 0, 255, 0),
        Color.argb(255, 0, 255, 255),
        Color.argb(255, 0, 0, 255),
        Color.argb(255, 255, 0, 255),
        Color.argb(255, 255, 0, 0)
    )
    private var currentColor = Color.argb(0, 0, 0, 0)
    private var bitmap: Bitmap = createBitmap(10, 10)
    private var wheelCanvas: Canvas = Canvas(bitmap)

    init {
        val shaderColor: Shader = SweepGradient(
            cX,
            cY,
            colorPalette,
            null
        )
        colorWheel.style = Paint.Style.STROKE
        colorWheel.strokeWidth = COLOR_WHEEL_THICKNESS
        colorWheel.setShader(shaderColor)

        pointer.style = Paint.Style.FILL
        pointer.setShadowLayer(15.0f, 0.0f, 0.0f, Color.argb(110, 0, 0, 0))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = createBitmap(w, h)
        wheelCanvas = Canvas(bitmap)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        cX = decodeMeasureSpec(widthMeasureSpec) * 0.5f
        cY = decodeMeasureSpec(widthMeasureSpec) * 0.5f
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        wheelCanvas.drawCircle(cX, cY, COLOR_WHEEL_RADIUS, colorWheel)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        if (touchX < bitmap.width && touchY < bitmap.height) {
            currentColor = bitmap[touchX.toInt(), touchY.toInt()]
        }

        pointer.setColor(currentColor)
        if (touchX != 0f && touchY != 0f) {
            canvas.drawCircle(touchX, touchY, POINTER_RADIUS, pointer)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x - cX
        val y = event.y - cY

        val angle = atan2(y, x)
        val distance = sqrt(x * x + y * y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val t = COLOR_WHEEL_THICKNESS * 0.5f + 48f
                if (distance < COLOR_WHEEL_RADIUS + t && distance > COLOR_WHEEL_RADIUS - t) {
                    touchX =
                        (COLOR_WHEEL_RADIUS * cos(angle) + cX)
                    touchY = (COLOR_WHEEL_RADIUS * sin(angle) + cY)
                    if (touchX < bitmap.width && touchY < bitmap.height) {
                        currentColor = bitmap[touchX.toInt(), touchY.toInt()]
                        colorChangeListener?.invoke(currentColor)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                touchX = (COLOR_WHEEL_RADIUS * cos(angle.toDouble()) + cX).toFloat()
                touchY = (COLOR_WHEEL_RADIUS * sin(angle.toDouble()) + cY).toFloat()
                colorChangeListener?.invoke(currentColor)
            }
        }

        if (angleOld != angle) {
            angleOld = angle
            invalidate()
        }
        return true
    }

    fun setColorChangeListener(listener: (Int?) -> Unit) {
        colorChangeListener = listener
    }

    private fun decodeMeasureSpec(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return if (specMode == MeasureSpec.UNSPECIFIED) 350 else specSize
    }

    companion object {
        private const val COLOR_WHEEL_RADIUS = 180f
        private const val COLOR_WHEEL_THICKNESS = 100f
        private const val POINTER_RADIUS = 90f
    }
}