package com.bewell.base


interface MainContract {

    interface Presenter<T> {
        fun attachView(view: T)
        fun detachView()
    }

}
