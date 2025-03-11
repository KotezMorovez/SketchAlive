package com.morovez.sketchalive.ui

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.morovez.sketchalive.databinding.FragmentPaletteBinding
import com.morovez.sketchalive.ui.PaletteHandlerImpl.Colors
import com.morovez.sketchalive.ui.common.createPopUpWindow
import javax.inject.Inject

interface PaletteHandler {
    fun setPaletteBinding(paletteViewBinding: FragmentPaletteBinding)
    fun setPaletteListener(lambda: (Colors) -> Unit)
    fun getColor(): Int
    fun showPalette()
}

class PaletteHandlerImpl @Inject constructor() : PaletteHandler {
    private lateinit var paletteViewBinding: FragmentPaletteBinding
    private var currentARGBColor = Color.argb(255, 25, 118, 210)
    private var colorChooseListener: ((Colors) -> Unit)? = null

    override fun showPalette() {
        val popUpWindow = createPopUpWindow(paletteViewBinding.root)

        with(paletteViewBinding) {
            firstColor.setOnClickListener {
                currentARGBColor = Color.argb(255, 255, 255, 255)
                val hexColor = String.format("#%06X", 0xFFFFFF and currentARGBColor)
                colorChooseListener?.invoke(
                    Colors(
                        color = currentARGBColor,
                        tint = ColorStateList.valueOf(hexColor.toColorInt())
                    )
                )
                popUpWindow.dismiss()
            }

            secondColor.setOnClickListener {
                currentARGBColor = Color.argb(255, 255, 61, 0)
                val hexColor = String.format("#%06X", 0xFFFFFF and currentARGBColor)
                colorChooseListener?.invoke(
                    Colors(
                        color = currentARGBColor,
                        tint = ColorStateList.valueOf(hexColor.toColorInt())
                    )
                )
                popUpWindow.dismiss()
            }

            thirdColor.setOnClickListener {
                currentARGBColor = Color.argb(255, 0, 0, 0)
                val hexColor = String.format("#%06X", 0xFFFFFF and currentARGBColor)
                colorChooseListener?.invoke(
                    Colors(
                        color = currentARGBColor,
                        tint = ColorStateList.valueOf(hexColor.toColorInt())
                    )
                )
                popUpWindow.dismiss()
            }

            fourthColor.setOnClickListener {
                currentARGBColor = Color.argb(255, 25, 118, 210)
                val hexColor = String.format("#%06X", 0xFFFFFF and currentARGBColor)
                colorChooseListener?.invoke(
                    Colors(
                        color = currentARGBColor,
                        tint = ColorStateList.valueOf(hexColor.toColorInt())
                    )
                )
                popUpWindow.dismiss()
            }

            colorWheel.setColorChangeListener {
                currentARGBColor = it ?: Color.argb(255, 0, 0, 0)
                val hexColor = String.format("#%06X", 0xFFFFFF and currentARGBColor)
                colorChooseListener?.invoke(
                    Colors(
                        color = currentARGBColor,
                        tint = ColorStateList.valueOf(hexColor.toColorInt())
                    )
                )
            }
        }
    }

    override fun setPaletteBinding(paletteViewBinding: FragmentPaletteBinding) {
        this.paletteViewBinding = paletteViewBinding
    }

    override fun setPaletteListener(lambda: (Colors) -> Unit) {
        colorChooseListener = lambda
    }

    override fun getColor(): Int {
        return currentARGBColor
    }

    data class Colors(
        val color: Int,
        val tint: ColorStateList
    )
}