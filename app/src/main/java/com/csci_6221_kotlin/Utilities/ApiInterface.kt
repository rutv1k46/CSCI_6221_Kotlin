package com.csci_6221_kotlin.Utilities

import com.csci_6221_kotlin.Models.ForecastModel
import com.csci_6221_kotlin.Models.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("weather")
    fun getCurrentWeatherData(
        @Query("lat") lat:String,
        @Query("lon") lon:String,
        @Query("units") units:String,
        @Query("APPID") appid:String
    ):Call<WeatherModel>

    @GET("weather")
    fun getCityWeatherData(
        @Query("q") q:String,
        @Query("units") units:String,
        @Query("APPID") appid:String
    ):Call<WeatherModel>

    @GET("forecast")
    fun getCityForecastData(
        @Query("q") q:String,
        @Query("units") units:String,
        @Query("APPID") appid:String
    ):Call<ForecastModel>

    @GET("forecast")
    fun getCurrentLocationForecastData(
        @Query("lat") lat:String,
        @Query("lon") lon:String,
        @Query("units") units:String,
        @Query("APPID") appid:String
    ):Call<ForecastModel>

}