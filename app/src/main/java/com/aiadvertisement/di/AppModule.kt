package com.aiadvertisement.di

import android.content.Context
import com.aiadvertisement.core.storage.InteractionStateStore
import com.aiadvertisement.core.storage.ScrollPositionStore
import com.aiadvertisement.data.mock.MockDataProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMockDataProvider(@ApplicationContext context: Context): MockDataProvider {
        return MockDataProvider(context)
    }

    @Provides
    @Singleton
    fun provideInteractionStateStore(@ApplicationContext context: Context): InteractionStateStore {
        return InteractionStateStore(context)
    }

    @Provides
    @Singleton
    fun provideScrollPositionStore(): ScrollPositionStore {
        return ScrollPositionStore()
    }
}
