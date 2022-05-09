package com.bewell.utils

import org.nield.kotlinstatistics.standardDeviation
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Math {

    fun Number.roundDecimalTo(numRoundTo: Int): Double {
        return when(this) {
            //is Double -> this.toBigDecimal().setScale(numRoundTo, RoundingMode.HALF_UP).toDouble()
            is Double -> String.format(Locale.US,"%.${numRoundTo}f", this).toDouble()
            //is Float -> ((this * k).roundToInt() / k)
            else -> throw Exception("Invalid type for rounding")
        }
    }

    fun <T> getModeOf(a: Array<T>): Pair<T, Int> {
        val sortedByFreq = a.groupBy { it }.entries.sortedByDescending { it.value.size }
        val maxFreq = sortedByFreq.first().value.size
        val modes = sortedByFreq.takeWhile { it.value.size == maxFreq }
        return Pair(modes.first().key, maxFreq)
    }

    fun getMxDMn(array: Array<Double>): Double {
        return array.maxOrNull()!! - array.minOrNull()!!
    }

    fun getAMo50(array: Array<Double>): Double {
        val freq = getModeOf(array).second
        return (freq.toDouble()/array.size) * 100.0
    }

    fun getCV(array: Array<Double>): Double {
        val SDNN = array.standardDeviation()
        val MRR = array.average()
        return (SDNN/MRR) * 100
    }

    fun getRMSSD(array: Array<Double>): Double {
        var sumOfDiff = 0.0
        var numOfDiff = 0.0
        for (i in 1 until array.size) {
            sumOfDiff += (array[i] - array[i - 1]).pow(2)
            numOfDiff += 1.0
        }
        return sqrt(sumOfDiff / numOfDiff)
    }

    fun getIN(array: Array<Double>): Double {
        val AMo50 = getAMo50(array)
        val MxDMn = getMxDMn(array)
        val Mode = getModeOf(array).first
        return AMo50/(2*MxDMn*Mode)
    }

    fun getIntervals(array: Array<Double>, numRoundTo: Int = 2): Array<Double> {
        var toAdd: Double?
        val retArray: MutableList<Double> = ArrayList()
        for (i in 1 until array.size) {
            toAdd = (array[i] - array[i - 1]).roundDecimalTo(numRoundTo)
            if (toAdd < 1.2 && toAdd > 0.6) retArray.add(toAdd)
        }
        return retArray.toTypedArray()
    }
}