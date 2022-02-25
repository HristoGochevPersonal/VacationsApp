package com.example.vacationsapp.core.entity

// Desired vacation core entity class
data class DesiredVacation(
    val id: Int,
    val name: String,
    val hotelName: String,
    val location: String,
    val cost: Int,
    val description: String,
    val imagePath: String,
)
