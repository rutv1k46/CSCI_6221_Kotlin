package com.csci_6221_kotlin.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.csci_6221_kotlin.Models.ForecastModel
import com.csci_6221_kotlin.Models.WeatherModel
import com.csci_6221_kotlin.R
import com.csci_6221_kotlin.Utilities.ApiUtilities
import com.csci_6221_kotlin.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 101

    private val apiKey="f940abf29513b27fe75bd7c3ac06feac"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        fusedLocationProvider=LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        binding.citySearch.setOnEditorActionListener { textView, i, keyEvent ->

            if (i==EditorInfo.IME_ACTION_SEARCH){

                getCityWeather(binding.citySearch.text.toString())
                getCityForecastWeather(binding.citySearch.text.toString())

                val view=this.currentFocus

                if (view!=null){

                    val imm:InputMethodManager=getSystemService(INPUT_METHOD_SERVICE)
                            as InputMethodManager

                    imm.hideSoftInputFromWindow(view.windowToken,0)

                    binding.citySearch.clearFocus()


                }

                return@setOnEditorActionListener true

            }
            else{

                return@setOnEditorActionListener false
            }

        }


        binding.currentLocation.setOnClickListener {

            getCurrentLocation()


        }


    }

    private fun getCityWeather(city: String) {

        binding.progressBar.visibility= View.VISIBLE

        ApiUtilities.getApiInterface()?.getCityWeatherData(city,"metric", apiKey)
            ?.enqueue(object :Callback<WeatherModel>{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherModel>, response: Response<WeatherModel>) {
                    if (response.isSuccessful){

                        binding.progressBar.visibility= View.GONE

                        response.body()?.let {
                            setData(it)
                        }

                    }
                    else{

                        Toast.makeText(this@MainActivity, "No City Found",
                            Toast.LENGTH_SHORT).show()

                        binding.progressBar.visibility= View.GONE

                    }

                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {


                }


            })


    }


    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {

        ApiUtilities.getApiInterface()?.getCurrentWeatherData(latitude,longitude,"metric",apiKey)
            ?.enqueue(object :Callback<WeatherModel>{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherModel>, response: Response<WeatherModel>) {

                    if (response.isSuccessful){

                        binding.progressBar.visibility= View.GONE

                        response.body()?.let {
                            setData(it)
                        }

                    }


                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {


                }

            })


    }

    private fun getCityForecastWeather(city: String) {

        ApiUtilities.getApiInterface()?.getCityForecastData(city,"metric",apiKey)
            ?.enqueue(object :Callback<ForecastModel>{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<ForecastModel>, response: Response<ForecastModel>) {

                    if (response.isSuccessful){

                        binding.progressBar.visibility= View.GONE

                        response.body()?.let {
                            setForecastData(it)
                        }

                    }


                }

                override fun onFailure(call: Call<ForecastModel>, t: Throwable) {


                }

            })


    }


    private fun fetchCurrentLocationForecast(latitude: String, longitude: String) {

        ApiUtilities.getApiInterface()?.getCurrentLocationForecastData(latitude, longitude, "metric",apiKey)
            ?.enqueue(object :Callback<ForecastModel>{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<ForecastModel>, response: Response<ForecastModel>) {

                    if (response.isSuccessful){

                        binding.progressBar.visibility= View.GONE

                        response.body()?.let {
                            setForecastData(it)
                        }

                    }


                }

                override fun onFailure(call: Call<ForecastModel>, t: Throwable) {


                }

            })


    }


    private fun getCurrentLocation(){

        if (checkPermissions()){

            if (isLocationEnabled()){

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    requestPermission()

                    return
                }
                fusedLocationProvider.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = location

                            binding.progressBar.visibility = View.VISIBLE

                            fetchCurrentLocationWeather(
                                location.latitude.toString(),
                                location.longitude.toString()
                            )

                            fetchCurrentLocationForecast(
                                location.latitude.toString(),
                                location.longitude.toString()
                            )

                        }
                    }

            }
            else{

                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

                startActivity(intent)


            }


        }
        else{

            requestPermission()

        }


    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_REQUEST_CODE
        )


    }

    private fun isLocationEnabled(): Boolean {

        val locationManager:LocationManager=getSystemService(Context.LOCATION_SERVICE)
                as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)




    }

    private fun checkPermissions(): Boolean {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            return true

        }

        return false



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==LOCATION_REQUEST_CODE){

            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                getCurrentLocation()

            }
            else{




            }



        }



    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData(body:WeatherModel){

        binding.apply {

            val currentDate=SimpleDateFormat("yyyy/MM/dd hh:mm aa").format(Date())

            dateTime.text=currentDate.toString().split("2022/")[1]

            maxTemp.text="Max: "+ floor(body?.main?.temp_max!!).toInt() +"°"

            minTemp.text="Min: "+ floor(body?.main?.temp_min!!).toInt() +"°"

            temp.text=""+ floor(body?.main?.temp!!).toInt() +"°"

            weatherTitle.text=body.weather[0].main

            var sunriseTd = SimpleDateFormat("hh:mm:ss").parse(ts2td(body.sys.sunrise.toLong()))
            sunriseValue.text=SimpleDateFormat("hh:mm aa").format(sunriseTd)

            var sunsetTd = SimpleDateFormat("hh:mm:ss").parse(ts2td(body.sys.sunset.toLong()))
            sunsetValue.text=SimpleDateFormat("hh:mm aa").format(sunsetTd)

            pressureValue.text=body.main.pressure.toString()

            humidityValue.text=body.main.humidity.toString()+"%"

            citySearch.setText(body.name)

            tempFValue.text=""+ floor(body?.main?.temp!!).times(1.8).plus(32).toInt()+"°"

            feelsLike.text= "Feels like: "+ floor(body.main.feels_like).toInt() +"°"

            windValue.text=body.wind.speed.toString()+"m/s"

        }

        updateUI(body.weather[0].id)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setForecastData(body:ForecastModel){

        binding.apply {


            nextDay1WeatherTitle.text=body.list[3].weather[0].main
            nextDay1WeatherValue.text=""+ floor(body.list[3].main?.temp!!).toInt() +"°"
            nextDay1WeatherDate.text=body.list[3].dt_txt.split(" ")[0].replace("-","/").split("2022/")[1]
//            nextDay1WeatherTime.text=body.list[3].dt_txt.split(" ")[1]

            nextDay2WeatherTitle.text=body.list[11].weather[0].main
            nextDay2WeatherValue.text=""+ floor(body.list[11].main?.temp!!).toInt() +"°"
            nextDay2WeatherDate.text=body.list[11].dt_txt.split(" ")[0].replace("-","/").split("2022/")[1]
//            nextDay2WeatherTime.text=body.list[3].dt_txt.split(" ")[1]

            nextDay3WeatherTitle.text=body.list[19].weather[0].main
            nextDay3WeatherValue.text=""+ floor(body.list[19].main?.temp!!).toInt() +"°"
            nextDay3WeatherDate.text=body.list[19].dt_txt.split(" ")[0].replace("-","/").split("2022/")[1]
//            nextDay3WeatherTime.text=body.list[3].dt_txt.split(" ")[1]

            updateForecastUI(body.list[3].weather[0].id, nextDay1WeatherImg)
            updateForecastUI(body.list[11].weather[0].id, nextDay2WeatherImg)
            updateForecastUI(body.list[19].weather[0].id, nextDay3WeatherImg)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ts2td(ts:Long):String{

        val localTime=ts.let {

            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()

        }

        return localTime.toString()


    }


    private fun updateUI(id: Int) {

        binding.apply {


            when (id) {

                //Thunderstorm
                in 200..232 -> {

                    weatherImg.setImageResource(R.drawable.ic_storm_weather)
                    mainLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.thunderstrom_bg)

                    optionsLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.thunderstrom_bg)


                }

                //Drizzle
                in 300..321 -> {

                    weatherImg.setImageResource(R.drawable.ic_few_clouds)
                    mainLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.drizzle_bg)

                    optionsLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.drizzle_bg)


                }

                //Rain
                in 500..531 -> {

                    weatherImg.setImageResource(R.drawable.ic_rainy_weather)
                    mainLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.rain_bg)

                    optionsLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.rain_bg)

                }

                //Snow
                in 600..622 -> {

                    weatherImg.setImageResource(R.drawable.ic_snow_weather)
                    mainLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.snow_bg)

                    optionsLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.snow_bg)

                }

                //Atmosphere
                in 701..781 -> {

                    weatherImg.setImageResource(R.drawable.ic_broken_clouds)
                    mainLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.atmosphere_bg)


                    optionsLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.atmosphere_bg)

                }

                //Clear
                800 -> {

                    weatherImg.setImageResource(R.drawable.ic_clear_day)
                    mainLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clear_bg)

                    optionsLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clear_bg)

                }

                //Clouds
                in 801..804 -> {

                    weatherImg.setImageResource(R.drawable.ic_cloudy_weather)
                    mainLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clouds_bg)

                    optionsLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.clouds_bg)

                }

                //unknown
                else->{

                    weatherImg.setImageResource(R.drawable.ic_unknown)
                    mainLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.unknown_bg)

                    optionsLayout.background=ContextCompat
                        .getDrawable(this@MainActivity, R.drawable.unknown_bg)


                }


            }





        }



    }

    private fun updateForecastUI(id: Int, nextDayImg: ImageView) {

        binding.apply {


            when (id) {

                //Thunderstorm
                in 200..232 -> {

                    nextDayImg.setImageResource(R.drawable.ic_storm_weather)

                }

                //Drizzle
                in 300..321 -> {

                    nextDayImg.setImageResource(R.drawable.ic_few_clouds)

                }

                //Rain
                in 500..531 -> {

                    nextDayImg.setImageResource(R.drawable.ic_rainy_weather)

                }

                //Snow
                in 600..622 -> {

                    nextDayImg.setImageResource(R.drawable.ic_snow_weather)

                }

                //Atmosphere
                in 701..781 -> {

                    nextDayImg.setImageResource(R.drawable.ic_broken_clouds)

                }

                //Clear
                800 -> {

                    nextDayImg.setImageResource(R.drawable.ic_clear_day)

                }

                //Clouds
                in 801..804 -> {

                    nextDayImg.setImageResource(R.drawable.ic_cloudy_weather)

                }

                //unknown
                else->{

                    nextDayImg.setImageResource(R.drawable.ic_unknown)

                }


            }





        }



    }





}

