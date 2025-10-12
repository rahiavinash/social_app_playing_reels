package com.wedo8020.app.utils
import android.os.Handler
import android.view.MotionEvent
import android.view.View

class LongPressListener(private val onLongPress: () -> Unit) : View.OnTouchListener {

    private val handler = Handler()
    private var longPressDetected = false

    private val longPressRunnable = Runnable {
        if (!longPressDetected) {
            onLongPress.invoke()
            longPressDetected = true
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.postDelayed(longPressRunnable, LONG_PRESS_TIME)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                longPressDetected = false
            }
        }
        return true
    }

    companion object {
        private const val LONG_PRESS_TIME = 1000L // Adjust the time as needed (in milliseconds)
    }
}
