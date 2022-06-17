package com.bewell.data

import java.io.Serializable

data class Measure(
    var id: String,
    var timeCreated: Long,

    var sdnn: Double?,
    var mrr: Double?,
    var mxdmn: Double?,
    var mode: Double?,
    var amo50: Double?,
    var cv: Double?,
    var rmssd: Double?,
    var stressIndex: Double?
) : Serializable
