package com.csci_6221_kotlin.Models

data class ForecastModel(
    val cod: Int,
    val message: String,
    val cnd: Int,
    val list: List<Listt>
)
