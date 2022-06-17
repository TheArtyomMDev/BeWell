package com.bewell.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bewell.data.HRVParam
import com.bewell.storage.dao.HRVParamDao

@Database(entities = [HRVParam::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hrvParamDao(): HRVParamDao?
}