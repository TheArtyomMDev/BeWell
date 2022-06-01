package com.bewell.presenter

import com.bewell.base.MainContract
import com.bewell.model.HRVParam
import com.bewell.view.ResultView

class ResultPresenter: MainContract.Presenter<ResultView>  {
    private var view: ResultView? = null
    private var params = mutableListOf<Array<String>>()

    override fun attachView(resultView: ResultView) {
        view = resultView
    }

    override fun detachView() {
        view = null
    }

    fun onParamsReceived(params: MutableList<HRVParam>) {}
}
