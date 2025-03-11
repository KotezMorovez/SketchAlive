package com.morovez.sketchalive.di

import android.content.Context
import com.morovez.sketchalive.ui.InstrumentsHandler
import com.morovez.sketchalive.ui.InstrumentsHandlerImpl
import com.morovez.sketchalive.ui.PaletteHandler
import com.morovez.sketchalive.ui.PaletteHandlerImpl
import com.morovez.sketchalive.ui.common.ResourceProvider
import com.morovez.sketchalive.ui.common.ResourceProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface UIModule {
    @Reusable
    @Binds
    fun bindPaletteHandler(impl: PaletteHandlerImpl): PaletteHandler

    @Reusable
    @Binds
    fun bindInstrumentsHandler(impl: InstrumentsHandlerImpl): InstrumentsHandler
}

@Module
@InstallIn(SingletonComponent::class)
class ProvideUIModule {
    @Singleton
    @Provides
    fun provideResourceProvider(@ApplicationContext context: Context): ResourceProvider {
        return ResourceProviderImpl(context)
    }
}