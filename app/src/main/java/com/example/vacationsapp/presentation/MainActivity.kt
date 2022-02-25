package com.example.vacationsapp.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.example.vacationsapp.R
import com.example.vacationsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        // Pop navigation backstack if it does not contain only the start fragment
        // If it does, resort to normal back press event
        val navController = findNavController(R.id.fragmentContainerView)
        println("Size is ${navController.backQueue.size}")
        if (navController.backQueue.size < 3) {
            super.onBackPressed()
        } else {
            navController.popBackStack()
        }
    }
}