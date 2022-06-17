package com.bewell.storage.dao

import androidx.room.*
import com.bewell.data.HRVParam

@Dao
interface HRVParamDao {
    @Query("SELECT * FROM hrvparams")
    fun getAll(): List<HRVParam>

    @Query("SELECT * FROM hrvparams WHERE name = :name")
    fun getByName(name: String): HRVParam?

    @Insert
    fun insert(hrvParam: HRVParam)

    @Update
    fun update(hrvParam: HRVParam)

    @Delete
    fun delete(hrvParam: HRVParam)
}