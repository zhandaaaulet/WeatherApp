package com.example.weatherapp.utils

import com.example.weatherapp.R
import java.util.*

class Converter {
    companion object {
        fun convertWeatherStateToImage(weatherStateId: Int) = when (weatherStateId) {
            in 200..299 -> R.drawable.weather_thunderstorm

            in 300..399 -> R.drawable.weather_shower_rain

            in 500..504 -> R.drawable.weather_rain
            511 -> R.drawable.weather_snow
            in 520..531 -> R.drawable.weather_shower_rain

            in 600..699 -> R.drawable.weather_snow

            in 700..799 -> R.drawable.weather_mist

            800 -> R.drawable.weather_clear

            801 -> R.drawable.weather_few_clouds
            802 -> R.drawable.weather_scattered_clouds
            803, 804 -> R.drawable.weather_broken_clouds

            else -> R.drawable.weather_rain
        }

        fun secondsToDate(seconds: Long): Date {
            return Date(seconds*1000)
        }
    }
}