package com.bewell.model

import java.io.Serializable

class HRVParam(var value: Double, var name: String, val dimension: String, val minValue: Double, val maxValue: Double, val description: String) :
    Serializable {

}

