package com.example.tuner.data

import androidx.lifecycle.LiveData

class TunningRepository (private val tunningDao : TunningDao){
    val readAllData: LiveData<List<Tunning>> = tunningDao.readAllData()

    suspend fun addTunning(tunning : Tunning) {
        tunningDao.addTunning(tunning)
    }
}