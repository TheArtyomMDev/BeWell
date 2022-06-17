package com.bewell.storage

import androidx.room.*
import com.bewell.data.Measure


@Dao
interface MeasureDao {
   @get:Query("SELECT * FROM measures")
   val all: List<Any?>?

   @Query("SELECT * FROM measures WHERE id = :id")
   fun getById(id: Long): Measure?

   @Insert
   fun insert(measure: Measure?)

   @Update
   fun update(measure: Measure?)

   @Delete
   fun delete(measure: Measure?)
}