package com.example.tuner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(entities = [Tunning::class], version = 1, exportSchema = false)
abstract class TunningDatabase: RoomDatabase() {

    abstract fun tunnignDao(): TunningDao

    companion object {
        @Volatile
        private var INSTANCE: TunningDatabase? = null

        @InternalCoroutinesApi
        fun getDatabase(context: Context): TunningDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TunningDatabase::class.java,
                    "tunning_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}