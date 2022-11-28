package com.csci_6221_kotlin.Models

// data class for sunset and sunrise data
data class Sys(
    val country: String,
    val id: Int,
    val sunrise: Int,
    val sunset: Int,
    val type: Int
)
