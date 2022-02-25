package com.example.vacationsapp.presentation.desiredVacations
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Desired vacation presentation model
@Parcelize
data class DesiredVacationModel(
    val id:Int,
    val name: String,
    val hotelName: String,
    val location: String,
    val cost: Int,
    val description: String,
    val imagePath: Uri,
): Parcelable
