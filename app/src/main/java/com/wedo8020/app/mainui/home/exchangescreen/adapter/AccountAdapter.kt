package com.wedo8020.app.mainui.home.exchangescreen.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.AccountsRecyclerBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.LeaderboardItem
import com.wedo8020.app.utils.Utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AccountAdapter(override val layoutId: Int,private val accountItemClick: AccountItemClick?= null):
    BaseRecyclerAdapter<AccountsRecyclerBinding, LeaderboardItem>() {




    @RequiresApi(Build.VERSION_CODES.O)

    @SuppressLint("SetTextI18n")
    override fun bind(holder: ViewHolder, item: LeaderboardItem, position: Int) {
        holder.binding.likeCount.text = item.monthLikes.toString()
        holder.binding.name.text = item.username.toString()
        holder.binding.userName.text = item.name.toString()
        holder.binding.textCount.text = item.rank.toString()
        Utils.convertDateAndTime(item.lastLike.toString())
        val parsedDate = LocalDate.parse(Utils.yearMonthDate)
        if(item.isBlueTickEnabled == true) {
            holder.binding.ivBlueTick.visibility = View.VISIBLE
        }
        else {
            holder.binding.ivBlueTick.visibility = View.GONE
        }
        val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("dd MMM"))
        holder.binding.timeDateCount.text = "$formattedDate ${Utils.timeProperFormat}"
        Glide.with(holder.itemView.context).load(item.profile).error(R.drawable.profile_dummy).into(holder.binding.ivUserProfileImage)
        holder.itemView.setOnClickListener {
            accountItemClick?.onClick(item._id?:"")
        }
    }
    interface AccountItemClick {
        fun onClick(accountId: String)
    }


}