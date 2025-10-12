package com.wedo8020.app.mainui.home.profiletab.linkedAccount

import android.annotation.SuppressLint
import com.wedo8020.app.databinding.LinkedAccountRvListBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.Users
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.convertDateAndTime
import com.wedo8020.app.utils.Utils.loadImageMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LinkedAdapter(
    override val layoutId: Int,
    private val onClickLinkedAccount: OnClickLinkedAccount
) :
    BaseRecyclerAdapter<LinkedAccountRvListBinding, Users>() {

    @SuppressLint("SetTextI18n")
    override fun bind(holder: ViewHolder, item: Users, position: Int) {
        holder.binding.tvName.text = item.username
        holder.binding.name.text = item.name
        holder.binding.tvARCount.text = item.totalDailyAR.toString() + " " + AppConstant.AR
        convertDateAndTime(item.createdAt)
        holder.binding.tvDate.text = Utils.finalDateFormats
        if (!item.profile.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                item.profile.let {
                    holder.binding.ivUserProfileImage.loadImageMessage(
                        it
                    )
                }
            }
        }

        holder.binding.ivUserProfileImage.setOnClickListener {
            onClickLinkedAccount.onClickProfile(position, list[position])
        }
    }

    interface OnClickLinkedAccount {
        fun onClick(id: String, username: String)
        fun onClickProfile(position: Int, data: Users)
    }


}