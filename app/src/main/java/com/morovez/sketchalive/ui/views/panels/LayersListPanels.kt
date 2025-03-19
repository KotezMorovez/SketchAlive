package com.morovez.sketchalive.ui.views.panels

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.morovez.sketchalive.R
import com.morovez.sketchalive.databinding.ViewPanelLayersListBottomBinding
import com.morovez.sketchalive.databinding.ViewPanelLayersListTopBinding
import com.morovez.sketchalive.ui.views.CanvasView

class LayersListTopPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var listener: ((LayersListTopPanelButtons) -> Unit)? = null
    private val viewBinding = ViewPanelLayersListTopBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )
    private var layersPreview = listOf<Pair<CanvasView.FrameNode, Bitmap>>()
    private var layersPreviewList: ArrayList<ImageView> = arrayListOf()
    private var activeFrame: CanvasView.FrameNode? = null
    private var index = 0

    init {
        addView(viewBinding.root)
    }

    fun setListener(listener: (LayersListTopPanelButtons) -> Unit) {
        this.listener = listener
    }

    fun hidePanel() {
        with(viewBinding.root) {
            isGone = true
        }
    }

    fun showLayersList(
        bitmapList: List<Pair<CanvasView.FrameNode, Bitmap>>,
        activeFrameNode: CanvasView.FrameNode
    ) {
        with(viewBinding) {
            root.isVisible = true

            layersPreviewList = arrayListOf(layer1, layer2, layer3)

            layersBack.setOnClickListener {
                layersBack.isEnabled = false
                listener?.invoke(LayersListTopPanelButtons.Back)
                layersBack.isEnabled = true
            }

            layersForward.setOnClickListener {
                layersForward.isEnabled = false
                listener?.invoke(LayersListTopPanelButtons.Forward)
                layersForward.isEnabled = true
            }

            updateListInfo(LayersList(bitmapList, activeFrameNode))
        }
    }

    fun updateListInfo(info: LayersList) {
        layersPreview = info.bitmapList
        activeFrame = info.activeFrameNode
        index = layersPreview.indexOfFirst {
            it.first.id == activeFrame!!.id
        }
        setAllPreviewsInactive()
        setFrame(layersPreviewList[index])

        invalidateLayersPreview()
    }

    private fun invalidateLayersPreview() {
        with(viewBinding) {
            layer1.background = layersPreview[0].second.toDrawable(resources)

            layer1.setOnClickListener {
                setFrame(layer1)
                listener?.invoke(LayersListTopPanelButtons.Layer(layersPreview[0].first))
            }

            if (layersPreview.size > 1) {
                layer2.background = layersPreview[1].second.toDrawable(resources)

                layer2.setOnClickListener {
                    setFrame(layer2)
                    listener?.invoke(LayersListTopPanelButtons.Layer(layersPreview[1].first))
                }
            } else {
                layer2.background = null
                layer2.setOnClickListener(null)
            }

            if (layersPreview.size > 2) {
                layer3.background = layersPreview[2].second.toDrawable(resources)

                layer3.setOnClickListener {
                    setFrame(layer3)
                    listener?.invoke(LayersListTopPanelButtons.Layer(layersPreview[2].first))
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

    private fun setFrame(layer: ImageView) {
        setAllPreviewsInactive()

        layer.setImageDrawable(
            ResourcesCompat.getDrawable(
                context.resources, R.drawable.layer_preview_active, null
            )
        )
    }
}

sealed interface LayersListTopPanelButtons {
    data object Back : LayersListTopPanelButtons
    data object Forward : LayersListTopPanelButtons
    data class Layer(val frameNode: CanvasView.FrameNode) : LayersListTopPanelButtons
}

data class LayersList(
    val bitmapList: List<Pair<CanvasView.FrameNode, Bitmap>>,
    val activeFrameNode: CanvasView.FrameNode
)

class LayersListBottomPanelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var listener: ((LayersListBottomPanelButtons) -> Unit)? = null
    private val viewBinding = ViewPanelLayersListBottomBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )

    init {
        addView(viewBinding.root)
    }

    fun setListener(listener: (LayersListBottomPanelButtons) -> Unit) {
        this.listener = listener
        applyListener()
    }

    fun showPanel() {
        with(viewBinding.layersListBottom) {
            isVisible = true
            isClickable = true
        }
    }

    fun hidePanel() {
        with(viewBinding.layersListBottom) {
            isGone = true
            isClickable = false
        }
    }

    private fun applyListener() {
        with(viewBinding) {
            done.setOnClickListener {
                listener?.invoke(LayersListBottomPanelButtons.DONE)
            }

            duplicate.setOnClickListener {
                listener?.invoke(LayersListBottomPanelButtons.DUPLICATE)
            }

            deleteAll.setOnClickListener {
                listener?.invoke(LayersListBottomPanelButtons.DELETE_ALL)
            }
        }
    }
}

enum class LayersListBottomPanelButtons {
    DUPLICATE,
    DELETE_ALL,
    DONE
}