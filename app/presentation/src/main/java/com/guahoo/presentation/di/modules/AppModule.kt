package com.guahoo.presentation.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.guahoo.data.db.dao.TrackDao
import com.guahoo.data.network.TracksApiService
import com.guahoo.data.preferenses.PreferencesService
import com.guahoo.data.repository.TracksRepositoryImpl
import com.guahoo.domain.repository.WeatherRepository
import com.guahoo.domain.repository.TracksRepository
import com.guahoo.domain.usecase.GetForecastUseCase
import com.guahoo.domain.usecase.GetTracksUseCase
import com.guahoo.domain.usecase.GetWeatherUseCase
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
    fun provideTrackRepository(api: TracksApiService,
                               trackDao: TrackDao,
                               preferencesService: PreferencesService,
                               ): TracksRepository{
        return TracksRepositoryImpl(api, trackDao, preferencesService)
    }

    @Provides
    fun provideGetWeatherUseCase(repository: WeatherRepository): GetWeatherUseCase {
        return GetWeatherUseCase(repository)
    }

    @Provides
    fun provideGetForecastUseCase(repository: WeatherRepository): GetForecastUseCase {
        return GetForecastUseCase(repository)
    }

    @Provides
    fun getTracksUseCase(repository: TracksRepository): GetTracksUseCase {
        return GetTracksUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PreferencesService.APP_PREFERENCES, Context.MODE_PRIVATE)
    }

    @Provides
    fun getSharedPreferences(sharedPreferences: SharedPreferences): PreferencesService {
        return PreferencesService(sharedPreferences)
    }

}