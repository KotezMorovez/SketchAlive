package com.morovez.sketchalive.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import com.morovez.sketchalive.databinding.ViewPanelPaletteBinding
import com.morovez.sketchalive.ui.common.createPopUpWindow

class PalettePanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val paletteViewBinding = ViewPanelPaletteBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )
    private var currentARGBColor = Color.argb(255, 25, 118, 210)
    private var colorChooseListener: ((Colors) -> Unit)? = null

    init {
        addView(paletteViewBinding.root)
    }

    fun showPalette() {
        val popUpWindow = createPopUpWindow(this)

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

    fun setPaletteListener(lambda: (Colors) -> Unit) {
        colorChooseListener = lambda
    }

    fun getColor(): Int {
        return currentARGBColor
    }

}

data class Colors(
    val color: Int,
    val tint: ColorStateList
)