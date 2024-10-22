package com.guahoo.presentation.di.modules

import com.guahoo.data.db.dao.TrackDao
import com.guahoo.data.network.TracksApiService
import com.guahoo.data.repository.TracksRepositoryImpl
import com.guahoo.domain.repository.WeatherRepository
import com.guahoo.domain.repository.TracksRepository
import com.guahoo.domain.usecase.GetForecastUseCase
import com.guahoo.domain.usecase.GetTracksUseCase
import com.guahoo.domain.usecase.GetWeatherUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideTrackRepository(api: TracksApiService,
                               trackDao: TrackDao,
                               ): TracksRepository{
        return TracksRepositoryImpl(api, trackDao)
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

}