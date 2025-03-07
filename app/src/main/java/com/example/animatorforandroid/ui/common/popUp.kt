package com.example.animatorforandroid.ui.common

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.example.animatorforandroid.utils.dpToPx

fun createPopUpWindow(instruments: View): PopupWindow {
    val popUpWindow = PopupWindow(
        instruments,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        true
    )

    popUpWindow.showAtLocation(
        instruments,
        Gravity.BOTTOM,
        0,
        dpToPx(124f, instruments.context)
    )
    return popUpWindow
}