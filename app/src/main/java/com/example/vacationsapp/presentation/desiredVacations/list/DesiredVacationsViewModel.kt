package com.example.vacationsapp.presentation.desiredVacations.list

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import  androidx.lifecycle.viewModelScope
import com.example.vacationsapp.core.repository.VacationsRepository
import com.example.vacationsapp.presentation.desiredVacations.DesiredVacationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Desired vacations list view model
class DesiredVacationsViewModel : ViewModel() {

    // Contains a reference to the database repository for the whole application
    private var databaseRepository = VacationsRepository.instance

    // State flow for the desired vacations recycler view
    private val _desiredVacationsStateFlow = MutableStateFlow<List<DesiredVacationModel>>(listOf())
    val desiredVacationsStateFlow = _desiredVacationsStateFlow.asStateFlow()

    // Shared flow for signaling of a deletion of a vacation
    private val _deletionSharedFlow = MutableSharedFlow<Pair<Boolean, DesiredVacationModel>>()
    val deletionSharedFlow = _deletionSharedFlow.asSharedFlow()

    // Refreshes the desired vacations state flow with info from the repository
    fun refreshDesiredVacations() {
        viewModelScope.launch(Dispatchers.IO) {
            val values = databaseRepository.vacationsFetch().map {
                DesiredVacationModel(
                    it.id,
                    it.name,
                    it.hotelName,
                    it.location,
                    it.cost,
                    it.description,
                    it.imagePath.toUri()
                )
            }
            _desiredVacationsStateFlow.emit(values)
        }
    }

    // Deletes a vacation from the repository and then signals to the observers of
    // the deletion shared flow whether the deletion was successful
    fun deleteDesiredVacation(desiredVacationModel: DesiredVacationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val deleted = databaseRepository.vacationDelete(desiredVacationModel.id)
            if (deleted > 0) _deletionSharedFlow.emit(Pair(true, desiredVacationModel))
            else _deletionSharedFlow.emit(Pair(false, desiredVacationModel))
        }
    }
}