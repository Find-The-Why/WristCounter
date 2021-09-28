package com.magicbox.wcounter

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.magicbox.wcounter.contracts.CounterServiceContract
import com.magicbox.wcounter.databinding.ActivityMainBinding
import com.magicbox.wcounter.services.CounterService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : Activity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var service : CounterServiceContract
    private lateinit var disposable : Disposable

    private var shouldBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            service = (binder as CounterService.CounterBinder).getService()
            binding?.counterText.text = "${service.getCount()}"
            shouldBound = true
            disposable = service.getCounterObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { counter ->
                    binding?.counterText.text = "$counter"
                }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            shouldBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(this, CounterService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (shouldBound) {
            service.onStop()
            unbindService(connection)
            shouldBound = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (shouldBound) {
            service.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}