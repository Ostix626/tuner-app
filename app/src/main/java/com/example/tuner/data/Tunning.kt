package com.example.tuner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tunnings_table")
data class Tunning (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val tunningName: String,
    val tunningTones: String
)