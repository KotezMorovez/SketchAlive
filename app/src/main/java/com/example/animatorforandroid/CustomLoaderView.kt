package com.example.animatorforandroid

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.example.animatorforandroid.databinding.ViewCustomLoaderBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomLoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    private val viewBinding =
        ViewCustomLoaderBinding.inflate(LayoutInflater.from(context), this, false)
    private var isLoaderRun = false

    init {
        addView(viewBinding.root)
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomLoaderView,
            0,
            0
        )

        viewBinding.apply {
            val size = typedArray.getDimensionPixelSize(
                R.styleable.CustomLoaderView_dots_size,
                dpToPx(10f, context)
            )
            val margin = typedArray.getDimensionPixelSize(
                R.styleable.CustomLoaderView_dots_margin,
                dpToPx(4f,context)
            )

            val color = typedArray.getColor(
                R.styleable.CustomLoaderView_dots_color,
                resources.getColor(R.color.white, null)
            )

            setAttrs(progressDot1ImageView, size, margin, color)
            setAttrs(progressDot2ImageView, size, margin, color)
            setAttrs(progressDot3ImageView, size, margin, color)
        }
    }

    private fun setAttrs(view: ImageView, size: Int, margin: Int = 0, color: Int) {
        view.setColorFilter(color)

        view.updateLayoutParams {
            height = size
            width = size
        }

        val params = view.layoutParams as LayoutParams

        params.setMargins(margin, 0, margin, 0)

        view.layoutParams = params

        view.requestLayout()
    }

    suspend fun startLoader() {
        with(viewBinding) {
            val dotsArray: ArrayList<ImageView> = arrayListOf()
            dotsArray.add(progressDot1ImageView)
            dotsArray.add(progressDot2ImageView)
            dotsArray.add(progressDot3ImageView)
            var dot = 0
            isLoaderRun = true

            coroutineScope {
                while (isLoaderRun) {
                    when (dot) {
                        0 -> {
                            launch(Dispatchers.Main) {
                                setDotVisibility1(dotsArray[dot])
                                setDotVisibility05(dotsArray[dot + 1])
                                setDotVisibility025(dotsArray[dotsArray.size - 1])
                                dot++
                            }
                            delay(500L)
                            continue
                        }

                        dotsArray.size - 1 -> {
                            launch(Dispatchers.Main) {
                                setDotVisibility025(dotsArray[0])
                                setDotVisibility05(dotsArray[dot - 1])
                                setDotVisibility1(dotsArray[dot])
                                dot = 0
                            }
                            delay(500L)
                            continue
                        }

                        else -> {
                            launch(Dispatchers.Main) {
                                setDotVisibility05(dotsArray[dot - 1])
                                setDotVisibility1(dotsArray[dot])
                                setDotVisibility05(dotsArray[dot + 1])
                                dot++
                            }
                            delay(500L)
                            continue
                        }
                    }
                }
            }
        }
    }

    fun stopLoader() {
        isLoaderRun = false
    }

    private fun setDotVisibility1(dot: ImageView) {
        dot.animate().alpha(1.0f).duration = 400L
        dot.visibility = VISIBLE
    }

    private fun setDotVisibility05(dot: ImageView) {
        dot.animate().alpha(0.5f).duration = 400L
        dot.visibility = VISIBLE
    }

    private fun setDotVisibility025(dot: ImageView) {
        dot.animate().alpha(0.25f).duration = 400L
        dot.visibility = VISIBLE
    }
}