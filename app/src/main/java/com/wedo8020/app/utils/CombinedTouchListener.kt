package com.wedo8020.app.utils
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View


class CombinedTouchListener(
context: Context,
private val onDoubleTap: () -> Unit,
private val onLongPressListeners: MutableList<() -> Unit> = mutableListOf()
) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    fun addLongPressListener(listener: () -> Unit) {
        onLongPressListeners.add(listener)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTap.invoke()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onLongPressListeners.forEach { it.invoke() }
        }
    }
}

