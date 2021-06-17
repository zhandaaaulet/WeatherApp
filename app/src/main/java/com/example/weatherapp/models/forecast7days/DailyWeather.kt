package com.example.weatherapp.models.forecast7days

import com.example.weatherapp.models.Weather
import java.io.Serializable

data class DailyWeather(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val humidity: Int,
    val temp: Temp,
    val weather:List<Weather>
):Serializable