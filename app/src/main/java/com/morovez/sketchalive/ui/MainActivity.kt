package com.morovez.sketchalive.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.morovez.sketchalive.R
import com.morovez.sketchalive.databinding.ActivityMainBinding
import com.morovez.sketchalive.ui.common.CanvasView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Objects
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var layersPreview = listOf<Pair<CanvasView.FrameNode, Bitmap>>()
    private var layersPreviewList: ArrayList<ImageView> = arrayListOf()
    private var mediator: Mediator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        mediator = Mediator(
            instrumentsPanel = viewBinding.instruments,
            canvasView = viewBinding.canvasView,
            palettePanel = PalettePanelView(this)
        ).apply {
            initialize()
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
                            "com.morovez.sketchalive.provider",
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
        }
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
            viewBinding.instruments.hidePanel()

            back.isInvisible = true
            forward.isInvisible = true
            delete.isInvisible = true
            create.isInvisible = true
            layers.isInvisible = true

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
            viewBinding.instruments.showPanel()
            back.isVisible = true
            forward.isVisible = true
            delete.isVisible = true
            create.isVisible = true
            layers.isVisible = true

            play.isVisible = true
            pause.isVisible = true

            back.isClickable = true
            forward.isClickable = true
            delete.isClickable = true
            create.isClickable = true
            layers.isClickable = true
            animationSlider.isClickable = false
            play.isClickable = true
            pause.isClickable = true
        }
    }
}