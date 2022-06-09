package com.bewell.data

import java.io.Serializable

data class HRVParam(
    var value: Double,
    var name: String,
    val dimension: String,
    val minValue: Double,
    val maxValue: Double,
    val description: String
    ) : Serializable

