package com.wedo8020.app.mainui.adapter

import android.view.View
import com.wedo8020.app.clickListeners
import com.wedo8020.app.databinding.SharePostUserListItemBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.Data
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharePostUserAdapter(override val layoutId: Int,val clickListeners: clickListeners) : BaseRecyclerAdapter<SharePostUserListItemBinding,Data>() {
    override fun bind(holder: ViewHolder, item: Data, position: Int) {
        holder.binding.apply {
            data = item
            CoroutineScope(Dispatchers.Main).launch{
                profileImage.loadImageAsync(item.profile)
            }
        }

        if (!item.isChecked){
            holder.binding.selectIndicatior.visibility = View.GONE
        }else{
            holder.binding.selectIndicatior.visibility = View.VISIBLE
        }
        if (!item.isMessageOn){
            holder.itemView.isEnabled = false
            holder.itemView.isClickable = false
            holder.itemView.alpha = 0.5f
        }else{
            holder.itemView.isEnabled = true
            holder.itemView.isClickable = true
            holder.itemView.alpha = 1.0f
        }

        holder.itemView.setOnClickListener {
            clickListeners.itemClicked(position)
                holder.binding.selectIndicatior.visibility = if (item.isChecked){
                    View.VISIBLE
                }else{
                    View.GONE
                }
        }
    }
}