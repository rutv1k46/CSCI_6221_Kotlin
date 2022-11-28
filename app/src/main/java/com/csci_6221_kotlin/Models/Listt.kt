package com.csci_6221_kotlin.Models

data class Listt(
    val dt: Int,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val rain: Rain,
    val sys: Sys,
    val dt_txt: String
)
