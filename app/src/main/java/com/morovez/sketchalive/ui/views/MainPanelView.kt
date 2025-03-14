package com.morovez.sketchalive.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.morovez.sketchalive.databinding.ViewPanelMainBinding

class MainPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var listener: ((MainPanelButtons) -> Unit)? = null
    private val viewBinding = ViewPanelMainBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    init {
        addView(viewBinding.root)
    }

    fun setListener(listener: (MainPanelButtons) -> Unit) {
        this.listener = listener
        applyListener()
    }

    fun showPanel() {
        with(viewBinding) {
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
            play.isClickable = true
            pause.isClickable = true
        }
    }

    fun hidePanel() {
        with(viewBinding) {
            back.isInvisible = true
            forward.isInvisible = true
            delete.isInvisible = true
            create.isInvisible = true
            layers.isInvisible = true
            play.isInvisible = true
            pause.isInvisible = true
            share.isInvisible = true

            back.isClickable = false
            forward.isClickable = false
            delete.isClickable = false
            create.isClickable = false
            layers.isClickable = false
            play.isClickable = false
            pause.isClickable = false
            share.isClickable = false
        }
    }

    private fun applyListener() {
        with(viewBinding) {
            back.setOnClickListener { listener?.invoke(MainPanelButtons.BACK) }
            forward.setOnClickListener { listener?.invoke(MainPanelButtons.FORWARD) }

            delete.setOnClickListener { listener?.invoke(MainPanelButtons.DELETE) }
            create.setOnClickListener { listener?.invoke(MainPanelButtons.CREATE) }
            layers.setOnClickListener { listener?.invoke(MainPanelButtons.LAYERS) }

            pause.setOnClickListener { listener?.invoke(MainPanelButtons.PAUSE) }
            play.setOnClickListener {
                listener?.invoke(MainPanelButtons.PLAY)
                play.isClickable = true
                pause.isClickable = true
                share.isClickable = true

                share.isVisible = true
                pause.isVisible = true
                play.isVisible = true
            }

            share.setOnClickListener { listener?.invoke(MainPanelButtons.SHARE) }
        }
    }
}

enum class MainPanelButtons {
    BACK,
    FORWARD,
    DELETE,
    CREATE,
    LAYERS,
    PAUSE,
    PLAY,
    SHARE
}