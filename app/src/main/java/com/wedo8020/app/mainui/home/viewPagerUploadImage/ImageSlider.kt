package com.wedo8020.app.mainui.home.viewPagerUploadImage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.net.Uri
import android.util.Log

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup
import android.widget.ImageView

import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.wedo8020.app.R
import java.io.IOException
import java.io.InputStream


class ImageSlider(private val context: Context, private var uriList: ArrayList<Uri>?) : PagerAdapter() {


    override fun getCount(): Int {
        Log.d("imageslideCount", "image slide is ${uriList?.size}")
        return uriList?.size ?: 0
    }

    fun updateDataSet(newUriList: ArrayList<Uri>?) {
        uriList = newUriList
        notifyDataSetChanged()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.item, null
        )
        val ivImages = view.findViewById<ImageView>(R.id.ivImage)

        val uri = uriList?.get(position)
        if (uri != null) {
            val bitmap = decodeUriToBitmap(uri)
            ivImages.setImageBitmap(bitmap)
        }

        val vp = container as ViewPager
        vp.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val view = `object` as View
        vp.removeView(view)
    }

    private fun decodeUriToBitmap(uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            // Handle exceptions (e.g., log the error)
            Log.e("ImageSlide", "Error decoding bitmap from URI: $e")
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                // Handle closing the input stream exception if necessary
                Log.e("ImageSlide", "Error closing input stream: $e")
            }
        }
        return null
    }
}
