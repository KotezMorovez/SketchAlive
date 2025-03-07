package com.example.animatorforandroid.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.animatorforandroid.ui.common.CanvasView
import com.example.animatorforandroid.R
import com.example.animatorforandroid.databinding.ActivityMainBinding
import com.example.animatorforandroid.databinding.FragmentInstrumentsBinding
import com.example.animatorforandroid.databinding.FragmentPaletteBinding
import com.example.animatorforandroid.databinding.FragmentSliderForInstrumentBinding
import com.example.animatorforandroid.utils.dpToPx
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Objects
import kotlin.math.roundToInt
import androidx.core.graphics.drawable.toDrawable
import com.example.animatorforandroid.ui.common.createPopUpWindow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    @Inject
    private lateinit var paletteHandler: PaletteHandler
    private var currentButton = Buttons.NONE
    private var layersPreview = listOf<Pair<CanvasView.FrameNode, Bitmap>>()
    private var layersPreviewList: ArrayList<ImageView> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        val paletteBinding = FragmentPaletteBinding.inflate(layoutInflater)
        paletteHandler.apply {
            setPaletteBinding(paletteBinding)
            setListener { colors: PaletteHandlerImpl.Colors ->
                val colorStateList = colors.tint
                val currentColor = colors.color
                viewBinding.color.backgroundTintList = colorStateList
                viewBinding.canvasView.setColor(currentColor)
            }

        }
        setContentView(viewBinding.root)

        initUI()
    }

    private fun initUI() {
        layersPreviewList = arrayListOf(
            viewBinding.layer1,
            viewBinding.layer2,
            viewBinding.layer3
        )

        with(viewBinding) {
            back.setOnClickListener { canvasView.undo() }
            forward.setOnClickListener { canvasView.redo() }

            delete.setOnClickListener { canvasView.deleteFrame() }
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

            animationSlider.value = viewBinding.canvasView.getCurrentAnimationSpeed()
            animationSlider.addOnSliderTouchListener(
                object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {
                        // Реагирует, когда событие касания ползунка запускается
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        // Реагирует, когда событие касания ползунка останавливается
                        viewBinding.canvasView.setAnimationSpeed(
                            slider.value.roundToInt().toFloat()
                        )
                    }
                }
            )

            pause.setOnClickListener {
                showDefaultPanels()
                canvasView.pause()
            }

            play.setOnClickListener {
                hideAllPanels()

                play.isClickable = true
                pause.isClickable = true
                animationSlider.isClickable = true
                share.isClickable = true

                animationSlider.isVisible = true
                share.isVisible = true
                pause.isVisible = true
                play.isVisible = true

                canvasView.play()
            }

            share.setOnClickListener {
                gifLoader.isVisible = true
                lifecycleScope.launch(Dispatchers.Main) {
                    loaderView.startLoader()
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    val file = File(canvasView.generateGif())
                    loaderView.stopLoader()

                    launch(Dispatchers.Main) {
                        gifLoader.isGone = true
                        val uriToGif = FileProvider.getUriForFile(
                            Objects.requireNonNull(applicationContext),
                            "com.example.animatorforandroid.provider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "image/gif"
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uriToGif)
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        val chooserIntent = Intent.createChooser(shareIntent, null)
                        this@MainActivity.startActivity(chooserIntent, null)
                    }
                }
            }

            pencil.setOnClickListener {
                canvasView.setInstrument(CanvasView.Instrument.PENCIL)
                val currentColor = paletteHandler.getColor()
                canvasView.setColor(currentColor)

                setCurrentButton(Buttons.PENCIL)
                showSlider()
            }

            brush.setOnClickListener {
                canvasView.setInstrument(CanvasView.Instrument.BRUSH)
                val currentColor = paletteHandler.getColor()
                canvasView.setColor(currentColor)

                setCurrentButton(Buttons.BRUSH)
                showSlider()
            }

            erase.setOnClickListener {
                canvasView.setInstrument(CanvasView.Instrument.ERASE)
                canvasView.setColor(Color.TRANSPARENT)

                setCurrentButton(Buttons.ERASE)
                showSlider()
            }

            instruments.setOnClickListener {
                showInstruments()
                setCurrentButton(Buttons.INSTRUMENTS)
                val currentColor = paletteHandler.getColor()
                canvasView.setColor(currentColor)
            }

            color.setOnClickListener {
                paletteHandler.showPalette()
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

                else -> {}
            }
        }

        currentButton = button
    }

    private fun showLayersList() {
        layersPreview = viewBinding.canvasView.getBitmapList()
        var activeFrame = viewBinding.canvasView.activeFrameNode
        setAllPreviewsInactive()
        invalidateLayersPreview()
        var index = layersPreview.indexOfFirst {
            it.first.id == activeFrame.id
        }

        layersPreviewList[index].setImageDrawable(
            ResourcesCompat.getDrawable(
                this@MainActivity.resources,
                R.drawable.layer_preview_active,
                null
            )
        )

        with(viewBinding) {
            hideAllPanels()
            layersTopPanel.isVisible = true
            layersBottomPanel.isVisible = true
            layersTopPanel.isClickable = true
            layersBottomPanel.isClickable = true

            layersBack.setOnClickListener {
                layersBack.isEnabled = false
                viewBinding.canvasView.moveBack()
                layersPreview = viewBinding.canvasView.getBitmapList()
                activeFrame = viewBinding.canvasView.activeFrameNode
                index = layersPreview.indexOfFirst {
                    it.first == activeFrame
                }
                setAllPreviewsInactive()
                layersPreviewList[index].setImageDrawable(
                    ResourcesCompat.getDrawable(
                        this@MainActivity.resources,
                        R.drawable.layer_preview_active,
                        null
                    )
                )
                invalidateLayersPreview()
                layersBack.isEnabled = true
            }
            layersForward.setOnClickListener {
                layersForward.isEnabled = false
                viewBinding.canvasView.moveForward()
                layersPreview = viewBinding.canvasView.getBitmapList()
                activeFrame = viewBinding.canvasView.activeFrameNode
                index = layersPreview.indexOfFirst {
                    it.first == activeFrame
                }
                setAllPreviewsInactive()
                layersPreviewList[index].setImageDrawable(
                    ResourcesCompat.getDrawable(
                        this@MainActivity.resources,
                        R.drawable.layer_preview_active,
                        null
                    )
                )
                invalidateLayersPreview()
                layersForward.isEnabled = true
            }

            duplicate.setOnClickListener {
                viewBinding.canvasView.duplicateActiveFrame()
                layersPreview = viewBinding.canvasView.getBitmapList()
                invalidateLayersPreview()
            }
            deleteAll.setOnClickListener {
                viewBinding.canvasView.deleteAllFrames()
                layersPreview = viewBinding.canvasView.getBitmapList()
                setAllPreviewsInactive()
                layersPreviewList[0].setImageDrawable(
                    ResourcesCompat.getDrawable(
                        this@MainActivity.resources,
                        R.drawable.layer_preview_active,
                        null
                    )
                )
                invalidateLayersPreview()
            }

            done.setOnClickListener {
                showDefaultPanels()
                canvasView.closeLayersManager()
            }
        }
    }

    private fun showInstruments() {
        val popUpInstrumentsBinding = FragmentInstrumentsBinding.inflate(layoutInflater)

        val popUpWindow = createPopUpWindow(popUpInstrumentsBinding.root)

        popUpInstrumentsBinding.rectangle.setOnClickListener {
            viewBinding.canvasView.setInstrument(CanvasView.Instrument.RECTANGLE)
            viewBinding.canvasView.setInstrumentWidth(16f)
            popUpWindow.dismiss()
        }
        popUpInstrumentsBinding.circle.setOnClickListener {
            viewBinding.canvasView.setInstrument(CanvasView.Instrument.CIRCLE)
            viewBinding.canvasView.setInstrumentWidth(16f)
            popUpWindow.dismiss()
        }
        popUpInstrumentsBinding.triangle.setOnClickListener {
            viewBinding.canvasView.setInstrument(CanvasView.Instrument.TRIANGLE)
            viewBinding.canvasView.setInstrumentWidth(16f)
            popUpWindow.dismiss()
        }
        popUpInstrumentsBinding.arrow.setOnClickListener {
            viewBinding.canvasView.setInstrument(CanvasView.Instrument.ARROW)
            viewBinding.canvasView.setInstrumentWidth(16f)
            popUpWindow.dismiss()
        }
    }

    private fun showSlider() {
        val popUpSliderBinding = FragmentSliderForInstrumentBinding.inflate(layoutInflater)

        createPopUpWindow(popUpSliderBinding.root)

        popUpSliderBinding.slider.value = viewBinding.canvasView.getCurrentInstrumentWidth()
        popUpSliderBinding.slider.setLabelFormatter {
            "${it.roundToInt()}"
        }
        popUpSliderBinding.slider.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    // Реагирует, когда событие касания ползунка запускается
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    // Реагирует, когда событие касания ползунка останавливается
                    viewBinding.canvasView.setInstrumentWidth(slider.value.roundToInt().toFloat())
                }
            }
        )
    }

    private fun invalidateLayersPreview() {
        with(viewBinding) {
            layer1.background = layersPreview[0].second.toDrawable(resources)

            layer1.setOnClickListener {
                setAllPreviewsInactive()
                layer1.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        this@MainActivity.resources,
                        R.drawable.layer_preview_active,
                        null
                    )
                )

                viewBinding.canvasView.showFrame(layersPreview[0].first)
            }

            if (layersPreview.size > 1) {
                layer2.background = layersPreview[1].second.toDrawable(resources)

                layer2.setOnClickListener {
                    setAllPreviewsInactive()
                    layer2.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            this@MainActivity.resources,
                            R.drawable.layer_preview_active,
                            null
                        )
                    )

                    viewBinding.canvasView.showFrame(layersPreview[1].first)
                }
            } else {
                layer2.background = null
                layer2.setOnClickListener(null)
            }

            if (layersPreview.size > 2) {
                layer3.background = layersPreview[2].second.toDrawable(resources)

                layer3.setOnClickListener {
                    setAllPreviewsInactive()
                    layer3.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            this@MainActivity.resources,
                            R.drawable.layer_preview_active,
                            null
                        )
                    )

                    viewBinding.canvasView.showFrame(layersPreview[2].first)
                }
            } else {
                layer3.background = null
                layer3.setOnClickListener(null)
            }
        }
    }

    private fun setAllPreviewsInactive() {
        with(viewBinding) {
            layer1.setImageDrawable(null)
            layer2.setImageDrawable(null)
            layer3.setImageDrawable(null)
        }
    }

    private fun hideAllPanels() {
        with(viewBinding) {
            back.isInvisible = true
            forward.isInvisible = true
            delete.isInvisible = true
            create.isInvisible = true
            layers.isInvisible = true

            pencil.isInvisible = true
            brush.isInvisible = true
            erase.isInvisible = true
            instruments.isInvisible = true
            color.isInvisible = true

            animationSlider.isInvisible = true
            share.isInvisible = true
            play.isInvisible = true
            pause.isInvisible = true
            gifLoader.isGone = true
            loaderView.stopLoader()

            layersTopPanel.isGone = true
            layersBottomPanel.isGone = true

            back.isClickable = false
            forward.isClickable = false
            delete.isClickable = false
            create.isClickable = false

            layers.isClickable = false
            pencil.isClickable = false
            brush.isClickable = false
            erase.isClickable = false
            instruments.isClickable = false
            color.isClickable = false

            animationSlider.isClickable = false
            share.isClickable = false
            play.isClickable = false
            pause.isClickable = false

            layersTopPanel.isClickable = false
            layersBottomPanel.isClickable = false
        }
    }

    private fun showDefaultPanels() {
        with(viewBinding) {
            hideAllPanels()
            back.isVisible = true
            forward.isVisible = true
            delete.isVisible = true
            create.isVisible = true
            layers.isVisible = true

            pencil.isVisible = true
            brush.isVisible = true
            erase.isVisible = true
            instruments.isVisible = true
            color.isVisible = true

            play.isVisible = true
            pause.isVisible = true

            back.isClickable = true
            forward.isClickable = true
            delete.isClickable = true
            create.isClickable = true
            layers.isClickable = true
            pencil.isClickable = true
            brush.isClickable = true
            erase.isClickable = true
            instruments.isClickable = true
            color.isClickable = true
            animationSlider.isClickable = false
            play.isClickable = true
            pause.isClickable = true
        }
    }

    enum class Buttons {
        NONE,
        PENCIL,
        BRUSH,
        ERASE,
        INSTRUMENTS,
        COLOR
    }
}