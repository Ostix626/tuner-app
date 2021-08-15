package com.example.tuner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.tuner.data.TunningDatabase
import com.example.tuner.model.Tunning
import com.example.tuner.repository.TunningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class TunningViewModel (application: Application) : AndroidViewModel(application) {
    val readAllData: LiveData<List<Tunning>>
    private var repository: TunningRepository? = null

    init {
        val tunningDao = TunningDatabase.getDatabase(application).tunnignDao()
        repository = TunningRepository(tunningDao)
        readAllData = repository?.readAllData!!
    }

    fun addTunning(tunning: Tunning) {
        viewModelScope.launch ( Dispatchers.IO ) {
            repository?.addTunning(tunning)
        }
    }

    fun updateTunning(tunning: Tunning) {
        viewModelScope.launch(Dispatchers.IO) {
            repository?.updateTunning(tunning)
        }
    }

    fun deleteTunning(tunning: Tunning) {
        viewModelScope.launch(Dispatchers.IO) {
            repository?.deleteTunning(tunning)
        }
    }

    fun deleteAllTunnings() {
        viewModelScope.launch (Dispatchers.IO) {
            repository?.deleteAllTunnings()
        }
    }
}