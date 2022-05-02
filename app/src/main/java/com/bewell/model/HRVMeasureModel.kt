package com.bewell.model

class HRVMeasureModel {
    data class param(
        val value: Double,
        val name: String,
        val dimension: String,
        val minValue: Double,
        val maxValue: Double,
        val description: String
    )
}