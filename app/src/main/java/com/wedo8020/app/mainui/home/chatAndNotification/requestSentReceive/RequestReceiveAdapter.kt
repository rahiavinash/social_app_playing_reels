package com.wedo8020.app.mainui.home.chatAndNotification.requestSentReceive

import android.view.View
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.RequestRecyclerDataBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.MessageData

class RequestReceiveAdapter(override val layoutId: Int,private val itemClick: RequestItemClick?= null):
    BaseRecyclerAdapter<RequestRecyclerDataBinding, MessageData>() {


    interface RequestItemClick {
        fun onClick(accountId: String, requestType: String, position : Int)
    }

    override fun bind(holder: ViewHolder, item: MessageData, position: Int) {


        Glide.with(holder.itemView.context).load(item.profile).error(R.drawable.profile_dummy).into(holder.binding.ivUserProfileImage)
        holder.binding.tvName.text = item.username
        holder.binding.name.text = item.overallLikes.toString()
        if(item.blueTickEnable == false){
            holder.binding.ivBlueTick.visibility = View.GONE
        }

        holder.binding.tvAccept.visibility = View.VISIBLE
        holder.binding.tvReject.visibility = View.VISIBLE
        holder.binding.tvCancel.visibility = View.GONE


        holder.binding.tvAccept.setOnClickListener {
            item.id?.let { it1 -> itemClick?.onClick(it1, "Accept",position) }
        }
        holder.binding.tvReject.setOnClickListener {
            item.id?.let { it1 -> itemClick?.onClick(it1, "Reject", position) }
        }
        holder.itemView.setOnClickListener {
            item.id?.let { it1 -> itemClick?.onClick(it1,"Other",position) }
        }






    }

}