package com.wedo8020.app.utils

import android.content.Intent
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object Extension {
    fun Fragment.share(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Wedo80")
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "choose one"))
    }

     fun View.viewIsLoaded( run: () -> Unit) {
         if (width > 0 && height > 0) {
             // The view is already laid out, so run the code immediately
             run()
         } else {
             // Set up a listener to be notified when the layout is ready
             this.viewTreeObserver.addOnGlobalLayoutListener(object :
                 ViewTreeObserver.OnGlobalLayoutListener {
                 override fun onGlobalLayout() {
                     // Check again whether the view is laid out (in case it changed in the meantime)
                     if (width > 0 && height > 0) {
                         run()
                         this@viewIsLoaded.viewTreeObserver.removeOnGlobalLayoutListener(this)
                     }
                 }
             })
         }
    }

    fun RecyclerView.waitForFirstVisibleItem(handler: Handler, intervalMillis: Long = 100, callback: () -> Unit) {
        val checkFirstVisibleItem = object : Runnable {
            override fun run() {
                val firstVisibleItemPosition = (layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition() ?: -1

                if (firstVisibleItemPosition != -1) {
                    // First completely visible item found, execute the callback
                    callback.invoke()
                } else {
                    // Retry after the specified interval
                    handler.postDelayed(this, intervalMillis)
                }
            }
        }

        // Start checking for the first visible item
        handler.post(checkFirstVisibleItem)
    }
}
