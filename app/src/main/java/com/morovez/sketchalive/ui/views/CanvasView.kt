package com.morovez.sketchalive.ui.views

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.morovez.sketchalive.data.AnimatedGIFWriter
import com.morovez.sketchalive.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.graphics.scale

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private var path = Path()
    private val eventList = ArrayList<Event>()
    private var frameIndex = 0L
    private var frameListSize = 1L
    private var rootFrameNode = FrameNode(
        id = frameIndex,
        canvasObjectList = arrayListOf()
    )
    var activeFrameNode = rootFrameNode
        private set
    private var animationActiveFrame = rootFrameNode
    private var animationFrameIndex = 0
    private var instrumentColor = Color.argb(255, 25, 118, 210)
    private var currentInstrument = Instrument.NONE
    private val instrumentMap: MutableMap<Instrument, Float> = mutableMapOf(
        Instrument.NONE to DEFAULT_INSTRUMENT_WIDTH
    )
    private var eventIndex = -1
    private var touchX = 0f
    private var touchY = 0f
    private var startCoordX = 0f
    private var startCoordY = 0f
    private var isForAnimation = false
    private var isForLayersManager = false
    private var frameDelay = 300L
    private var paint = Paint().apply {
        color = instrumentColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        alpha = 255
    }
    private val valueAnimator = ValueAnimator().apply {
        repeatCount = INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            if ((it.animatedValue as Float).toInt() != animationFrameIndex) {
                animationFrameIndex = (it.animatedValue as Float).toInt()

                animationActiveFrame = if (animationActiveFrame.next != null) {
                    animationActiveFrame.next!!
                } else {
                    rootFrameNode
                }
                invalidate()
            }
        }
    }
    private val backgroundBitmap: Bitmap by lazy {
        compressBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.canvas),
            this.width,
            this.height
        )
    }
    private val canvas = Canvas()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isForAnimation) {
            if (activeFrameNode.prev != null) {
                drawFrame(activeFrameNode.prev!!.canvasObjectList, canvas, true)
            }
            drawFrame(activeFrameNode.canvasObjectList, canvas, false)
        } else {
            drawFrame(animationActiveFrame.canvasObjectList, canvas, false)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isForAnimation || isForLayersManager) {
            return false
        }

        val x = event.x
        val y = event.y

        if (currentInstrument == Instrument.NONE) {
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

    fun setInstrumentWidth(size: Float) {
        instrumentMap[currentInstrument] = size
    }

    fun getCurrentInstrumentWidth(): Float {
        return instrumentMap[currentInstrument] ?: DEFAULT_INSTRUMENT_WIDTH
    }

    fun getCurrentAnimationSpeed(): Float {
        return when (frameDelay) {
            300L -> {
                1f
            }

            200L -> {
                2f
            }

            else -> {
                3f
            }
        }
    }

    fun setAnimationSpeed(speed: Float) {
        frameDelay = when (speed.toInt()) {
            1 -> {
                300L
            }

            2 -> {
                200L
            }

            else -> {
                100L
            }
        }

        valueAnimator.duration = frameDelay * frameListSize
    }

    fun setInstrument(instrument: Instrument) {
        currentInstrument = instrument
        if (!instrumentMap.containsKey(instrument)) {
            instrumentMap[instrument] = DEFAULT_INSTRUMENT_WIDTH
        }
    }

    fun setColor(color: Int) {
        instrumentColor = color
        paint.color = color
    }

    fun deleteFrame() {
        if (activeFrameNode != rootFrameNode) {
            activeFrameNode.prev?.next = activeFrameNode.next
            activeFrameNode.next?.prev = activeFrameNode.prev
            activeFrameNode = activeFrameNode.prev!!
            eventList.clear()
            eventIndex -= 1
        } else {
            rootFrameNode = rootFrameNode.next ?: FrameNode(
                id = frameIndex,
                canvasObjectList = arrayListOf()
            )
            rootFrameNode.prev = null
            activeFrameNode = rootFrameNode
            eventIndex = -1
        }
        invalidate()
    }

    fun create() {
        frameIndex++
        val newFrameNode = FrameNode(
            id = frameIndex,
            canvasObjectList = arrayListOf(),
            prev = activeFrameNode
        )

        if (activeFrameNode.next == null) {
            activeFrameNode.next = newFrameNode
        } else {
            newFrameNode.next = activeFrameNode.next
            activeFrameNode.next?.prev = newFrameNode
            activeFrameNode.next = newFrameNode
        }

        activeFrameNode = newFrameNode
        frameListSize++
        clear()
    }

    fun undo() {
        if (eventIndex >= 0) {
            val event = eventList[eventIndex]
            activeFrameNode.canvasObjectList[event.index].isDelete =
                !activeFrameNode.canvasObjectList[event.index].isDelete
            eventIndex--
            invalidate()
        }
    }

    fun redo() {
         if (eventIndex < eventList.size - 1) {
            eventIndex++
            val event = eventList[eventIndex]
            activeFrameNode.canvasObjectList[event.index].isDelete =
                !activeFrameNode.canvasObjectList[event.index].isDelete
            invalidate()
        }
    }

    fun play() {
        isForAnimation = true
        animationFrameIndex = 0

        valueAnimator.duration = frameDelay * frameListSize

        valueAnimator.setFloatValues(0.01f, frameListSize.toFloat() - 0.01f)

        animationActiveFrame = rootFrameNode
        invalidate()
        valueAnimator.start()
    }

    fun pause() {
        isForAnimation = false
        valueAnimator.cancel()
        invalidate()
    }

    fun duplicateActiveFrame() {
        frameIndex++
        val newFrameNode = FrameNode(
            id = frameIndex,
            canvasObjectList = ArrayList(activeFrameNode.canvasObjectList),
            prev = activeFrameNode,
            next = activeFrameNode.next
        )
        activeFrameNode.next?.prev = newFrameNode
        activeFrameNode.next = newFrameNode
        activeFrameNode = newFrameNode
        frameListSize++
        invalidate()
    }

    fun showFrame(frameNode: FrameNode) {
        activeFrameNode = frameNode
        invalidate()
    }

    fun deleteAllFrames() {
        rootFrameNode.canvasObjectList.clear()
        rootFrameNode.next = null
        activeFrameNode = rootFrameNode
        frameListSize = 1L
        invalidate()
    }

    fun getBitmapList(): List<Pair<FrameNode, Bitmap>> {
        isForLayersManager = true
        val result: ArrayList<Pair<FrameNode, Bitmap>> = arrayListOf()

        result.add(createBitmapPair(activeFrameNode))

        if (activeFrameNode == rootFrameNode) {
            if (activeFrameNode.next != null) {
                result.add(createBitmapPair(activeFrameNode.next!!))

                if (activeFrameNode.next!!.next != null) {
                    result.add(createBitmapPair(activeFrameNode.next!!.next!!))
                }
            }
            return result
        }

        if (activeFrameNode.next == null) {
            if (activeFrameNode.prev != null) {
                result.add(0, createBitmapPair(activeFrameNode.prev!!))

                if (activeFrameNode.prev!!.prev != null) {
                    result.add(0, createBitmapPair(activeFrameNode.prev!!.prev!!))
                }
            }
            return result
        }


        result.add(0, createBitmapPair(activeFrameNode.prev!!))
        result.add(createBitmapPair(activeFrameNode.next!!))
        return result
    }

    fun closeLayersManager() {
        isForLayersManager = false
    }

    fun moveBack() {
        activeFrameNode = activeFrameNode.prev ?: return
        invalidate()
    }

    fun moveForward() {
        activeFrameNode = activeFrameNode.next ?: return
        invalidate()
    }

    fun generateGif(): String {
        var actualFrameNode: FrameNode? = rootFrameNode
        val root = context.cacheDir.absolutePath + "/animator_gif_cache"
        val cacheUri = "$root/animation.gif"
        val dir = File(root)

        dir.mkdir()
        val writer = AnimatedGIFWriter(true)
        val output = FileOutputStream(cacheUri)
        writer.prepareForWrite(output, -1, -1)

        do {
            val bitmap = createBitmapFullSize(actualFrameNode!!)
            writer.writeFrame(output, bitmap)
            actualFrameNode = actualFrameNode.next
        } while (actualFrameNode != null)

        writer.finishWrite(output)
        return cacheUri
    }

    private fun clear() {
        activeFrameNode.canvasObjectList.clear()
        eventList.clear()
        eventIndex = -1
        invalidate()
    }

    private fun createBitmapFullSize(frameNode: FrameNode): Bitmap {
        val bitmap = backgroundBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas.setBitmap(bitmap)
        drawFrame(frameNode.canvasObjectList, canvas, false)
        return bitmap
    }

    private fun createBitmapPair(frameNode: FrameNode): Pair<FrameNode, Bitmap> {
        val bitmap = createBitmapFullSize(frameNode)
        return Pair(frameNode, compressBitmap(bitmap, 128, 218)) // ~ 16:9
    }

    private fun compressBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val resizedBitmap = bitmap.scale(width, height)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
        val byteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun calculateArrowCoords(
        length: Float = 40.0F,
        headAngle: Double = 45.0,
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

    private fun isPointOnPath(path: Path, width: Float, x: Float, y: Float): Boolean {
        val pathMeasure = PathMeasure(path, false)
        val pathLength = pathMeasure.length
        val precision = width * 5

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

    private fun drawFrame(list: List<CanvasObject>, canvas: Canvas, isTranslucent: Boolean) {
        for (canvasObject in list) {
            if (canvasObject.isDelete) {
                continue
            }
            paint.strokeWidth = canvasObject.width

            if (canvasObject.instrument == Instrument.PENCIL) {
                with(paint) {
                    strokeJoin = Paint.Join.BEVEL
                    strokeCap = Paint.Cap.SQUARE
                }
            } else {
                with(paint) {
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                }
            }

            if (isTranslucent) {
                val translucentColor = canvasObject.color and 0x00ffffff or (70 shl 24)
                paint.color = translucentColor
            } else {
                paint.color = canvasObject.color
            }

            canvas.drawPath(canvasObject.path, paint)
        }
    }

    private fun touchStart(x: Float, y: Float) {
        if (currentInstrument != Instrument.ERASE) {
            path = Path()
            val canvasObject = CanvasObject(
                path = path,
                color = instrumentColor,
                instrument = currentInstrument,
                width = instrumentMap[currentInstrument] ?: DEFAULT_INSTRUMENT_WIDTH,
                isDelete = false
            )

            activeFrameNode.canvasObjectList.add(canvasObject)

            if (eventIndex != eventList.size - 1) {
                while (eventIndex != eventList.size - 1) {
                    eventList.removeAt(eventList.lastIndex)
                }
            }

            eventList.add(Event.DrawObject(index = activeFrameNode.canvasObjectList.size - 1))
            eventIndex++

        }

        when (currentInstrument) {
            Instrument.PENCIL, Instrument.BRUSH, Instrument.ERASE -> {
                path.moveTo(x, y)
                touchX = x
                touchY = y
            }

            Instrument.CIRCLE, Instrument.RECTANGLE, Instrument.TRIANGLE, Instrument.ARROW -> {
                startCoordX = x
                startCoordY = y
            }

            else -> {}
        }
    }

    private fun touchMove(x: Float, y: Float) {
        when (currentInstrument) {
            Instrument.PENCIL, Instrument.BRUSH -> {
                val dx = abs(x - touchX)
                val dy = abs(y - touchY)
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(touchX, touchY, (x + touchX) / 2, (y + touchY) / 2)
                    touchX = x
                    touchY = y
                }
            }

            Instrument.ERASE -> {
                for (i in activeFrameNode.canvasObjectList.indices) {
                    if (
                        !activeFrameNode.canvasObjectList[i].isDelete &&
                        isPointOnPath(
                            activeFrameNode.canvasObjectList[i].path,
                            instrumentMap[currentInstrument] ?: DEFAULT_INSTRUMENT_WIDTH,
                            x,
                            y
                        )
                    ) {
                        activeFrameNode.canvasObjectList[i].isDelete = true

                        if (eventIndex != eventList.size - 1) {
                            while (eventIndex != eventList.size - 1) {
                                eventList.removeAt(eventList.lastIndex)
                            }
                        }

                        eventList.add(Event.EraseObject(index = i))
                        eventIndex++
                    }
                }

                touchX = x
                touchY = y
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
        when (currentInstrument) {
            Instrument.PENCIL, Instrument.BRUSH -> {
                path.lineTo(touchX, touchY)
            }

            Instrument.ERASE -> {
                for (i in activeFrameNode.canvasObjectList.indices) {
                    if (
                        !activeFrameNode.canvasObjectList[i].isDelete &&
                        isPointOnPath(
                            activeFrameNode.canvasObjectList[i].path,
                            instrumentMap[currentInstrument] ?: DEFAULT_INSTRUMENT_WIDTH,
                            x,
                            y
                        )
                    ) {
                        activeFrameNode.canvasObjectList[i].isDelete = true

                        if (eventIndex != eventList.size - 1) {
                            while (eventIndex != eventList.size - 1) {
                                eventList.removeAt(eventList.lastIndex)
                            }
                        }

                        eventList.add(Event.EraseObject(index = i))
                        eventIndex++
                    }
                }

                touchX = x
                touchY = y
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
        val width: Float = DEFAULT_INSTRUMENT_WIDTH,
        var isDelete: Boolean
    )

    data class FrameNode(
        val id: Long,
        val canvasObjectList: ArrayList<CanvasObject>,
        var prev: FrameNode? = null,
        var next: FrameNode? = null
    )

    companion object {
        private const val TOUCH_TOLERANCE = 4f
        private const val DEFAULT_INSTRUMENT_WIDTH = 4f
    }
}