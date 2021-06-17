package com.example.weatherapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.weatherapp.R
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Converter
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var country: TextView
    private lateinit var weather: TextView
    private lateinit var degree: TextView
    private lateinit var tempMin: TextView
    private lateinit var tempMax: TextView
    private lateinit var wind: TextView
    private lateinit var humidity: TextView
    private lateinit var sunrise: TextView
    private lateinit var sunset: TextView

    private lateinit var weatherImg: ImageView

    private lateinit var updateBtn: Button
    private lateinit var forecast7DaysBtn: Button

    private var latitude = 0.0
    private var longitude = 0.0

    private fun init() {
        country = findViewById(R.id.country)
        weather = findViewById(R.id.weather)
        degree = findViewById(R.id.degree)
        tempMin = findViewById(R.id.temp_minimum)
        tempMax = findViewById(R.id.temp_maximum)
        wind = findViewById(R.id.wind)
        humidity = findViewById(R.id.humidity)
        sunrise = findViewById(R.id.sunrise)
        sunset = findViewById(R.id.sunset)

        updateBtn = findViewById(R.id.update)
        forecast7DaysBtn = findViewById(R.id.forecast7DaysBtn)

        /*weatherImg = findViewById(R.id.weatherImg)*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        updateBtn.setOnClickListener {
            getLocationWeatherDetails(latitude, longitude)
        }

        forecast7DaysBtn.setOnClickListener {
            val intent = Intent(this,Forecast7DaysActivity::class.java)
            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!isLocationEnabled()) {
            Toast.makeText(
                this, "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) requestLocationData()
                        if (report.isAnyPermissionPermanentlyDenied)
                            Toast.makeText(
                                this@MainActivity,
                                "You have denied location permission. Please allow it.",
                                Toast.LENGTH_SHORT
                            ).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread().check()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Permissions required")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation = p0.lastLocation
            latitude = lastLocation.latitude
            longitude = lastLocation.longitude
            getLocationWeatherDetails(latitude, longitude)
        }
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (Constants.isNetworkAvailable(this)) {
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService =
                retrofit.create(WeatherService::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )

            listCall.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weatherList = response.body()
                        manipulateResponse(weatherList)
                    } else {
                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not found")
                            }
                            else -> {
                                Log.e("Error", "Generic error")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Failure Error", t.message.toString())
                }
            })
        } else {
            Toast.makeText(
                this,
                "No internet connection avaliable.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun manipulateResponse(weatherList: WeatherResponse?) {
        Log.i("Response result", "$weatherList")

        country.text = weatherList?.sys?.country
        weather.text = weatherList?.weather?.get(0)?.description
        degree.text = weatherList?.main?.temp.toString()
        tempMin.text = weatherList?.main?.temp_min.toString()
        tempMax.text = weatherList?.main?.temp_max.toString()
        wind.text = weatherList?.wind?.speed?.toString()
        humidity.text = weatherList?.main?.humidity?.toString()

        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy hh:mm")
        sunrise.text = simpleDateFormat.format(Converter.secondsToDate(weatherList?.sys?.sunrise!!))
        sunset.text = simpleDateFormat.format(Converter.secondsToDate(weatherList?.sys?.sunset!!))

        val imgId = Converter.convertWeatherStateToImage(weatherList.weather[0].id)
        /*weatherImg.setImageResource(imgId)*/
    }


}