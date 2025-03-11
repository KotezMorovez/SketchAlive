package com.morovez.sketchalive.di

import com.morovez.sketchalive.ui.PaletteHandler
import com.morovez.sketchalive.ui.PaletteHandlerImpl
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