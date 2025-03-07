package com.example.animatorforandroid.di

import com.example.animatorforandroid.ui.PaletteHandler
import com.example.animatorforandroid.ui.PaletteHandlerImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface UIModule {
    @Reusable
    @Binds
    fun bindPaletteHandler(impl: PaletteHandlerImpl): PaletteHandler
}