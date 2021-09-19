package com.magicbox.wcounter

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import com.magicbox.wcounter.databinding.ActivityMainBinding
import android.widget.Toast

import com.github.tbouron.shakedetector.library.ShakeDetector
import android.os.Vibrator
import androidx.core.content.getSystemService


class MainActivity : Activity() {

    private lateinit var binding : ActivityMainBinding
    private var counter : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        counter = resources.getInteger(R.integer.init_count)
        binding.counterText.text = "${counter++}"

        addSensorListeners()
    }

    private fun addSensorListeners() {
        ShakeDetector.create(this) {
            binding.counterText.text = "${counter++}"
            vibrate()
            Toast.makeText(applicationContext, "Device shaken!", Toast.LENGTH_SHORT).show()
        }
//        ShakeDetector.updateConfiguration(80.0f, 1)
    }

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
        super.onPause()
        ShakeDetector.start()
    }

    override fun onStop() {
        super.onStop()
        ShakeDetector.stop()
    }

    override fun onDestroy() {
        ShakeDetector.destroy()
        super.onDestroy()
    }
}