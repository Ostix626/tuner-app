package com.example.tuner.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@InternalCoroutinesApi
class TunningViewModel (application: Application) : AndroidViewModel(application) {
    private val readAllData: LiveData<List<Tunning>>
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
}