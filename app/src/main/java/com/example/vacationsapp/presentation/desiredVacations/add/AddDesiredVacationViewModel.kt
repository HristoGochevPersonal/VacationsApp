package com.example.vacationsapp.presentation.desiredVacations.add

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vacationsapp.core.entity.DesiredVacation
import com.example.vacationsapp.core.repository.VacationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Desired vacations add view model
class AddDesiredVacationViewModel : ViewModel() {

    // Contains a reference to the database repository for the whole application
    private var databaseRepository = VacationsRepository.instance

    // Keeps track of the selected image by the user
    private val _imagePathStateFlow = MutableStateFlow<Uri?>(null)
    val imagePathStateFlow = _imagePathStateFlow.asStateFlow()

    // Shared flow for signaling of an addition of vacation
    private val _creationSharedFlow = MutableSharedFlow<Boolean>()
    val creationSharedFlow = _creationSharedFlow.asSharedFlow()

    // Updates the image uri state flow
    fun setImageUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _imagePathStateFlow.emit(uri)
        }
    }


    fun createDesiredVacation(
        name: String,
        hotelName: String,
        location: String,
        cost: String,
        description: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Creates a vacation
            val costInt = cost.toIntOrNull()
            val imagePath=imagePathStateFlow.value?.toString()
            if (name.isEmpty() || hotelName.isEmpty() || location.isEmpty() || description.isEmpty() || imagePath.isNullOrEmpty() || costInt == null) {
                _creationSharedFlow.emit(false)
                return@launch
            }
            val value =
                DesiredVacation(-1, name, hotelName, location, costInt, description, imagePath)
            // Inserts it in the repository
            val creation = databaseRepository.vacationInsert(value)
            //  Signaling to the observers of the creation shared flow whether the creation was successful
            if (creation > 0) _creationSharedFlow.emit(true)
            else _creationSharedFlow.emit(false)
        }
    }
}