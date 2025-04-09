package com.project.medikare.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrescriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FirestoreRepository(application)

    fun fetchAndStorePrescription(prescriptionID: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.fetchAndStorePrescription(prescriptionID, onComplete)
        }
    }

    fun getPrescriptionFromRoom(prescriptionID: String, callback: (PrescriptionEntity?) -> Unit) {
        viewModelScope.launch {
            val prescription = repository.getPrescriptionFromRoom(prescriptionID)
            // Post value to main thread for UI updates
            withContext(Dispatchers.Main) {
                callback(prescription)
            }
        }
    }
}
