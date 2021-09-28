package com.magicbox.wcounter.contracts

import io.reactivex.rxjava3.core.Observable

interface CounterServiceContract {
    fun onPause()
    fun onStop()
    fun getCount() : Int
    fun getCounterObservable() : Observable<Int>
}