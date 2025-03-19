package com.morovez.sketchalive.ui.views.panels

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.slider.Slider
import com.morovez.sketchalive.databinding.ViewSliderAnimationBinding
import kotlin.math.roundToInt

class AnimationSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var sliderListener: ((Float) -> Unit)? = null
    private val viewBinding = ViewSliderAnimationBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    init {
        addView(viewBinding.root)
    }

    fun setListener(listener: (Float) -> Unit) {
        sliderListener = listener
        applyListener()
    }

    fun showAnimationSlider(value: Float) {
        with(viewBinding) {
            root.isVisible = true
            root.isClickable = true

            slider.value = value
        }
    }

    fun hidePanel() {
        with(viewBinding.root) {
            isInvisible = true
            isClickable = false
        }
    }

    private fun applyListener() {
        viewBinding.slider.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    // Реагирует, когда событие касания ползунка запускается
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    // Реагирует, когда событие касания ползунка останавливается
                    sliderListener?.invoke(slider.value.roundToInt().toFloat())
                }
            }
        )
    }
}