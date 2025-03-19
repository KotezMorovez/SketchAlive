package com.morovez.sketchalive.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.morovez.sketchalive.databinding.ActivityMainBinding
import com.morovez.sketchalive.ui.views.panels.InstrumentSliderView
import com.morovez.sketchalive.ui.views.panels.PalettePanelView
import dagger.hilt.android.AndroidEntryPoint

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
            animationSlider = viewBinding.animationSlider
        )

        setContentView(viewBinding.root)
    }
}