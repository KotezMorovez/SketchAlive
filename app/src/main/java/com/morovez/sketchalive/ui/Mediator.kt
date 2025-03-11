package com.morovez.sketchalive.ui

import android.graphics.Color
import com.morovez.sketchalive.ui.common.CanvasView
import com.morovez.sketchalive.ui.common.Figure
import com.morovez.sketchalive.ui.common.Instrument
import com.morovez.sketchalive.ui.common.InstrumentsPanelView
import javax.inject.Inject

interface Mediator {
    fun initMediator()
}

class MediatorImpl @Inject constructor(
    val instrumentsPanel: InstrumentsPanelView,
    val palettePanel: PaletteHandler,
    val canvasView: CanvasView
) : Mediator {

    override fun initMediator() {
        instrumentsPanel.setListeners(
            initInstrumentsListener(),
            initFiguresListener(),
//            initSliderListener(),
//            initPaletteListener(),
//            initCanvasListener()
        )
    }

    private fun initInstrumentsListener(): (Instrument) -> Unit {
        return { instrument ->
            when (instrument) {
                Instrument.PENCIL -> {
                    canvasView.setInstrument(CanvasView.Instrument.PENCIL)
                    val currentColor = palettePanel.getColor()
                    canvasView.setColor(currentColor)
//                    showSlider()
                }

                Instrument.BRUSH -> {
                    canvasView.setInstrument(CanvasView.Instrument.BRUSH)
                    val currentColor = palettePanel.getColor()
                    canvasView.setColor(currentColor)
//                    showSlider()

                }

                Instrument.ERASE -> {
                    canvasView.setInstrument(CanvasView.Instrument.ERASE)
                    canvasView.setColor(Color.TRANSPARENT)
//                    showSlider()
                }

                Instrument.INSTRUMENTS -> {
                    val currentColor = palettePanel.getColor()
                    canvasView.setColor(currentColor)
                }

                Instrument.COLOR -> {
                    palettePanel.showPalette()
                }

                else -> {}
            }
        }
    }

    private fun initFiguresListener(): (Figure) -> Unit {
        return { figure ->
            when (figure) {
                Figure.RECTANGLE -> {
                    canvasView.setInstrument(CanvasView.Instrument.CIRCLE)
                    canvasView.setInstrumentWidth(DEFAULT_INSTRUMENT_WIDTH)
                }

                Figure.CIRCLE -> {
                    canvasView.setInstrument(CanvasView.Instrument.CIRCLE)
                    canvasView.setInstrumentWidth(DEFAULT_INSTRUMENT_WIDTH)
                }

                Figure.ARROW -> {
                    canvasView.setInstrument(CanvasView.Instrument.ARROW)
                    canvasView.setInstrumentWidth(DEFAULT_INSTRUMENT_WIDTH)
                }

                Figure.TRIANGLE -> {
                    canvasView.setInstrument(CanvasView.Instrument.TRIANGLE)
                    canvasView.setInstrumentWidth(DEFAULT_INSTRUMENT_WIDTH)
                }
            }

        }
    }

//    private fun showSlider() {
//        val popUpSliderBinding = FragmentSliderForInstrumentBinding.inflate(layoutInflater)
//
//        createPopUpWindow(popUpSliderBinding.root)
//
//        popUpSliderBinding.slider.value = viewBinding.canvasView.getCurrentInstrumentWidth()
//        popUpSliderBinding.slider.setLabelFormatter {
//            "${it.roundToInt()}"
//        }
//        popUpSliderBinding.slider.addOnSliderTouchListener(
//            object : Slider.OnSliderTouchListener {
//                override fun onStartTrackingTouch(slider: Slider) {
//                    // Реагирует, когда событие касания ползунка запускается
//                }
//
//                override fun onStopTrackingTouch(slider: Slider) {
//                    // Реагирует, когда событие касания ползунка останавливается
//                    viewBinding.canvasView.setInstrumentWidth(slider.value.roundToInt().toFloat())
//                }
//            }
//        )
//    }

    companion object {
        private const val DEFAULT_INSTRUMENT_WIDTH = 16F
    }
}