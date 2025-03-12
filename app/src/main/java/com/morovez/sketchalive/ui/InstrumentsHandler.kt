package com.morovez.sketchalive.ui

import android.content.res.ColorStateList
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.morovez.sketchalive.R
import com.morovez.sketchalive.databinding.FragmentFiguresBinding
import com.morovez.sketchalive.databinding.FragmentInstrumentsBinding
import com.morovez.sketchalive.ui.common.ResourceProvider
import com.morovez.sketchalive.ui.common.createPopUpWindow
import javax.inject.Inject

interface InstrumentsHandler {
    fun setBindings(
        instrumentsBinding: FragmentInstrumentsBinding,
        figuresBinding: FragmentFiguresBinding
    )

    fun setListeners(
        instrumentListener: (Instrument) -> Unit,
        figureListener: (Figure) -> Unit
    )

    fun setColorTint(color: ColorStateList)
    fun hidePanel()
    fun showPanel()
}

class InstrumentsHandlerImpl @Inject constructor(
    private val resourceProvider: ResourceProvider
) : InstrumentsHandler {
    private lateinit var instrumentsViewBinding: FragmentInstrumentsBinding
    private lateinit var popUpInstrumentsBinding: FragmentFiguresBinding
    private var instrumentChooserListener: ((Instrument) -> Unit)? = null
    private var figureChooserListener: ((Figure) -> Unit)? = null
    private var currentButton: Instrument = Instrument.NONE

    override fun setColorTint(color: ColorStateList) {
        instrumentsViewBinding.color.backgroundTintList = color
    }

    override fun hidePanel() {
        with(instrumentsViewBinding) {
            pencil.isInvisible = true
            brush.isInvisible = true
            erase.isInvisible = true
            instruments.isInvisible = true
            color.isInvisible = true

            pencil.isClickable = false
            brush.isClickable = false
            erase.isClickable = false
            instruments.isClickable = false
            color.isClickable = false
        }
    }

    override fun showPanel() {
        with(instrumentsViewBinding) {
            pencil.isVisible = true
            brush.isVisible = true
            erase.isVisible = true
            instruments.isVisible = true
            color.isVisible = true

            pencil.isClickable = true
            brush.isClickable = true
            erase.isClickable = true
            instruments.isClickable = true
            color.isClickable = true
        }
    }

    override fun setBindings(
        instrumentsBinding: FragmentInstrumentsBinding,
        figuresBinding: FragmentFiguresBinding
    ) {
        instrumentsViewBinding = instrumentsBinding
        popUpInstrumentsBinding = figuresBinding
    }

    override fun setListeners(
        instrumentListener: (Instrument) -> Unit,
        figureListener: (Figure) -> Unit
    ) {
        instrumentChooserListener = instrumentListener
        figureChooserListener = figureListener

        applyInstrumentsListener()
    }

    private fun applyInstrumentsListener() {
        with(instrumentsViewBinding) {
            pencil.setOnClickListener {
                setCurrentButton(Instrument.PENCIL)
                instrumentChooserListener?.invoke(Instrument.PENCIL)
            }

            brush.setOnClickListener {
                setCurrentButton(Instrument.BRUSH)
                instrumentChooserListener?.invoke(Instrument.BRUSH)
            }

            erase.setOnClickListener {
                setCurrentButton(Instrument.ERASE)
                instrumentChooserListener?.invoke(Instrument.ERASE)
            }

            instruments.setOnClickListener {
                setCurrentButton(Instrument.INSTRUMENTS)
                instrumentChooserListener?.invoke(Instrument.INSTRUMENTS)
            }

            color.setOnClickListener {
                setCurrentButton(Instrument.COLOR)
                instrumentChooserListener?.invoke(Instrument.COLOR)
            }
        }
    }

    private fun setCurrentButton(button: Instrument) {
        if (currentButton == button) {
            return
        }

        instrumentsViewBinding.let {
            setAllButtonsInactive()
            val colorActive = resourceProvider.getColor(R.color.button_active)

            when (button) {
                Instrument.PENCIL -> {
                    it.pencil.setColorFilter(colorActive)
                }

                Instrument.BRUSH -> {
                    it.brush.setColorFilter(colorActive)
                }

                Instrument.ERASE -> {
                    it.erase.setColorFilter(colorActive)
                }

                Instrument.INSTRUMENTS -> {
                    showFigures()
                    it.instruments.setColorFilter(colorActive)
                }

                Instrument.COLOR -> {
                    it.color.setImageDrawable(resourceProvider.getDrawable(R.drawable.color_active))
                }

                else -> {}
            }
        }

        currentButton = button
    }

    private fun showFigures() {
        val popUpWindow = createPopUpWindow(popUpInstrumentsBinding.root)

        with(popUpInstrumentsBinding) {
            rectangle.setOnClickListener {
                figureChooserListener?.invoke(Figure.RECTANGLE)
                popUpWindow.dismiss()
            }

            circle.setOnClickListener {
                figureChooserListener?.invoke(Figure.CIRCLE)
                popUpWindow.dismiss()
            }

            triangle.setOnClickListener {
                figureChooserListener?.invoke(Figure.TRIANGLE)
                popUpWindow.dismiss()
            }

            arrow.setOnClickListener {
                figureChooserListener?.invoke(Figure.ARROW)
                popUpWindow.dismiss()
            }
        }
    }

    private fun setAllButtonsInactive() {
        with(instrumentsViewBinding) {
            val colorInactive = resourceProvider.getColor(R.color.button_inactive)

            pencil.setColorFilter(colorInactive)
            brush.setColorFilter(colorInactive)
            erase.setColorFilter(colorInactive)
            instruments.setColorFilter(colorInactive)
            color.setImageDrawable(null)
        }
    }
}

enum class Instrument {
    NONE,
    PENCIL,
    BRUSH,
    ERASE,
    INSTRUMENTS,
    COLOR
}

enum class Figure {
    RECTANGLE,
    CIRCLE,
    TRIANGLE,
    ARROW
}