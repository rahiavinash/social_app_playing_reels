package com.wedo8020.app.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.wedo8020.app.databinding.CustomMessageNotificationBinding
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.wedo8020.app.utils.Utils.messgaegDisableMultiTap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

object ShowCustomMessage {
    private lateinit var toast : Toast
    private var handler = android.os.Handler(Looper.myLooper()!!)
    private var topMargin = 200 // Initial top margin for the first custom layout view
    private const val DISPLAY_DURATION_MS = 2000L // Duration in milliseconds
    private const val SWIPE_THRESHOLD = 150 // Swipe threshold in pixels
    private const val SWIPE_VELOCITY_THRESHOLD =
        200 // Swipe velocity threshold in pixels per second
//    private const val MARGIN_INCREMENT = 300 // Margin increment for each new custom layout

    private val messageQueue = mutableListOf<View>()

    /* fun addCustomLayout(
         message: String,run: () -> Unit ,
         heading: String = "",
         imageUrl: String = "",
         color: Int = R.color.card_notification_color
     ) {
         val rootView =
             MainApplication.get().appLifecycleObserver.activity?.findViewById<ViewGroup>(android.R.id.content)
         rootView?.let {
             val context = MainApplication.get().appLifecycleObserver.activity
             val customLayoutBinding =
                 CustomMessageNotificationBinding.inflate(LayoutInflater.from(context))

             val params = ViewGroup.MarginLayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT
             )

             // Set top margin for the new custom layout
             params.topMargin = topMargin
             params.leftMargin = 20
             params.rightMargin = 20
             topMargin += MARGIN_INCREMENT
             customLayoutBinding.apply {
                 val newMessageView = root
                 newMessageView.layoutParams = params
                 it.addView(newMessageView)

                 txtMsg.text = message
                 profileName.text = heading.ifBlank { context?.getString(R.string.app_name) }

                 mainCard.setOnClickListener { run() }
                 if (imageUrl.startsWith("https")) {
                     CoroutineScope(Dispatchers.IO).launch {
                         profileImg.loadImageAsync(imageUrl)
                     }
                 }


                 mainCard.setCardBackgroundColor(MainApplication.get().applicationContext.getColor(color))

                 // Increment the top margin for the next custom layout


                 // Set up swipe-to-remove gesture detection
                 setupSwipeToDismiss(it, newMessageView)

                 // Add the new message to the queue
                 messageQueue.add(newMessageView)

                 // Remove the view after a certain duration
                 removeCustomLayoutAfterDelay(context, newMessageView)
             }
         }
     }*/
    fun addCustomLayout(
        message: String,
        heading: String = "",
        imageUrl: String = "",
        color: Int = R.color.card_notification_color,
        run: (() -> Unit)? = null
    ) {
        try {

            if(MainApplication.get().appLifecycleObserver.activity !=null) {
                val rootView =
                    MainApplication.get().appLifecycleObserver.activity?.window?.decorView as ViewGroup

                if (run == null) {
                    if (::toast.isInitialized) {
                        toast.cancel()
                    }
                    val activity = MainApplication.get().appLifecycleObserver.activity
                    val inflater = activity?.layoutInflater
                    val layout = inflater?.inflate(
                        R.layout.toast_layout,
                        activity.findViewById(R.id.toast_layout)
                    )

                    val text = layout?.findViewById<TextView>(R.id.toast_text)
                    text?.text = message

                    toast = Toast(activity)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.view = layout
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    return
                }
                rootView?.let {
                    val context = MainApplication.get().appLifecycleObserver.activity
                    val customLayoutBinding =
                        CustomMessageNotificationBinding.inflate(LayoutInflater.from(context))

                    val params = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    // Set top margin for the new custom layout
                    params.topMargin = topMargin
                    params.leftMargin = 20
                    params.rightMargin = 20

                    // Remove previous views from messageQueue
                    messageQueue.forEach { previousView ->
                        it.removeView(previousView)
                    }
                    messageQueue.clear()

                    customLayoutBinding.mainLayout.setOnClickListener { view ->
                        run()
                        view.messgaegDisableMultiTap()
                        animateRemoveView(it, customLayoutBinding.root, 0f)
                    }
//           topMargin += MARGIN_INCREMENT
                    customLayoutBinding.apply {
                        val newMessageView = root
                        newMessageView.layoutParams = params
                        it.addView(newMessageView)

                        txtMsg.text = message
                        profileName.text = heading.ifBlank { context?.getString(R.string.app_name) }


                        if (imageUrl.startsWith("https")) {
                            CoroutineScope(Dispatchers.IO).launch {
                                profileImg.loadImageAsync(imageUrl)
                            }
                        }

                        mainCard.setCardBackgroundColor(
                            MainApplication.get().applicationContext.getColor(
                                color
                            )
                        )

                        // Increment the top margin for the next custom layout

                        // Set up swipe-to-remove gesture detection
                        setupSwipeToDismiss(it, newMessageView)

                        // Add the new message to the queue
                        messageQueue.add(newMessageView)

                        // Remove the view after a certain duration
                        removeCustomLayoutAfterDelay(context, newMessageView)
                    }
                }
            }
        }
        catch (e : Exception){
            Log.e("exception", e.message.toString())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeToDismiss(rootView: ViewGroup, view: View) {
        var initialX = 0f
        var isSwiping = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX - view.x
                    isSwiping = false
                    handler.postDelayed({},500)
                }

                MotionEvent.ACTION_MOVE -> {
                    val newMargin = event.rawX - initialX
                    if (abs(newMargin) > SWIPE_THRESHOLD) {
                        isSwiping = true
                        // Move the view horizontally along with touch
                        view.x = newMargin
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (isSwiping) {
                        val deltaX = event.rawX - initialX
                        val velocityTracker = VelocityTracker.obtain()
                        velocityTracker.addMovement(event)
                        velocityTracker.computeCurrentVelocity(1000)
                        val xVelocity = velocityTracker.xVelocity

                        if (abs(deltaX) > view.width / 2 || abs(xVelocity) > SWIPE_VELOCITY_THRESHOLD) {
                            // If swiped more than half of the screen or swiped quickly, remove the view
                            animateRemoveView(rootView, view, deltaX)
                        } else {
                            // If not swiped enough, reset the view position
                            resetViewPosition(view)
                            // Resume the scheduled removal
                            return@setOnTouchListener false
                        }

                        velocityTracker.recycle()
                    }
                }
                else -> {
                    return@setOnTouchListener false
                }
            }
            true
        }
    }

    private fun animateRemoveView(rootView: ViewGroup, viewToRemove: View, deltaX: Float) {
        val directionMultiplier = if (deltaX > 0) 1 else -1
        val animator =
            ValueAnimator.ofFloat(viewToRemove.x, directionMultiplier * rootView.width.toFloat())
        animator.duration = 500 // Adjust animation duration as needed

        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            // Move the view horizontally along with the animation
            viewToRemove.x = value
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                rootView.removeView(viewToRemove)
                messageQueue.remove(viewToRemove)
                updateMessagePositions()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.start()
    }

    private fun resetViewPosition(view: View) {
        view.x = 20f // Reset x position
    }

    private fun updateMessagePositions() {
        var currentTopMargin = 200
        for (messageView in messageQueue) {
            messageView.y = currentTopMargin.toFloat() // Set y position
//            currentTopMargin += MARGIN_INCREMENT // Increment for the next message
        }
        topMargin = currentTopMargin
    }

    private fun removeCustomLayoutAfterDelay(context: Context?, viewToRemove: View) {
        Utils.runWithDelay(DISPLAY_DURATION_MS) {
            animateRemoveView(
                (context as? Activity)?.findViewById<ViewGroup>(android.R.id.content)
                    ?: return@runWithDelay,
                viewToRemove,
                0f
            )
        }
    }
}

/*object ShowCustomMessage {
    private var topMargin = 200 // Initial top margin for the first custom layout view
    private const val DISPLAY_DURATION_MS = 5000L // Duration in milliseconds
    private const val SWIPE_THRESHOLD = 150 // Swipe threshold in pixels
    private const val SWIPE_VELOCITY_THRESHOLD = 200 // Swipe velocity threshold in pixels per second
    private const val MARGIN_INCREMENT = 200 // Margin increment for each new custom layout

    private val messageQueue = mutableListOf<View>()

    fun addCustomLayout(
        activity: Activity,
        message: String,
        heading: String = "",
        imageUrl: String = "",
        color: Int = R.color.card_notification_color
    ) {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        val customLayoutBinding = CustomMessageNotificationBinding.inflate(activity.layoutInflater)

        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set top margin for the new custom layout
        params.topMargin = topMargin
        params.leftMargin = 20
        params.rightMargin = 20

        customLayoutBinding.apply {
            val newMessageView = root
            newMessageView.layoutParams = params
            rootView.addView(newMessageView)

            txtMsg.text = message
            profileName.text = heading.ifBlank { activity.getString(R.string.app_name) }

            if (imageUrl.startsWith("https")) {
                CoroutineScope(Dispatchers.IO).launch {
                    profileImg.loadImageAsync(imageUrl)
                }
            }

            mainCard.setCardBackgroundColor(activity.getColor(color))

            // Increment the top margin for the next custom layout
            topMargin += MARGIN_INCREMENT

            // Set up swipe-to-remove gesture detection
            setupSwipeToDismiss(rootView, newMessageView)

            // Add the new message to the queue
            messageQueue.add(newMessageView)

            // Remove the view after a certain duration
            removeCustomLayoutAfterDelay(activity, newMessageView)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeToDismiss(rootView: ViewGroup, view: View) {
        var initialX = 0f
        var isSwiping = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX - view.x
                    isSwiping = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val newMargin = event.rawX - initialX
                    if (abs(newMargin) > SWIPE_THRESHOLD) {
                        isSwiping = true
                        // Move the view horizontally along with touch
                        view.x = newMargin
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isSwiping) {
                        val deltaX = event.rawX - initialX
                        val velocityTracker = VelocityTracker.obtain()
                        velocityTracker.addMovement(event)
                        velocityTracker.computeCurrentVelocity(1000)
                        val xVelocity = velocityTracker.xVelocity

                        if (abs(deltaX) > view.width / 2 || abs(xVelocity) > SWIPE_VELOCITY_THRESHOLD) {
                            // If swiped more than half of the screen or swiped quickly, remove the view
                            animateRemoveView(rootView, view, deltaX)
                        } else {
                            // If not swiped enough, reset the view position
                            resetViewPosition(view)
                            // Resume the scheduled removal
                        }

                        velocityTracker.recycle()
                    }
                }
            }
            true
        }
    }

    private fun animateRemoveView(rootView: ViewGroup, viewToRemove: View, deltaX: Float) {
        val directionMultiplier = if (deltaX > 0) 1 else -1
        val animator = ValueAnimator.ofFloat(viewToRemove.x, directionMultiplier * rootView.width.toFloat())
        animator.duration = 500 // Adjust animation duration as needed

        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            // Move the view horizontally along with the animation
            viewToRemove.x = value
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                rootView.removeView(viewToRemove)
                messageQueue.remove(viewToRemove)
                updateMessagePositions()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.start()
    }

    private fun resetViewPosition(view: View) {
        view.x = 20f // Reset x position
    }

    private fun updateMessagePositions() {
        var currentTopMargin = 0
        for (messageView in messageQueue) {
            messageView.y = currentTopMargin.toFloat() // Set y position
            currentTopMargin += MARGIN_INCREMENT // Increment for the next message
        }
        topMargin = currentTopMargin
    }

    private fun removeCustomLayoutAfterDelay(activity: Activity, viewToRemove: View) {
        Utils.runWithDelay(DISPLAY_DURATION_MS) {
            animateRemoveView(activity.findViewById(android.R.id.content), viewToRemove, 0f)
        }
    }
}*/

