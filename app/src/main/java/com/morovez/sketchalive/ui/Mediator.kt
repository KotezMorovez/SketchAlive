package com.morovez.sketchalive.ui

import android.graphics.Color
import com.morovez.sketchalive.ui.views.CanvasView
import com.morovez.sketchalive.ui.views.Colors
import com.morovez.sketchalive.ui.views.Figure
import com.morovez.sketchalive.ui.views.Instrument
import com.morovez.sketchalive.ui.views.InstrumentsPanelView
import com.morovez.sketchalive.ui.views.PalettePanelView
import com.morovez.sketchalive.ui.views.SliderView

class Mediator(
    private val instrumentsPanel: InstrumentsPanelView,
    private val canvasView: CanvasView,
    private val palettePanel: PalettePanelView,
    private val sliderView: SliderView
) {

    fun initialize() {
        instrumentsPanel.setListeners(
            initInstrumentsListener(),
            initFiguresListener(),
        )

        initPaletteListener()
        initSliderListener()
    }

    private fun initInstrumentsListener(): (Instrument) -> Unit {
        return { instrument ->
            when (instrument) {
                Instrument.PENCIL -> {
                    canvasView.setInstrument(CanvasView.Instrument.PENCIL)
                    val currentColor = palettePanel.getColor()
                    canvasView.setColor(currentColor)
                    sliderView.showSlider(canvasView.getCurrentInstrumentWidth())
                }

                Instrument.BRUSH -> {
                    canvasView.setInstrument(CanvasView.Instrument.BRUSH)
                    val currentColor = palettePanel.getColor()
                    canvasView.setColor(currentColor)
                    sliderView.showSlider(canvasView.getCurrentInstrumentWidth())
                }

                Instrument.ERASE -> {
                    canvasView.setInstrument(CanvasView.Instrument.ERASE)
                    canvasView.setColor(Color.TRANSPARENT)
                    sliderView.showSlider(canvasView.getCurrentInstrumentWidth())
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
                    canvasView.setInstrument(CanvasView.Instrument.RECTANGLE)
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

    private fun initPaletteListener() {
        palettePanel.setPaletteListener { colors: Colors ->
            val colorStateList = colors.tint
            val currentColor = colors.color

            instrumentsPanel.setColorTint(colorStateList)
            canvasView.setColor(currentColor)
        }
    }

    private fun initSliderListener() {
        sliderView.setListener { value ->
           canvasView.setInstrumentWidth(value)
        }
    }

    companion object {
        private const val DEFAULT_INSTRUMENT_WIDTH = 16F
    }
}