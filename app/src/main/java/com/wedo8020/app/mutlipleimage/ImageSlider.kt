package com.wedo8020.app.mutlipleimage

import android.content.Context

import android.net.Uri
import android.os.Handler
import android.os.Looper

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup
import android.widget.ImageView

import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.wedo8020.app.R


class ImageSlide(private val context: Context, private var imageList: ArrayList<Uri>?) : PagerAdapter() {
    private val handler = Handler(Looper.getMainLooper())
    override fun getCount(): Int {
        return imageList!!.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View =  (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.item, null)
        val ivImages = view.findViewById<ImageView>(R.id.ivImage)
//        Glide.with(requireActivity())
//            .load(Image)
//            .centerCrop()
//            .placeholder(R.drawable.place_holder)
//            .into(binding.profilePostIV)
        ivImages.setImageURI(imageList!![position])
        val vp = container as ViewPager
        vp.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val view = `object` as View
        vp.removeView(view)
    }

}