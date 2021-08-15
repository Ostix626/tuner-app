package com.example.tuner.repository

import androidx.lifecycle.LiveData
import com.example.tuner.data.TunningDao
import com.example.tuner.model.Tunning

class TunningRepository (private val tunningDao : TunningDao){
    val readAllData: LiveData<List<Tunning>> = tunningDao.readAllData()

    suspend fun addTunning(tunning : Tunning) {
        tunningDao.addTunning(tunning)
    }

    suspend fun updateTunning(tunning: Tunning) {
        tunningDao.updateTunning(tunning)
    }

    suspend fun deleteTunning(tunning: Tunning) {
        tunningDao.deleteTunning(tunning)
    }

    suspend fun deleteAllTunnings() {
        tunningDao.deleteAllTunnings()
    }
}