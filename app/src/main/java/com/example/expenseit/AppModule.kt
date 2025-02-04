package com.example.expenseit

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class) // This makes the module available in the Singleton scope
object AppModule {

    // Provide the Context to Hilt
    @Provides
    fun provideContext(application: android.app.Application): Context {
        return application.applicationContext
    }
}