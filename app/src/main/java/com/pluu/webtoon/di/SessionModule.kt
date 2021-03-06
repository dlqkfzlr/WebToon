package com.pluu.webtoon.di

import com.pluu.webtoon.data.pref.PrefConfig
import com.pluu.webtoon.model.Session
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object SessionModule {
    @Singleton
    @Provides
    fun provideSession(
        prefConfig: PrefConfig
    ) = Session(prefConfig)

    @Provides
    fun provideNavType(
        session: Session
    ) = session.navi
}
