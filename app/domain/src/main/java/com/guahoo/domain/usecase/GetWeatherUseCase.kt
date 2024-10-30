package com.guahoo.domain.usecase


import com.guahoo.domain.commons.ResultState
import com.guahoo.domain.entity.ForecastEntity
import com.guahoo.domain.entity.WeatherDetailsEntity
import com.guahoo.domain.repository.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest


class GetWeatherUseCase(private val weatherRepository: WeatherRepository) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(city: String): Flow<ResultState<Pair<WeatherDetailsEntity, ForecastEntity>>> {
        return combine(
            weatherRepository.getWeather(city),
            weatherRepository.getForecast(city)
        ) { weatherResult, forecastResult ->
            if (weatherResult is ResultState.Success && forecastResult is ResultState.Success) {
                ResultState.Success(Pair(weatherResult.data, forecastResult.data))
            } else if (weatherResult is ResultState.Error) {
                ResultState.Error(weatherResult.message)
            } else if (forecastResult is ResultState.Error) {
                ResultState.Error(forecastResult.message)
            } else {
                ResultState.Loading("")
            }
        }.mapLatest { resultState ->
            resultState
        }
    }

}

class GetForecastUseCase(private val weatherRepository: WeatherRepository) {
    operator fun invoke(city: String): Flow<ResultState<ForecastEntity>> {
        return weatherRepository.getForecast(city)
    }
}
