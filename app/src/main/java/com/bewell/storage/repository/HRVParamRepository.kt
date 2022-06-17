package com.bewell.storage.repository

import com.bewell.data.HRVParam
import com.bewell.storage.AppDatabase

class HRVParamRepository(db: AppDatabase) {
    var hrvParamDao = db.hrvParamDao()

    fun getAllParams(): List<HRVParam> {
        return hrvParamDao!!.getAll()
    }
}