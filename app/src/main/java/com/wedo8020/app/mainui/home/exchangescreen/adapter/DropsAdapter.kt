package com.wedo8020.app.mainui.home.exchangescreen.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.DropsRecyclerBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.Data
import com.wedo8020.app.utils.Hashtag
import com.wedo8020.app.utils.Utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DropsAdapter(override val layoutId: Int, private val itemClick: DropsClick?= null):
    BaseRecyclerAdapter<DropsRecyclerBinding, Data>(), Hashtag.onHashtagClick {

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun bind(holder: ViewHolder, item: Data, position: Int) {
        holder.binding.likeCount.text = item.monthLikes.toString()
        holder.binding.textCount.text = item.rank.toString()
        Utils.convertDateAndTime(item.lastLike.toString())
        val parsedDate = LocalDate.parse(Utils.yearMonthDate)
        val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("dd MMM"))
        holder.binding.timeDateCount.text = "$formattedDate ${Utils.timeProperFormat}"
        holder.binding.description.text = item.description
        if (!item?.description.isNullOrEmpty()) {
//                        val usernames = Utils.getUsernames(item?.description ?: "")
            val description = Utils.convertDesc(item?.description ?: "")

            val ss = SpannableString(description)
            val hashtagSpans1: ArrayList<IntArray> = Utils.getSpans(description, '#')
            val calloutSpans1: ArrayList<IntArray> = Utils.getSpans(description, '@')
            Utils.setSpanComment(
                ss,
                hashtagSpans1,
                this@DropsAdapter, item?.hashtags?.let { ArrayList(it) } ?: ArrayList()
            )
            Utils.setSpanUname(
                ss,
                calloutSpans1,
                this@DropsAdapter,
                item?.mentionUsers
            )
            holder.binding.description.visibility = View.VISIBLE
            holder.binding.description.movementMethod = LinkMovementMethod.getInstance()
            holder.binding.description.text = ss
            holder.binding.description.maxLines = 1
        }
        Log.d("dropsAdapter", "${item.type}")
        if(item.type == "image"){
            if((item.images?.size ?: 0) > 0) {
                Glide.with(holder.itemView.context).load(item.images?.get(0)).error(R.drawable.profile_dummy).into(holder.binding.ivUserProfileImage)
            }
            else {
                Glide.with(holder.itemView.context).load(R.drawable.profile_dummy).into(holder.binding.ivUserProfileImage)
            }

        }
        else if(item.type=="video"){
            Glide.with(holder.itemView.context).load(item.videoThumbnail).error(R.drawable.profile_dummy).into(holder.binding.ivUserProfileImage)

        }

        holder.itemView.setOnClickListener {

            itemClick?.onClick(item)
        }


    }

    interface DropsClick {
        fun onClick(dropData: Data)
    }

    override fun hashTagClick(text: String) {

    }

    override fun mentionClick(text: String) {

    }


}