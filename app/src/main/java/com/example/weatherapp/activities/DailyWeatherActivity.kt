package com.example.weatherapp.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.R
import com.example.weatherapp.models.forecast7days.DailyWeather
import com.example.weatherapp.utils.Converter
import java.text.SimpleDateFormat

class DailyWeatherActivity : AppCompatActivity() {

    private lateinit var weather: TextView
    private lateinit var tempMin: TextView
    private lateinit var tempMax: TextView
    private lateinit var humidity: TextView
    private lateinit var sunrise: TextView
    private lateinit var sunset: TextView
    private lateinit var date: TextView

    private lateinit var weatherImg: ImageView

    private fun init() {
        weather = findViewById(R.id.weather)
        tempMin = findViewById(R.id.temp_minimum)
        tempMax = findViewById(R.id.temp_maximum)
        humidity = findViewById(R.id.humidity)
        sunrise = findViewById(R.id.sunrise)
        sunset = findViewById(R.id.sunset)
        date = findViewById(R.id.date)


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_weather)
        init()
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
        val dailyWeather = intent.getSerializableExtra("daily") as DailyWeather

        weather.text = dailyWeather.weather[0].description
        tempMin.text = dailyWeather.temp.min.toString()
        tempMax.text = dailyWeather.temp.max.toString()
        humidity.text = dailyWeather.humidity.toString()
        date.text = simpleDateFormat.format(Converter.secondsToDate(dailyWeather.dt))

        simpleDateFormat.applyPattern("dd.MM.yyyy hh:mm")
        sunrise.text = simpleDateFormat.format(Converter.secondsToDate(dailyWeather?.sunrise!!))
        sunset.text = simpleDateFormat.format(Converter.secondsToDate(dailyWeather?.sunset!!))

        val imgId = Converter.convertWeatherStateToImage(dailyWeather.weather[0].id)
        weatherImg.setImageResource(imgId)


    }
}