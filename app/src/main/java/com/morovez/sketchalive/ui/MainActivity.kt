package com.morovez.sketchalive.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.morovez.sketchalive.databinding.ActivityMainBinding
import com.morovez.sketchalive.ui.views.CanvasView.FrameNode
import com.morovez.sketchalive.ui.views.panels.InstrumentSliderView
import com.morovez.sketchalive.ui.views.panels.PalettePanelView
import com.morovez.sketchalive.utils.AnimatedGIFWriter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Objects

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var mediator: Mediator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        mediator = Mediator(
            mainPanel = viewBinding.mainPanel,
            instrumentsPanel = viewBinding.instruments,
            canvasView = viewBinding.canvasView,
            palettePanel = PalettePanelView(this),
            instrumentSliderView = InstrumentSliderView(this),
            layersListTopPanel = viewBinding.layersTopPanel,
            layersListBottomPanel = viewBinding.layersBottomPanel,
            animationSlider = viewBinding.animationSlider,
            gifLoader = viewBinding.gifLoader,
            listener = { rootFrameNode ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val file = File(generateGif(rootFrameNode))

                    launch(Dispatchers.Main) {
                        mediator?.stopLoader()

                        val uriToGif = FileProvider.getUriForFile(
                            Objects.requireNonNull(this@MainActivity),
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
        )

        setContentView(viewBinding.root)
    }

    private fun generateGif(rootFrameNode: FrameNode): String {
        var actualFrameNode: FrameNode? = rootFrameNode
        val root = this@MainActivity.cacheDir.absolutePath + "/animator_gif_cache"
        val cacheUri = "$root/animation.gif"
        val dir = File(root)

        dir.mkdir()
        val writer = AnimatedGIFWriter(true)
        val output = FileOutputStream(cacheUri)
        writer.prepareForWrite(output, -1, -1)

        do {
            val bitmap = viewBinding.canvasView.createBitmapFullSize(actualFrameNode!!)
            writer.writeFrame(output, bitmap)
            actualFrameNode = actualFrameNode.next
        } while (actualFrameNode != null)

        writer.finishWrite(output)
        return cacheUri
    }
}