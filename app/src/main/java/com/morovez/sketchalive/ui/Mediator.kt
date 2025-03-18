package com.morovez.sketchalive.ui

import android.graphics.Bitmap
import android.graphics.Color
import com.morovez.sketchalive.ui.views.CanvasView
import com.morovez.sketchalive.ui.views.Colors
import com.morovez.sketchalive.ui.views.Figure
import com.morovez.sketchalive.ui.views.Instrument
import com.morovez.sketchalive.ui.views.InstrumentsPanelView
import com.morovez.sketchalive.ui.views.LayersList
import com.morovez.sketchalive.ui.views.LayersListBottomPanelButtons
import com.morovez.sketchalive.ui.views.LayersListBottomPanelView
import com.morovez.sketchalive.ui.views.LayersListTopPanelButtons
import com.morovez.sketchalive.ui.views.LayersListTopPanelView
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
    private val layersListTopPanel: LayersListTopPanelView,
    private val layersListBottomPanel: LayersListBottomPanelView
) {

    fun initialize() {
        initMainPanelListener()
        initInstrumentsListener()
        initPaletteListener()
        initSliderListener()
        initLayersListPanelsListener()
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
                    hideAllPanels()
                    showLayersListPanels(
                        canvasView.getBitmapList(),
                        canvasView.activeFrameNode
                    )
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

    private fun hideAllPanels() {
        instrumentsPanel.hidePanel()
        mainPanel.hidePanel()
        layersListTopPanel.hidePanel()
        layersListBottomPanel.hidePanel()

//        animationSlider.isInvisible = true
//        gifLoader.isGone = true
//        loaderView.stopLoader()
//
//        animationSlider.isClickable = false
    }

    private fun initLayersListPanelsListener() {
        val topPanelListener: (LayersListTopPanelButtons) -> Unit = { button ->
            when (button) {
                LayersListTopPanelButtons.Back -> {
                    canvasView.moveBack()
                }

                is LayersListTopPanelButtons.Layer -> {
                    canvasView.showFrame(button.frameNode)
                }

                LayersListTopPanelButtons.Forward -> {
                    canvasView.moveForward()
                }
            }

            layersListTopPanel.updateListInfo(getLayersList())
        }

        layersListTopPanel.setListener(topPanelListener)

        layersListBottomPanel.setListener { button ->
            when (button) {
                LayersListBottomPanelButtons.DONE -> {
                    canvasView.closeLayersManager()
                    showDefaultPanels()
                }

                LayersListBottomPanelButtons.DUPLICATE -> {
                    canvasView.duplicateActiveFrame()
                    layersListTopPanel.updateListInfo(getLayersList())
                }

                LayersListBottomPanelButtons.DELETE_ALL -> {
                    canvasView.deleteAllFrames()
                    layersListTopPanel.updateListInfo(getLayersList())
                }
            }
        }
    }

    private fun getLayersList() = LayersList(
        bitmapList = canvasView.getBitmapList(),
        activeFrameNode = canvasView.activeFrameNode
    )

    private fun showLayersListPanels(
        bitmapList: List<Pair<CanvasView.FrameNode, Bitmap>>,
        activeFrameNode: CanvasView.FrameNode
    ) {
        layersListTopPanel.showLayersList(bitmapList, activeFrameNode)
        layersListBottomPanel.showPanel()
    }

    private fun showDefaultPanels() {
        hideAllPanels()
        instrumentsPanel.showPanel()
        mainPanel.showPanel()
    }

    companion object {
        private const val DEFAULT_INSTRUMENT_WIDTH = 16F
    }
}