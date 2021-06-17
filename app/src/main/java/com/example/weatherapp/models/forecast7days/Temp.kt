package com.example.weatherapp.models.forecast7days

import java.io.Serializable

data class Temp(
    val day: Double,
    val night: Double,
    val min: Double,
    val max: Double
):Serializable
