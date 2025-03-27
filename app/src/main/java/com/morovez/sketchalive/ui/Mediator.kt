package com.morovez.sketchalive.ui

import android.graphics.Color
import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import com.morovez.sketchalive.ui.views.CanvasView
import com.morovez.sketchalive.ui.views.panels.AnimationSliderView
import com.morovez.sketchalive.ui.views.panels.Colors
import com.morovez.sketchalive.ui.views.panels.Figure
import com.morovez.sketchalive.ui.views.panels.GifLoaderView
import com.morovez.sketchalive.ui.views.panels.Instrument
import com.morovez.sketchalive.ui.views.panels.InstrumentSliderView
import com.morovez.sketchalive.ui.views.panels.InstrumentsPanelView
import com.morovez.sketchalive.ui.views.panels.LayersList
import com.morovez.sketchalive.ui.views.panels.LayersListBottomPanelButtons
import com.morovez.sketchalive.ui.views.panels.LayersListBottomPanelView
import com.morovez.sketchalive.ui.views.panels.LayersListTopPanelButtons
import com.morovez.sketchalive.ui.views.panels.LayersListTopPanelView
import com.morovez.sketchalive.ui.views.panels.MainPanelButtons
import com.morovez.sketchalive.ui.views.panels.MainPanelView
import com.morovez.sketchalive.ui.views.panels.PalettePanelView
import kotlinx.coroutines.GlobalScope

class Mediator(
    private val mainPanel: MainPanelView,
    private val instrumentsPanel: InstrumentsPanelView,
    private val canvasView: CanvasView,
    private val palettePanel: PalettePanelView,
    private val instrumentSliderView: InstrumentSliderView,
    private val layersListTopPanel: LayersListTopPanelView,
    private val layersListBottomPanel: LayersListBottomPanelView,
    private val animationSlider: AnimationSliderView,
    private val gifLoader: GifLoaderView,
    private val listener: (CanvasView.FrameNode) -> Unit
) {
    init {
        initCanvasView()
        initMainPanelListener()
        initInstrumentsListener()
        initPaletteListener()
        initSliderListener()
        initLayersListPanelsListener()
        initAnimationSliderListener()
    }

    fun stopLoader() {
        gifLoader.hide()
    }

    private fun initCanvasView() {
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
    }

    private fun initInstrumentsListener() {
        instrumentsPanel.setListeners(
            instrumentListener = { instrument ->
                when (instrument) {
                    Instrument.PENCIL -> {
                        canvasView.setInstrument(CanvasView.Instrument.PENCIL)
                        val currentColor = palettePanel.getColor()
                        canvasView.setColor(currentColor)
                        instrumentSliderView.showSlider(canvasView.getCurrentInstrumentWidth())
                    }

                    Instrument.BRUSH -> {
                        canvasView.setInstrument(CanvasView.Instrument.BRUSH)
                        val currentColor = palettePanel.getColor()
                        canvasView.setColor(currentColor)
                        instrumentSliderView.showSlider(canvasView.getCurrentInstrumentWidth())
                    }

                    Instrument.ERASE -> {
                        canvasView.setInstrument(CanvasView.Instrument.ERASE)
                        canvasView.setColor(Color.TRANSPARENT)
                        instrumentSliderView.showSlider(canvasView.getCurrentInstrumentWidth())
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
        instrumentSliderView.setListener { value ->
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
                    showLayersListPanels(getLayersList())
                }

                MainPanelButtons.PAUSE -> {
                    showDefaultPanels()
                    canvasView.pause()
                }

                MainPanelButtons.PLAY -> {
                    hideAllPanels()
                    animationSlider.showAnimationSlider(canvasView.getCurrentAnimationSpeed())
                    canvasView.play()
                }

                MainPanelButtons.SHARE -> {
                    gifLoader.show(GlobalScope)
                    listener.invoke(canvasView.rootFrameNode)
                }
            }
        }
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

    private fun initAnimationSliderListener() {
        animationSlider.setListener { value ->
            canvasView.setAnimationSpeed(value)
        }
    }

    private fun hideAllPanels() {
        instrumentsPanel.hidePanel()
        mainPanel.hidePanel()
        layersListTopPanel.hidePanel()
        layersListBottomPanel.hidePanel()
        animationSlider.hidePanel()
        gifLoader.hide()
    }

    private fun showLayersListPanels(
        layersList: LayersList
    ) {
        layersListTopPanel.showLayersList(layersList.bitmapList, layersList.activeFrameNode)
        layersListBottomPanel.showPanel()
    }

    private fun showDefaultPanels() {
        hideAllPanels()
        instrumentsPanel.showPanel()
        mainPanel.showPanel()
    }

    private fun getLayersList() = LayersList(
        bitmapList = canvasView.getBitmapList(),
        activeFrameNode = canvasView.activeFrameNode
    )

    companion object {
        private const val DEFAULT_INSTRUMENT_WIDTH = 16F
    }
}