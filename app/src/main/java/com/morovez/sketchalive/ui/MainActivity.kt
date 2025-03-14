package com.morovez.sketchalive.ui

import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import com.google.android.material.slider.Slider
import com.morovez.sketchalive.databinding.ActivityMainBinding
import com.morovez.sketchalive.ui.views.PalettePanelView
import com.morovez.sketchalive.ui.views.SliderView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var mediator: Mediator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        mediator = Mediator(
            mainPanel = viewBinding.mainPanel,
            instrumentsPanel = viewBinding.instruments,
            canvasView = viewBinding.canvasView,
            palettePanel = PalettePanelView(this),
            sliderView = SliderView(this)
        ).apply {
            initialize()
        }

        setContentView(viewBinding.root)
        initUI()
    }

    private fun initUI() {
        with(viewBinding) {
            canvasView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    val corner = 56f
                    outline?.setRoundRect(
                        0,
                        0,
                        view!!.width,
                        view.height,
                        corner
                    )
                }
            }
            canvasView.clipToOutline = true

            animationSlider.value = viewBinding.canvasView.getCurrentAnimationSpeed()
            animationSlider.addOnSliderTouchListener(
                object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                        // Реагирует, когда событие касания ползунка запускается
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        // Реагирует, когда событие касания ползунка останавливается
                        viewBinding.canvasView.setAnimationSpeed(
                            slider.value.roundToInt().toFloat()
                        )
                    }
                }
            )
        }
    }

    private fun hideAllPanels() {
        with(viewBinding) {
            viewBinding.instruments.hidePanel()

            animationSlider.isInvisible = true
            gifLoader.isGone = true
            loaderView.stopLoader()

            layersTopPanel.isGone = true
            layersBottomPanel.isGone = true

            animationSlider.isClickable = false
            layersTopPanel.isClickable = false
            layersBottomPanel.isClickable = false
        }
    }
}