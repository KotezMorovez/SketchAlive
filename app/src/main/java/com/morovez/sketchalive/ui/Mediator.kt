package com.morovez.sketchalive.ui

import android.graphics.Color
import com.morovez.sketchalive.ui.views.CanvasView
import com.morovez.sketchalive.ui.views.Colors
import com.morovez.sketchalive.ui.views.Figure
import com.morovez.sketchalive.ui.views.Instrument
import com.morovez.sketchalive.ui.views.InstrumentsPanelView
import com.morovez.sketchalive.ui.views.MainPanelButtons
import com.morovez.sketchalive.ui.views.MainPanelView
import com.morovez.sketchalive.ui.views.PalettePanelView
import com.morovez.sketchalive.ui.views.SliderView

class Mediator(
    private val mainPanel: MainPanelView,
    private val instrumentsPanel: InstrumentsPanelView,
    private val canvasView: CanvasView,
    private val palettePanel: PalettePanelView,
    private val sliderView: SliderView,
) {

    fun initialize() {
        initMainPanelListener()
        initInstrumentsListener()
        initPaletteListener()
        initSliderListener()
    }

    private fun initInstrumentsListener() {
        instrumentsPanel.setListeners(
            instrumentListener = { instrument ->
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
            },
            figureListener = { figure ->
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
        )
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

    private fun initMainPanelListener() {
        mainPanel.setListener { button ->
            when (button) {
                MainPanelButtons.BACK -> {
                    canvasView.undo()
                }

                MainPanelButtons.FORWARD -> {
                    canvasView.redo()
                }

                MainPanelButtons.DELETE -> {
                    canvasView.deleteFrame()
                }

                MainPanelButtons.CREATE -> {
                    canvasView.create()
                }

                MainPanelButtons.LAYERS -> {
//                    showLayersListPanels()
                }

                MainPanelButtons.PAUSE -> {
                    showDefaultPanels()
                    canvasView.pause()
                }

                MainPanelButtons.PLAY -> {
                    hideAllPanels()
                    // animationSlider.prepare()
                    canvasView.play()
                }

                MainPanelButtons.SHARE -> {
//                    gifLoader.isVisible = true
//                    lifecycleScope.launch(Dispatchers.Main) {
//                        loaderView.startLoader()
//                    }
//
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        val file = File(canvasView.generateGif())
//                        loaderView.stopLoader()
//
//                        launch(Dispatchers.Main) {
//                            gifLoader.isGone = true
//                            val uriToGif = FileProvider.getUriForFile(
//                                Objects.requireNonNull(applicationContext),
//                                "com.morovez.sketchalive.provider",
//                                file
//                            )
//                            val shareIntent = Intent(Intent.ACTION_SEND)
//                            shareIntent.type = "image/gif"
//                            shareIntent.putExtra(Intent.EXTRA_STREAM, uriToGif)
//                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//                            val chooserIntent = Intent.createChooser(shareIntent, null)
//                            this@MainActivity.startActivity(chooserIntent, null)
//                        }
//                    }
                }
            }
        }
    }

    private fun showDefaultPanels() {
        hideAllPanels()
        instrumentsPanel.showPanel()
        mainPanel.showPanel()
    }

    private fun hideAllPanels() {
        instrumentsPanel.hidePanel()
        mainPanel.hidePanel()
    }

    companion object {
        private const val DEFAULT_INSTRUMENT_WIDTH = 16F
    }
}