package com.pluu.webtoon.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object LoggerModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor() = HttpLoggingInterceptor(
        object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Timber.tag("OkHttp").d(message)
            }
        }).apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
}