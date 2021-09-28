package com.magicbox.wcounter.services

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import com.github.tbouron.shakedetector.library.ShakeDetector
import com.magicbox.wcounter.R
import com.magicbox.wcounter.contracts.CounterServiceContract
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import java.util.*

class CounterService : Service(), CounterServiceContract {

    private val TAG = "WCounter"
    private val TIMEOUT_DURATION: Long = 30000L
    private val TICK_DURATION: Long = 1000L

    private var counter : Int = 0

    private lateinit var binder: CounterBinder
    private lateinit var counterObservable: Observable<Int>
    private lateinit var counterObserver: ObservableEmitter<Int>

    override fun onCreate() {
        super.onCreate()

        counter = resources.getInteger(R.integer.init_count)
        binder = CounterBinder()

        object : CountDownTimer(TIMEOUT_DURATION, TICK_DURATION) {

            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                showToast("Service stopped due to timeout!")
                stopSelf()
            }
        }.start()

        addSensorListeners()
    }

    private fun addSensorListeners() {
        ShakeDetector.create(this) {
            counterObserver.onNext(++counter)
            vibrate()
            showToast("Counting...")
        }
//        ShakeDetector.updateConfiguration(80.0f, 1)
        counterObservable = Observable.create { emitter -> counterObserver = emitter }
        counterObservable = counterObservable.share()
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    open inner class CounterBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): CounterService = this@CounterService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int  = START_NOT_STICKY

    private fun vibrate() {
        val pattern = longArrayOf(0, 200, 100, 300)
        val vibrator = getSystemService<Vibrator>()
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    override fun onPause() {
        ShakeDetector.start()
    }

    override fun onStop() {
        ShakeDetector.stop()
    }

    override fun getCount() : Int = counter
    override fun getCounterObservable(): Observable<Int> = counterObservable

    override fun onDestroy() {
        ShakeDetector.destroy()
    }

    private fun showToast(text : String) {
        Log.d(TAG, text)
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}