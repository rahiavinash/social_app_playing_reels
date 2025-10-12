package com.wedo8020.app.mainui.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.wedo8020.app.R
import com.wedo8020.app.eventBus.HomePagerScroll
import com.wedo8020.app.eventBus.isInnerPageScroll
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class ImageSlide(private val context: Context, private var imageList: List<String>,gestureListener: GestureListener) : PagerAdapter(),OnTouchListener {
    private val handler = Handler(Looper.getMainLooper())
    private val t = gestureListener
    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View =  (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.image_slider_item, null)
        val ivImages = view.findViewById<ImageView>(R.id.ivImage)
        val mainLayout= view.findViewById<ConstraintLayout>(R.id.mainLayout)
        CoroutineScope(Dispatchers.Main).launch {
            ivImages.loadImageAsync(imageList[position])
        }
        mainLayout.setOnTouchListener(this)
        val vp = container as ViewPager
        vp.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val view = `object` as View
        vp.removeView(view)
    }
    private val gestureDetectorCompat = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener(){

        override fun onDoubleTap(e: MotionEvent): Boolean {

            gestureListener.onDoubleTap(e.x,e.y)
//            EventBus.getDefault().post(isDoubleTap(true,e.x,e.y+250,postId,imgView,textView,targetX,targetY))
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            t.onLongPress()
        }
    })


    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        gestureDetectorCompat.onTouchEvent(p1!!)

        when(p1.actionMasked){
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (p1.pointerCount >= 2) {
                    // Two or more fingers are down, start the long press timer
                    handler.postDelayed({
                                        if (p1.pointerCount==1){

                                        }else if (p1.pointerCount==2){
                                            t.onTwoFingerTouchAndHold()
                                        }
                    }, 1000)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if(p1.actionMasked == MotionEvent.ACTION_UP){
                    EventBus.getDefault().post(isInnerPageScroll(true))
                    EventBus.getDefault().post(HomePagerScroll(true))
                }
                if (p1.pointerCount < 2) {
                    // Less than two fingers are down, cancel the long press timer
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }
        return true
    }

    interface GestureListener{
        fun onDoubleTap(x:Float,y:Float)
        fun onTwoFingerTouchAndHold()
        fun onLongPress()
    }
}