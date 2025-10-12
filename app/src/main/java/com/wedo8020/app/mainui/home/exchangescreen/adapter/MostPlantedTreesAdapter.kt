package com.wedo8020.app.mainui.home.exchangescreen.adapter

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.MostPlantedTreesRecyclerBinding

import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.UserType
import com.wedo8020.app.utils.Utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MostPlantedTreesAdapter(override val layoutId: Int,private val itemClick: MostPlantedItemClick?= null):
    BaseRecyclerAdapter<MostPlantedTreesRecyclerBinding, UserType>() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun bind(holder: ViewHolder, item: UserType, position: Int) {
        holder.binding.likeCount.text = item.eTrees.toString()
        holder.binding.name.text = item.username.toString()
        holder.binding.userName.text = item.name.toString()
        holder.binding.textCount.text = item.rank.toString()
        Utils.convertDateAndTime(item.lastPurchase.toString())
        val parsedDate = LocalDate.parse(Utils.yearMonthDate)
        if(item.blueTickEnable == true) {
            holder.binding.ivBlueTick.visibility = View.VISIBLE
        }
        else {
            holder.binding.ivBlueTick.visibility = View.GONE
        }
        val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("dd MMM"))
        holder.binding.timeDateCount.text = "$formattedDate ${Utils.timeProperFormat}"
        Glide.with(holder.itemView.context).load(item.profile).error(R.drawable.profile_dummy).into(holder.binding.ivUserProfileImage)

        holder.itemView.setOnClickListener {
            itemClick?.onClick(item._id.toString())
        }
    }

    interface MostPlantedItemClick {
        fun onClick(mostPlantedId : String)
    }


}