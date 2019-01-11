package com.dev.nihitb06.newlightningnote.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeListener (private val onShakeListener: OnShakeListener) : SensorEventListener {

    private var shakeTimeStamp = 0L
    private var shakeCount = 0

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Do Nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val x = (event?.values?.get(0) ?: 0 / SensorManager.GRAVITY_EARTH) * 1.0
        val y = (event?.values?.get(1) ?: 0 / SensorManager.GRAVITY_EARTH) * 1.0
        val z = (event?.values?.get(2) ?: 0 / SensorManager.GRAVITY_EARTH) * 1.0

        val gForce = Math.sqrt(x*x + y*y + z*z)

        if(gForce > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()

            if(shakeTimeStamp + SHAKE_INTERMITTENT_TIME_MS > now) {
                return
            }
            if(shakeTimeStamp + SHAKE_RESET_TIME_MS < now) {
                shakeCount = 0
            }

            shakeTimeStamp = now
            shakeCount++

            onShakeListener.onShake(shakeCount)
        }
    }

    interface OnShakeListener {
        fun onShake(count: Int)
    }

    companion object {
        const val SHAKE_THRESHOLD = 36.0
        const val SHAKE_INTERMITTENT_TIME_MS = 100
        const val SHAKE_RESET_TIME_MS = 1000
    }
}