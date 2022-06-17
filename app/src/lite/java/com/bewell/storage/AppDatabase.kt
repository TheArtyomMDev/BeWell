package com.bewell.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bewell.data.Measure


@Database(entities = [Measure::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun measureDao(): MeasureDao?
}