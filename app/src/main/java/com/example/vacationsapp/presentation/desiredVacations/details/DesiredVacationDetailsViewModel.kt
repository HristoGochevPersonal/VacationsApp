package com.example.vacationsapp.presentation.desiredVacations.details

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vacationsapp.core.repository.VacationsRepository
import com.example.vacationsapp.presentation.desiredVacations.DesiredVacationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Desired vacation details view model
class DesiredVacationDetailsViewModel : ViewModel() {

    // Contains a reference to the database repository for the whole application
    private var databaseRepository = VacationsRepository.instance

    // State flow with vacation information
    private val _vacationModelStateFlow = MutableStateFlow<DesiredVacationModel?>(null)
    val vacationModelStateFlow = _vacationModelStateFlow.asStateFlow()

    // State flow that keeps track of whether a notification is set for a vacation
    private val _notificationSetStateFlow = MutableStateFlow(false)
    val notificationSetStateFlow = _notificationSetStateFlow.asStateFlow()


    // Refreshes all info about a vacation
    fun refreshInfo(vacationId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.vacationFetch(vacationId)?.let{
                with(it){
                    val model = DesiredVacationModel(
                        id,
                        name,
                        hotelName,
                        location,
                        cost,
                        description,
                        imagePath.toUri()
                    )
                    _vacationModelStateFlow.emit(model)
                }
            }
        }
    }

    // Updates the notification state flow
    fun setNotificationSet(set: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _notificationSetStateFlow.emit(set)
        }
    }
}