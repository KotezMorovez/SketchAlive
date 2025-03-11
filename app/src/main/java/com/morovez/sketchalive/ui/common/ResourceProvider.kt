package com.morovez.sketchalive.ui.common

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat

interface ResourceProvider {
    fun getString(@StringRes res: Int, vararg args: Any): String
    fun getString(@StringRes res: Int): String
    fun getColor(@ColorRes res: Int): Int
    fun getDrawable(@DrawableRes res: Int): Drawable?
}

class ResourceProviderImpl(
    private val context: Context
) : ResourceProvider {

    override fun getString(@StringRes res: Int, vararg args: Any): String {
        return context.getString(res, args)
    }

    override fun getString(@StringRes res: Int): String {
        return context.getString(res)
    }

    override fun getColor(@ColorRes res: Int): Int {
        return context.getColor(res)
    }

    override fun getDrawable(res: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, res, null)
    }
}