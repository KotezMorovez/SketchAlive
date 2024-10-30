package com.example.animatorforandroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.util.EventLog
import android.view.MotionEvent
import android.view.View
import androidx.resourceinspection.annotation.Attribute.IntMap
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var path = Path()
    private var canvasObjectList = ArrayList<CanvasObject>()
    private val eventList = ArrayList<Event>()
    private var instrumentColor = Color.BLUE
    private val instrumentStyle: Paint.Style = Paint.Style.STROKE
    private var instrument = Instrument.NONE
    private var eventIndex = -1
    private var mX = 0f
    private var mY = 0f
    private var startCoordX = 0f
    private var startCoordY = 0f
    private var paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = instrumentStyle
        alpha = 255
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (canvasObject in canvasObjectList) {
            if (canvasObject.isDelete) {
                continue
            }

            if (canvasObject.instrument == Instrument.PENCIL) {
                paint.strokeWidth = 4f
                paint.strokeJoin = Paint.Join.BEVEL
                paint.strokeCap = Paint.Cap.SQUARE
            } else {
                paint.strokeWidth = 16f
                paint.strokeJoin = Paint.Join.ROUND
                paint.strokeCap = Paint.Cap.ROUND
            }

            paint.color = canvasObject.color
            canvas.drawPath(canvasObject.path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (instrument == Instrument.NONE) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                touchUp(x, y)
                invalidate()
            }
        }

        return true
    }

    fun setInstrument(instrument: Instrument) {
        this.instrument = instrument
    }

    fun setColor(color: Int) {
        instrumentColor = color
        paint.color = color
    }

    fun clear() {
        canvasObjectList.clear()
        eventList.clear()
        eventIndex = -1
        invalidate()
    }

    fun create() {
        // TODO Создание нового кадра
    }

    fun undo() {
        if (eventIndex >= 0) {
            val event = eventList[eventIndex]
            canvasObjectList[event.index].isDelete = !canvasObjectList[event.index].isDelete
            eventIndex--
            invalidate()
        }
    }

    fun redo() {
        if (eventIndex < eventList.size - 1) {
            eventIndex++
            val event = eventList[eventIndex]
            canvasObjectList[event.index].isDelete = !canvasObjectList[event.index].isDelete
            invalidate()
        }
    }

    private fun touchStart(x: Float, y: Float) {
        if (instrument != Instrument.ERASE) {
            path = Path()
            val canvasObject = CanvasObject(
                path = path,
                color = instrumentColor,
                instrument = instrument,
                isDelete = false
            )

            canvasObjectList.add(canvasObject)

            if (eventIndex != eventList.size - 1) {
                while (eventIndex != eventList.size - 1) {
                    eventList.removeLast()
                }
            }

            eventList.add(Event.DrawObject(index = canvasObjectList.size - 1))
            eventIndex++

        }

        when (instrument) {
            Instrument.PENCIL, Instrument.BRUSH, Instrument.ERASE -> {
                path.moveTo(x, y)
                mX = x
                mY = y
            }

            Instrument.CIRCLE, Instrument.RECTANGLE, Instrument.TRIANGLE, Instrument.ARROW -> {
                startCoordX = x
                startCoordY = y
            }

            else -> {}
        }
    }

    private fun touchMove(x: Float, y: Float) {
        when (instrument) {
            Instrument.PENCIL, Instrument.BRUSH -> {
                val dx = abs(x - mX)
                val dy = abs(y - mY)
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                    mX = x
                    mY = y
                }
            }

            Instrument.ERASE -> {
                for (i in canvasObjectList.indices) {
                    if (
                        !canvasObjectList[i].isDelete &&
                        isPointOnPath(canvasObjectList[i].path, x, y)
                    ) {
                        canvasObjectList[i].isDelete = true

                        if (eventIndex != eventList.size - 1) {
                            while (eventIndex != eventList.size - 1) {
                                eventList.removeLast()
                            }
                        }

                        eventList.add(Event.EraseObject(index = i))
                        eventIndex++
                    }
                }

                mX = x
                mY = y
            }

            Instrument.CIRCLE -> {
                path.reset()
                val radius =
                    if (abs(startCoordX - x) < abs(startCoordY - y)) {
                        abs(startCoordX - x)
                    } else {
                        abs(startCoordY - y)
                    }
                path.addCircle(
                    (startCoordX + x) / 2,
                    (startCoordY + y) / 2,
                    radius,
                    Path.Direction.CW
                )
            }

            Instrument.RECTANGLE -> {
                path.reset()

                if (startCoordX < x) {
                    if (startCoordY > y) {
                        // старт - левый нижний угол
                        path.addRect(startCoordX, y, x, startCoordY, Path.Direction.CW)
                    } else {
                        // старт - левый верхний угол
                        path.addRect(startCoordX, startCoordY, x, y, Path.Direction.CW)
                    }
                } else {
                    if (startCoordY > y) {
                        // старт - правый нижний угол
                        path.addRect(x, y, startCoordX, startCoordY, Path.Direction.CW)
                    } else {
                        // старт - правый верхний угол
                        path.addRect(x, startCoordY, startCoordX, y, Path.Direction.CW)
                    }
                }
            }

            Instrument.TRIANGLE -> {
                path.reset()
                // найти наименьшую сторону, наметить квадрат и вписать в него треугольник
                val side = if (abs(startCoordX - x) > abs(startCoordY - y)) {
                    abs(startCoordY - y)
                } else {
                    abs(startCoordX - x)
                }

                if (startCoordX < x) {
                    if (startCoordY > y) {
                        // старт - левый нижний угол
                        path.moveTo(startCoordX, startCoordY)
                        path.lineTo(startCoordX + side / 2, startCoordY - side)
                        path.lineTo(startCoordX + side, startCoordY)
                        path.close()
                    } else {
                        // старт - левый верхний угол
                        path.moveTo(startCoordX, startCoordY + side)
                        path.lineTo(startCoordX + side / 2, startCoordY)
                        path.lineTo(startCoordX + side, startCoordY + side)
                        path.close()
                    }
                } else {
                    if (startCoordY > y) {
                        // старт - правый нижний угол
                        path.moveTo(startCoordX, startCoordY)
                        path.lineTo(startCoordX - side / 2, startCoordY - side)
                        path.lineTo(startCoordX - side, startCoordY)
                        path.close()
                    } else {
                        // старт - правый верхний угол
                        path.moveTo(startCoordX, startCoordY + side)
                        path.lineTo(startCoordX - side / 2, startCoordY)
                        path.lineTo(startCoordX - side, startCoordY + side)
                        path.close()
                    }
                }
            }

            Instrument.ARROW -> {
                path.reset()
                val (x1, y1, x2, y2) = calculateArrowCoords(40f, 45.0, x, y)

                path.moveTo(startCoordX, startCoordY)
                path.lineTo(x, y)
                path.lineTo(x1, y1)
                path.moveTo(x, y)
                path.lineTo(x2, y2)
            }

            else -> {}
        }
    }

    private fun touchUp(x: Float, y: Float) {
        when (instrument) {
            Instrument.PENCIL, Instrument.BRUSH -> {
                path.lineTo(mX, mY)
            }

            Instrument.ERASE -> {
                for (i in canvasObjectList.indices) {
                    if (
                        !canvasObjectList[i].isDelete &&
                        isPointOnPath(canvasObjectList[i].path, x, y)
                    ) {
                        canvasObjectList[i].isDelete = true

                        if (eventIndex != eventList.size - 1) {
                            while (eventIndex != eventList.size - 1) {
                                eventList.removeLast()
                            }
                        }

                        eventList.add(Event.EraseObject(index = i))
                        eventIndex++
                    }
                }

                mX = x
                mY = y
            }

            Instrument.CIRCLE -> {
                path.reset()
                val radius =
                    if (abs(startCoordX - x) < abs(startCoordY - y)) {
                        abs(startCoordX - x)
                    } else {
                        abs(startCoordY - y)
                    }
                path.addCircle(
                    (startCoordX + x) / 2,
                    (startCoordY + y) / 2,
                    radius,
                    Path.Direction.CW
                )
            }

            Instrument.RECTANGLE -> {
                path.reset()

                if (startCoordX < x) {
                    if (startCoordY > y) {
                        // старт - левый нижний угол
                        path.addRect(startCoordX, y, x, startCoordY, Path.Direction.CW)
                    } else {
                        // старт - левый верхний угол
                        path.addRect(startCoordX, startCoordY, x, y, Path.Direction.CW)
                    }
                } else {
                    if (startCoordY > y) {
                        // старт - правый нижний угол
                        path.addRect(x, y, startCoordX, startCoordY, Path.Direction.CW)
                    } else {
                        // старт - правый верхний угол
                        path.addRect(x, startCoordY, startCoordX, y, Path.Direction.CW)
                    }
                }
            }

            Instrument.TRIANGLE -> {
                path.reset()
                // найти наименьшую сторону, наметить квадрат и вписать в него треугольник
                val side = if (abs(startCoordX - x) > abs(startCoordY - y)) {
                    abs(startCoordY - y)
                } else {
                    abs(startCoordX - x)
                }

                if (startCoordX < x) {
                    if (startCoordY > y) {
                        // старт - левый нижний угол
                        path.moveTo(startCoordX, startCoordY)
                        path.lineTo(startCoordX + side / 2, startCoordY - side)
                        path.lineTo(startCoordX + side, startCoordY)
                        path.close()
                    } else {
                        // старт - левый верхний угол
                        path.moveTo(startCoordX, startCoordY + side)
                        path.lineTo(startCoordX + side / 2, startCoordY)
                        path.lineTo(startCoordX + side, startCoordY + side)
                        path.close()
                    }
                } else {
                    if (startCoordY > y) {
                        // старт - правый нижний угол
                        path.moveTo(startCoordX, startCoordY)
                        path.lineTo(startCoordX - side / 2, startCoordY - side)
                        path.lineTo(startCoordX - side, startCoordY)
                        path.close()
                    } else {
                        // старт - правый верхний угол
                        path.moveTo(startCoordX, startCoordY + side)
                        path.lineTo(startCoordX - side / 2, startCoordY)
                        path.lineTo(startCoordX - side, startCoordY + side)
                        path.close()
                    }
                }
            }

            Instrument.ARROW -> {
                path.reset()
                val (x1, y1, x2, y2) = calculateArrowCoords(40f, 45.0, x, y)

                path.moveTo(startCoordX, startCoordY)
                path.lineTo(x, y)
                path.lineTo(x1, y1)
                path.moveTo(x, y)
                path.lineTo(x2, y2)
            }

            else -> {}
        }
    }

    private fun calculateArrowCoords(
        length: Float,
        headAngle: Double,
        x: Float,
        y: Float
    ): FloatArray {
        val angle = atan2(y - startCoordY, x - startCoordX)

        val arrowX1 = x - length * cos(angle - Math.toRadians(headAngle))
        val arrowY1 = y - length * sin(angle - Math.toRadians(headAngle))

        val arrowX2 = x - length * cos(angle + Math.toRadians(headAngle))
        val arrowY2 = y - length * sin(angle + Math.toRadians(headAngle))

        return floatArrayOf(
            arrowX1.toFloat(),
            arrowY1.toFloat(),
            arrowX2.toFloat(),
            arrowY2.toFloat()
        )
    }

    private fun isPointOnPath(path: Path, x: Float, y: Float): Boolean {
        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length
        val precision = 20f

        val pos = FloatArray(2)
        val tan = FloatArray(2)

        var distance = 0f
        while (distance < pathLength) {
            pathMeasure.getPosTan(distance, pos, tan)
            if (abs(pos[0] - x) < precision && abs(pos[1] - y) < precision) {
                return true
            }
            distance += precision
        }

        return false
    }

    enum class Instrument {
        NONE,
        PENCIL,
        BRUSH,
        ERASE,
        RECTANGLE,
        CIRCLE,
        ARROW,
        TRIANGLE
    }

    sealed class Event(open val index: Int) {
        data class DrawObject(override val index: Int) : Event(index)
        data class EraseObject(override val index: Int) : Event(index)
    }

    data class CanvasObject(
        val color: Int,
        val instrument: Instrument,
        val path: Path,
        var isDelete: Boolean
    )

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }
}