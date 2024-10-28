package com.example.animatorforandroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class CanvasView(
    context: Context,
    private val getChosenColor: () -> Int,
    private val getPaintStyle: () -> Paint.Style,
    private val getChosenShape: () -> Shape
) : View(context) {
    private val paint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = getChosenColor.invoke()
        paint.style = getPaintStyle.invoke()

        val shape = getChosenShape.invoke()
        when (shape) {
            Shape.CIRCLE -> {
                canvas.drawCircle()
            }
            Shape.TRIANGLE -> {
                canvas.drawPicture()
            }
            Shape.RECTANGLE -> {
                canvas.drawRect()
            }
            else -> {}
        }
    }


}