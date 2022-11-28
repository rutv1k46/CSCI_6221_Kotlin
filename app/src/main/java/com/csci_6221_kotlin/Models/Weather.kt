package com.csci_6221_kotlin.Models

// data class for weather data
data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)
