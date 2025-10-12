package com.wedo8020.app.mainui.home.exchangescreen.adapter

import android.util.Log
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.UserListBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.User

class UserListAdapter(override val layoutId: Int, private val hashTagClickListener: UserListClick):
    BaseRecyclerAdapter<UserListBinding, User>() {
    override fun bind(holder: ViewHolder, item: User, position: Int) {
        holder.binding.tvName.text = item.name
        holder.binding.name.text = item.username
        Log.d("adapterData", "${item.name} ${item._id}")
        Glide.with(holder.itemView.context).load(item.profile).error(R.drawable.profile_dummy).into(holder.binding.ivUserProfileImage)
        holder.itemView.setOnClickListener {

                hashTagClickListener.onClick(item.name.toString(), item.username.toString(),item.profile.toString())

        }
    }


    interface UserListClick {
        fun onClick(name: String, username: String, profile : String)
    }
}
