package com.wedo8020.app.base

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.wedo8020.app.R


object BindingAdapter {
    @JvmStatic
    @BindingAdapter("bind:imageUrl")
    fun setImageUl(image: ImageView, url: String?) {
        Log.e("imageUrl", url.toString())

        if (!url.isNullOrEmpty()) {

            Glide.with(image.context)
                .load("$url")
                .error(R.drawable.ic_greyuser)
                .into(image);

        }else{
            Glide.with(image.context).load(R.drawable.ic_greyuser).into(image)
        }
    }

}