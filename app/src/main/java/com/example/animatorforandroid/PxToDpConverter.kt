package com.example.animatorforandroid

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue

fun dpToPx(dp: Float, context: Context): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    ).toInt()

fun pxToDp(px: Float, context: Context): Int =
    (px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))
        .toInt()
