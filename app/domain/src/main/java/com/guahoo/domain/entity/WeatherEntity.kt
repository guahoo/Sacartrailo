package com.guahoo.domain.entity

// Coordinates of the location
data class CoordEntity(
    val lon: Double,
    val lat: Double
)

// Represents weather information at a certain time
data class WeatherItemEntity(
    val icon: String,
    val description: String,
    val main: String,
    val id: Int
)

// Represents wind data
data class WindEntity(
    val deg: Int,
    val speed: Double,
    val gust: Double
)

// Cloud data
data class CloudsEntity(
    val all: Int
)

// City details
data class CityEntity(
    val country: String,
    val coord: CoordEntity,
    val sunrise: Long,
    val sunset: Long,
    val timezone: Int,
    val name: String,
    val id: Int,
    val population: Int
)

// Main forecast details like temperature, humidity, pressure
data class MainForecastEntity(
    val temp: Double,
    val tempMin: Double,
    val humidity: Int,
    val pressure: Int,
    val feelsLike: Double,
    val tempMax: Double
)

// Represents a weather forecast for a specific time
data class WeatherListItemEntity(
    val dt: Long,
    val weather: List<WeatherItemEntity>?,
    val main: MainForecastEntity,
    val clouds: CloudsEntity,
    val wind: WindEntity
)

// Full forecast entity containing city and weather list
data class ForecastEntity(
    val city: CityEntity,
    val weatherList: List<WeatherListItemEntity>
)

data class WeatherDetailsEntity(
    val visibility: Int,
    val timezone: Int,
    val temperature: Double,
    val humidity: Int,
    val cloudiness: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long,
    val timestamp: Int,
    val latitude: Double,
    val longitude: Double,
    val weatherDescription: String?,
    val icon: String?,
    val cityName: String,
    val windSpeed: Double,
    val windDirection: Int,
    val pressure: Int
)
