package com.example.tuner.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TunningDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTunning(tunning: Tunning)

    @Query("SELECT * FROM tunnings_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Tunning>>
}