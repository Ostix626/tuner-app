package com.example.tuner.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "tunnings_table")
data class Tunning (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val tunningName: String,
    val tunningTones: String
): Parcelable