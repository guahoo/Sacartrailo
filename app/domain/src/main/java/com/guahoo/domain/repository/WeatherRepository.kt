package com.guahoo.domain.repository

import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.ForecastEntity
import com.guahoo.domain.entity.WeatherDetailsEntity
import kotlinx.coroutines.flow.Flow


interface WeatherRepository {

    fun getWeather(city: String): Flow<ResultState<WeatherDetailsEntity>>

    fun getForecast(city: String): Flow<ResultState<ForecastEntity>>
}