package com.example.weatherapp.models.forecast7days

import java.io.Serializable

data class OneCallResponse(
    val lon: Double,
    val lat: Double,
    val daily: List<DailyWeather>
):Serializable
