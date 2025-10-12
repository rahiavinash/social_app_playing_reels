package com.wedo8020.app.utils

import android.hardware.Sensor
import android.hardware.SensorEvent

import android.hardware.SensorEventListener
import android.hardware.SensorManager





class ShakeGestureDetector : SensorEventListener {
    private var mLastShakeTimestamp: Long = 0
    private var mListener: OnShakeListener? = null

    interface OnShakeListener {
        fun onShake()
    }

    fun setOnShakeListener(listener: OnShakeListener?) {
        mListener = listener
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type === Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())
                .toFloat() - SensorManager.GRAVITY_EARTH
            val currentTime = System.currentTimeMillis()
            if (acceleration > SHAKE_THRESHOLD_GRAVITY) {
                if (currentTime - mLastShakeTimestamp > SHAKE_SLOP_TIME_MS) {
                    if (mListener != null) {

                        mListener!!.onShake()
                    }
                    mLastShakeTimestamp = currentTime
                }
                mLastShakeTimestamp = currentTime
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 40.7f
        private const val SHAKE_SLOP_TIME_MS = 500
    }
}