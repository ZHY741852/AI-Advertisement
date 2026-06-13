package com.aiadvertisement.data.di

import com.aiadvertisement.data.repository.AdRepositoryImpl
import com.aiadvertisement.domain.repository.AdRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAdRepository(impl: AdRepositoryImpl): AdRepository
}
