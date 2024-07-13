package com.example.pickshow.data.models

data class Theatre(
    val id: String,
    val theatreName: String,
    val address: String = "",  // Optional fields based on your JSON structure
    val latitude: String = "",
    val longitude: String = ""
)

