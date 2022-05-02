package com.bewell.storage

import android.content.Context
import android.content.SharedPreferences

class Preferences(context: Context) {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor
        get() = preferences.edit()

    var measureTimeInSec: Int?
        get() = preferences.getInt(PREF_MEASURE_TIME_IN_SEC, 0)
        set(data) {
            editor.putInt(PREF_MEASURE_TIME_IN_SEC, data!!).commit()
        }
    var generalStartTimeInMilliSec: Int?
        get() = preferences.getInt(PREF_GENERAL_START_TIME_IN_MILLI_SEC, 0)
        set(data) {
            editor.putInt(PREF_GENERAL_START_TIME_IN_MILLI_SEC, data!!).commit()
        }

    companion object {
        const val FILE_NAME = "preferences"
        const val PREF_GOING = "alreadyUsed"
        const val PREF_MEASURE_TIME_IN_SEC = "measureTimeInSec"
        const val PREF_GENERAL_START_TIME_IN_MILLI_SEC = "generalStartTimeInMilliSec"
    }

    init {
        preferences = context.getSharedPreferences(FILE_NAME, 0)
    }

}