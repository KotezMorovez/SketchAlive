package com.morovez.sketchalive.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.morovez.sketchalive.R
import com.morovez.sketchalive.databinding.ViewPanelFiguresBinding
import com.morovez.sketchalive.databinding.ViewPanelInstrumentsBinding
import com.morovez.sketchalive.ui.common.createPopUpWindow

class InstrumentsPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val instrumentsViewBinding = ViewPanelInstrumentsBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )
    private val popUpInstrumentsBinding = ViewPanelFiguresBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )
    private var instrumentChooserListener: ((Instrument) -> Unit)? = null
    private var figureChooserListener: ((Figure) -> Unit)? = null
    private var currentButton: Instrument = Instrument.NONE

    init {
        addView(instrumentsViewBinding.root)
    }

    fun setColorTint(color: ColorStateList) {
        instrumentsViewBinding.color.backgroundTintList = color
    }

    fun hidePanel() {
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

    fun showPanel() {
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

    fun setListeners(
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
        if (currentButton == button && button != Instrument.INSTRUMENTS) {
            return
        }

        instrumentsViewBinding.let {
            setAllButtonsInactive()
            val colorActive = context.getColor(R.color.button_active)

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
                    it.color.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.color_active,
                            null
                        )
                    )
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
            val colorInactive = context.getColor(R.color.button_inactive)

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