package com.morovez.sketchalive.di

import android.content.Context
import com.morovez.sketchalive.ui.views.panels.PalettePanelView
import com.morovez.sketchalive.ui.common.ResourceProviderImpl
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UIModule {
    @Singleton
    @Provides
    fun provideResourceProvider(@ApplicationContext context: Context) =
        ResourceProviderImpl(context)

    @Provides
    @Reusable
    fun providePalettePanelView(@ApplicationContext context: Context) = PalettePanelView(context)
}