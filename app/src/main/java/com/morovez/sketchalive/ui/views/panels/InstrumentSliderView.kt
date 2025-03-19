package com.morovez.sketchalive.ui.views.panels

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.slider.Slider
import com.morovez.sketchalive.databinding.ViewSliderInstrumentBinding
import com.morovez.sketchalive.ui.common.createPopUpWindow
import kotlin.math.roundToInt

class InstrumentSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var sliderListener: ((Float) -> Unit)? = null
    private val popUpSliderBinding = ViewSliderInstrumentBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    init {
        addView(popUpSliderBinding.root)
    }

    fun setListener(listener: (Float) -> Unit) {
        sliderListener = listener
    }

    fun showSlider(currentValue: Float) {
        createPopUpWindow(this)
        popUpSliderBinding.slider.value = currentValue

        popUpSliderBinding.slider.setLabelFormatter {
            "${it.roundToInt()}"
        }

        popUpSliderBinding.slider.addOnSliderTouchListener(
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