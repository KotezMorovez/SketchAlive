package com.example.animatorforandroid

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.animatorforandroid.databinding.ActivityMainBinding
import com.example.animatorforandroid.databinding.FragmentInstrumentsBinding
import com.example.animatorforandroid.databinding.FragmentPaletteBinding

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var currentColor = R.color.blue
    private var currentButton = Buttons.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        initUI()
    }

    private fun initUI() {
        with(viewBinding) {
            back.setOnClickListener { canvasView.undo() }
            forward.setOnClickListener { canvasView.redo() }

            delete.setOnClickListener { canvasView.clear() }
            create.setOnClickListener { canvasView.create() }
            layers.setOnClickListener { showLayersList() }

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

            pause.setOnClickListener { AnimatorHandler.pause() }
            play.setOnClickListener { AnimatorHandler.play() }

            pencil.setOnClickListener {
                canvasView.setInstrument(CanvasView.Instrument.PENCIL)
                canvasView.setColor(resources.getColor(currentColor, null))

                setCurrentButton(Buttons.PENCIL)
            }

            brush.setOnClickListener {
                canvasView.setInstrument(CanvasView.Instrument.BRUSH)
                canvasView.setColor(resources.getColor(currentColor, null))

                setCurrentButton(Buttons.BRUSH)
            }

            erase.setOnClickListener {
                canvasView.setInstrument(CanvasView.Instrument.ERASE)
                canvasView.setColor(Color.TRANSPARENT)

                setCurrentButton(Buttons.ERASE)
            }

            instruments.setOnClickListener {
                showInstruments()
                setCurrentButton(Buttons.INSTRUMENTS)
                canvasView.setColor(resources.getColor(currentColor, null))
            }

            color.setOnClickListener {
                showPalette()
                setCurrentButton(Buttons.COLOR)
            }
        }
    }

    private fun setCurrentButton(button: Buttons) {
        if (currentButton == button) {
            return
        }
        viewBinding.let {
            when (currentButton) {
                Buttons.PENCIL -> {
                    it.pencil.setColorFilter(this.resources.getColor(R.color.button_inactive, null))
                }

                Buttons.BRUSH -> {
                    it.brush.setColorFilter(this.resources.getColor(R.color.button_inactive, null))
                }

                Buttons.ERASE -> {
                    it.erase.setColorFilter(this.resources.getColor(R.color.button_inactive, null))
                }

                Buttons.INSTRUMENTS -> {
                    it.instruments.setColorFilter(
                        this.resources.getColor(
                            R.color.button_inactive,
                            null
                        )
                    )
                }

                Buttons.COLOR -> {
                    it.color.setImageDrawable(null)
                }

                Buttons.PALETTE -> {
                    // TODO
                }

                else -> {}
            }

            when (button) {
                Buttons.PENCIL -> {
                    it.pencil.setColorFilter(this.resources.getColor(R.color.button_active, null))
                }

                Buttons.BRUSH -> {
                    it.brush.setColorFilter(this.resources.getColor(R.color.button_active, null))
                }

                Buttons.ERASE -> {
                    it.erase.setColorFilter(this.resources.getColor(R.color.button_active, null))
                }

                Buttons.INSTRUMENTS -> {
                    it.instruments.setColorFilter(
                        this.resources.getColor(
                            R.color.button_active,
                            null
                        )
                    )
                }

                Buttons.COLOR -> {
                    it.color.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            this.resources,
                            R.drawable.color_active,
                            null
                        )
                    )
                }

                Buttons.PALETTE -> {
                    // TODO
                }

                else -> {}
            }
        }

        currentButton = button
    }


    private fun showPalette() {
        val popUpPaletteBinding = FragmentPaletteBinding.inflate(layoutInflater)

        val popUpWindow = createPopUpWindow(popUpPaletteBinding.root)

        popUpPaletteBinding.firstColor.setOnClickListener {
            viewBinding.color.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.white, null))
            currentColor = R.color.white
            viewBinding.canvasView.setColor(resources.getColor(R.color.white,null))
            popUpWindow.dismiss()
        }

        popUpPaletteBinding.secondColor.setOnClickListener {
            viewBinding.color.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.orange, null))
            currentColor = R.color.orange
            viewBinding.canvasView.setColor(resources.getColor(R.color.orange,null))
            popUpWindow.dismiss()
        }

        popUpPaletteBinding.thirdColor.setOnClickListener {
            viewBinding.color.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.black, null))
            currentColor = R.color.black
            viewBinding.canvasView.setColor(resources.getColor(R.color.black,null))
            popUpWindow.dismiss()
        }

        popUpPaletteBinding.fourthColor.setOnClickListener {
            viewBinding.color.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.blue, null))
            currentColor = R.color.blue
            viewBinding.canvasView.setColor(resources.getColor(R.color.blue,null))
            popUpWindow.dismiss()
        }
    }

    private fun showInstruments() {
        val popUpInstrumentsBinding = FragmentInstrumentsBinding.inflate(layoutInflater)

        val popUpWindow = createPopUpWindow(popUpInstrumentsBinding.root)

        popUpInstrumentsBinding.rectangle.setOnClickListener {
            viewBinding.canvasView.setInstrument(CanvasView.Instrument.RECTANGLE)
            popUpWindow.dismiss()
        }
        popUpInstrumentsBinding.circle.setOnClickListener {
            viewBinding.canvasView.setInstrument(CanvasView.Instrument.CIRCLE)
            popUpWindow.dismiss()
        }
        popUpInstrumentsBinding.triangle.setOnClickListener {
            viewBinding.canvasView.setInstrument(CanvasView.Instrument.TRIANGLE)
            popUpWindow.dismiss()
        }
        popUpInstrumentsBinding.arrow.setOnClickListener {
            viewBinding.canvasView.setInstrument(CanvasView.Instrument.ARROW)
            popUpWindow.dismiss()
        }
    }

    private fun createPopUpWindow(view: View): PopupWindow {
        val popUpWindow = PopupWindow(
            view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popUpWindow.showAtLocation(
            viewBinding.instruments,
            Gravity.BOTTOM,
            0,
            dpToPx(124f, this)
        )
        return popUpWindow
    }

    private fun showLayersList() {}

    enum class Buttons {
        NONE,
        PENCIL,
        BRUSH,
        ERASE,
        INSTRUMENTS,
        COLOR,
        PALETTE
    }
}