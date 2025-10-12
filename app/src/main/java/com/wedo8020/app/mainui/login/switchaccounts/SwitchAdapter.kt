package com.wedo8020.app.mainui.login.switchaccounts


import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.SwitchAccountRecyclerviewBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsEntity
import com.wedo8020.app.utils.Prefs


class SwitchAdapter(override val layoutId: Int,private val checkBoxClickListener: CheckBoxClickListener?= null):
    BaseRecyclerAdapter<SwitchAccountRecyclerviewBinding, SwitchAccountsEntity>() {

    private var selectedPosition: Int = -1

    override fun bind(holder: ViewHolder, item: SwitchAccountsEntity, position: Int) {
        holder.binding.tvName.text = item.username
        Glide.with(holder.itemView.context).load(item.profile).error(R.drawable.profile_dummy)
            .into(holder.binding.ivUserProfileImage)

       // holder.binding.checkBox.isChecked = position == selectedPosition

        holder.binding.checkBox.isChecked=false


        if(Prefs.init().username == item.username){
            holder.binding.checkBox.isChecked = true
        }


        holder.binding.checkBox.setOnClickListener {
            checkBoxClickListener?.onCheckBoxClick(item, holder.binding.checkBox.isChecked)
        }
    }


    interface CheckBoxClickListener {
        fun onCheckBoxClick(item: SwitchAccountsEntity, isChecked: Boolean)
    }



}