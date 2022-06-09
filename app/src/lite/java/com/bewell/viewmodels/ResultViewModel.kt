package com.bewell.viewmodels

import com.bewell.base.MainContract
import com.bewell.data.HRVParam
import com.bewell.MeasureResultActivity

class ResultViewModel: MainContract.Presenter<MeasureResultActivity>  {
    private var view: MeasureResultActivity? = null
    private var params = mutableListOf<Array<String>>()

    override fun attachView(measureResultActivity: MeasureResultActivity) {
        view = measureResultActivity
    }

    override fun detachView() {
        view = null
    }

    fun onParamsReceived(params: MutableList<HRVParam>) {}
}
