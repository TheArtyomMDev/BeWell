package com.bewell.utils

import org.nield.kotlinstatistics.standardDeviation
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import org.apache.commons.math3.distribution.TDistribution
import kotlin.math.abs
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
        val sdnn = array.standardDeviation()
        val mrr = array.average()
        return (sdnn/mrr) * 100
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

    fun filterArray(array: Array<Double>, confidenceInterval: Double): Array<Double> {
        var t = TDistribution(array.size.toDouble()).inverseCumulativeProbability(confidenceInterval)
        var average = array.average()
        var stdev = array.standardDeviation()
        var ret = mutableListOf<Double>()

        for(elem in array) println(elem)
        println("Array size: ${array.size}, confidenceInterval: $confidenceInterval, t: $t")
        println("Average: $average, standardDeviation: $stdev")

        for(elem in array) {
            var control = abs(elem - average) / stdev
            println("$elem | $control | ${if(control > t) "deleted" else ""}")
            if(control <= t) ret.add(elem)
        }

        return ret.toTypedArray()
    }
}