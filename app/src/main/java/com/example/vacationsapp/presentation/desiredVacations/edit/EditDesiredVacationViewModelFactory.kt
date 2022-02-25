package com.example.vacationsapp.presentation.desiredVacations.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vacationsapp.presentation.desiredVacations.DesiredVacationModel
import java.lang.IllegalArgumentException

// Vacations view model factory
@Suppress("UNCHECKED_CAST")
class EditDesiredVacationViewModelFactory(
    private val model:DesiredVacationModel
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditDesiredVacationViewModel::class.java)) {
            return EditDesiredVacationViewModel(model) as T
        }
        throw IllegalArgumentException("ViewModel class not found")
    }
}