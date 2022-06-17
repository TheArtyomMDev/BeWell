package com.bewell.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "hrvparams")
data class HRVParam(
    @PrimaryKey
    var id: String,

    var name: String,
    val dimension: String,
    val minValue: Double,
    val maxValue: Double,
    val info: String
)

