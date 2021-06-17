package com.example.weatherapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.weatherapp.R
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.models.forecast7days.OneCallResponse
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
import java.util.*
import kotlin.collections.HashMap

class Forecast7DaysActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude = 0.0
    private var longitude = 0.0

    private lateinit var mainLayout: LinearLayout

    private fun init() {
        mainLayout = findViewById(R.id.mainLayout)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast_7_days)
        init()

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
                                this@Forecast7DaysActivity,
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

            val listCall: Call<OneCallResponse> = service.getOneCall(
                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )

            listCall.enqueue(object : Callback<OneCallResponse> {
                override fun onResponse(
                    call: Call<OneCallResponse>,
                    response: Response<OneCallResponse>
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

                override fun onFailure(call: Call<OneCallResponse>, t: Throwable) {
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

    private fun manipulateResponse(dailyWeatherList: OneCallResponse?) {
        Log.i("Response result", "$dailyWeatherList")

        val viewKVs: HashMap<String, Int> = HashMap(7);
        viewKVs["1Img"] = R.id.day1Img
        viewKVs["1Min"] = R.id.day1Min
        viewKVs["1Max"] = R.id.day1Max

        viewKVs["2Img"] = R.id.day2Img
        viewKVs["2Min"] = R.id.day2Min
        viewKVs["2Max"] = R.id.day2Max

        viewKVs["3Img"] = R.id.day3Img
        viewKVs["3Min"] = R.id.day3Min
        viewKVs["3Max"] = R.id.day3Max

        viewKVs["4Img"] = R.id.day4Img
        viewKVs["4Min"] = R.id.day4Min
        viewKVs["4Max"] = R.id.day4Max

        viewKVs["5Img"] = R.id.day5Img
        viewKVs["5Min"] = R.id.day5Min
        viewKVs["5Max"] = R.id.day5Max

        viewKVs["6Img"] = R.id.day6Img
        viewKVs["6Min"] = R.id.day6Min
        viewKVs["6Max"] = R.id.day6Max

        viewKVs["7Img"] = R.id.day7Img
        viewKVs["7Min"] = R.id.day7Min
        viewKVs["7Max"] = R.id.day7Max

        viewKVs["1"] = R.id.d1
        viewKVs["2"] = R.id.d2
        viewKVs["3"] = R.id.d3
        viewKVs["4"] = R.id.d4
        viewKVs["5"] = R.id.d5
        viewKVs["6"] = R.id.d6
        viewKVs["7"] = R.id.d7

        for (i in 0..6) {
            val day = i + 1
            val weatherImg: ImageView = findViewById(viewKVs[day.toString() + "Img"]!!)
            val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")

            val date: TextView = findViewById(viewKVs[day.toString()]!!)
            date.text =
                simpleDateFormat.format(Converter.secondsToDate(dailyWeatherList!!.daily[i].dt))
            val min: TextView = findViewById(viewKVs[day.toString() + "Min"]!!)
            val max: TextView = findViewById(viewKVs[day.toString() + "Max"]!!)

            weatherImg.setImageResource(Converter.convertWeatherStateToImage(dailyWeatherList!!.daily[i].weather[0].id))
            min.text = dailyWeatherList!!.daily[i].temp.min.toString() + "℃"
            max.text = dailyWeatherList!!.daily[i].temp.max.toString() + "℃"

            weatherImg.setOnClickListener {
                val intent = Intent(this, DailyWeatherActivity::class.java)
                intent.putExtra("daily", dailyWeatherList.daily[i])
                startActivity(intent)
            }
        }
    }

}