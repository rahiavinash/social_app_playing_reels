package com.wedo8020.app.mainui.adapter

import android.view.View
import com.wedo8020.app.R
import com.wedo8020.app.databinding.MentionListRecycleviewBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.MentionUserData
import com.bumptech.glide.Glide

class MentionListAdapter(override val layoutId: Int, private val mentionClickListener: mentionClick):BaseRecyclerAdapter<MentionListRecycleviewBinding, MentionUserData>() {
    override fun bind(holder: ViewHolder, item: MentionUserData, position: Int) {
        holder.binding.MentionNames.text=item.username
        holder.binding.followCount.text=item.name
        Glide.with(holder.itemView.context).load(item.profile).error(R.drawable.profile_dummy).into(holder.binding.profileImage)
        holder.itemView.setOnClickListener {
            mentionClickListener.onClick(item._id,item.username)
        }
        if (list[position].blueTickEnable) {
            holder.binding.ivBlueTick.visibility = View.VISIBLE
        } else {
            holder.binding.ivBlueTick.visibility = View.GONE
        }

    }
    interface mentionClick {
        fun onClick(id: String,username : String)
    }
}