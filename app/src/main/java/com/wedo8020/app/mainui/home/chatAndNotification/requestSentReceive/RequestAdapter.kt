package com.wedo8020.app.mainui.home.chatAndNotification.requestSentReceive

import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.RequestRecyclerDataBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.MessageData

class RequestAdapter(override val layoutId: Int,private val itemClick: RequestItemClick?= null, private val click : ItemClick?=null):
    BaseRecyclerAdapter<RequestRecyclerDataBinding, MessageData>() {


    interface RequestItemClick {
        fun cancelOnClick(accountId: String, position : Int)
    }

    interface  ItemClick {
        fun onClick(accountId: String)
    }

    override fun bind(holder: ViewHolder, item: MessageData, position: Int) {


        Glide.with(holder.itemView.context).load(item.profile).error(R.drawable.profile_dummy)
            .into(holder.binding.ivUserProfileImage)
        holder.binding.tvName.text = item.username
        holder.binding.name.text = item.overallLikes.toString()
        if (item.blueTickEnable == false) {
            holder.binding.ivBlueTick.visibility = View.GONE
        }
        holder.binding.tvAccept.visibility = View.GONE
        holder.binding.tvReject.visibility = View.GONE
        holder.binding.tvCancel.visibility = View.VISIBLE



        holder.binding.tvCancel.setOnClickListener {
            Log.d("userAccountName", "${item.id}")
            item.id?.let { it1 -> itemClick?.cancelOnClick(accountId = it1, position = position) }
        }

        holder.binding.root.setOnClickListener {
            item.id?.let { it1 -> click?.onClick(accountId = it1) }
        }
    }
}