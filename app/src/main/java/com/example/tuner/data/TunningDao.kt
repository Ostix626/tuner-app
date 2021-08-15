package com.example.tuner.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.tuner.model.Tunning

@Dao
interface TunningDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTunning(tunning: Tunning)

    @Update
    suspend fun updateTunning(tunning: Tunning)

    @Query("SELECT * FROM tunnings_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Tunning>>
}