package com.example.vacationsapp.presentation.desiredVacations.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vacationsapp.core.entity.DesiredVacation
import com.example.vacationsapp.core.repository.VacationsRepository
import com.example.vacationsapp.presentation.desiredVacations.DesiredVacationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Desired vacation edit view model
class EditDesiredVacationViewModel(val model: DesiredVacationModel) : ViewModel() {

    // Contains a reference to the database repository for the whole application
    private var databaseRepository = VacationsRepository.instance

    // State flows with vacation information
    private val _imagePathStateFlow = MutableStateFlow(model.imagePath)
    val imagePathStateFlow = _imagePathStateFlow.asStateFlow()

    // Shared flow for signaling of an edit of a vacation
    private val _editSharedFlow = MutableSharedFlow<Boolean>()
    val editSharedFlow = _editSharedFlow.asSharedFlow()

    // Shared flow for signaling of a deletion of a vacation
    private val _deletionSharedFlow = MutableSharedFlow<Boolean>()
    val deletionSharedFlow = _deletionSharedFlow.asSharedFlow()

    // Updates the image uri state flow
    fun setImageUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _imagePathStateFlow.emit(uri)
        }
    }

    // Edit vacation
    fun editDesiredVacation(name:String,hotelName:String,location:String,cost:String,description:String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Creating a vacation like the old one but with new info
            val costInt = cost.toIntOrNull()
            val imagePath = imagePathStateFlow.value.toString()
            if (name.isEmpty() || hotelName.isEmpty() || location.isEmpty() || description.isEmpty() || imagePath.isEmpty() || costInt == null) {
                _editSharedFlow.emit(false)
                return@launch
            }
            val value =
                DesiredVacation(model.id, name, hotelName, location, costInt, description, imagePath)
            // Updating the old one
            val edited = databaseRepository.vacationUpdate(value)
            //  Signaling to the observers of the edit shared flow whether the editing was successful
            if (edited > 0) _editSharedFlow.emit(true)
            else _editSharedFlow.emit(false)
        }
    }

    // Delete vacation
    fun deleteDesiredVacation() {
        viewModelScope.launch(Dispatchers.IO) {
            // Deletes a vacation from the repository
            val deleted = databaseRepository.vacationDelete(model.id)
            // Signals to the observers of the the deletion shared flow whether the deletion was successful
            if (deleted > 0) _deletionSharedFlow.emit(true)
            else _deletionSharedFlow.emit(false)
        }
    }
}