package com.example.vacationsapp.presentation

import android.app.Application
import com.example.vacationsapp.core.repository.VacationsRepository

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initializing the singleton repository on application level
        VacationsRepository.initInstance(this)
    }
}