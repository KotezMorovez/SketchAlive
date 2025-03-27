package com.morovez.sketchalive.ui.views.panels

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.morovez.sketchalive.databinding.ViewGifLoaderBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GifLoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var job: Job? = null
    private val viewBinding = ViewGifLoaderBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    init {
        addView(viewBinding.root)
    }

    fun show(scope: CoroutineScope) {
        with(viewBinding) {
            root.isVisible = true

            job = scope.launch {
                loaderView.startLoader()
            }
        }
    }

    fun hide() {
        with(viewBinding) {
            root.isGone = true
            loaderView.stopLoader()

            job?.cancel()
            job = null
        }
    }
}