package com.pantrychef.back.di

import android.content.Context
import com.pantrychef.back.data.mock.MockAuthDataSource
import com.pantrychef.back.data.mock.MockProductDataSource
import com.pantrychef.back.data.mock.MockRecipeDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideMockAuthDataSource(): MockAuthDataSource {
        return MockAuthDataSource()
    }

    @Provides
    @Singleton
    fun provideMockProductDataSource(@ApplicationContext context: Context): MockProductDataSource {
        return MockProductDataSource(context)
    }

    @Provides
    @Singleton
    fun provideMockRecipeDataSource(@ApplicationContext context: Context): MockRecipeDataSource {
        return MockRecipeDataSource(context)
    }
}