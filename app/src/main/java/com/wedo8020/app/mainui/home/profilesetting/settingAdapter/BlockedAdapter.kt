package com.wedo8020.app.mainui.home.profilesetting.settingAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.databinding.BlockedRecycleviewBinding
import com.wedo8020.app.response.BlockedUserData
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BlockedAdapter (var list:ArrayList<BlockedUserData>,onClickListener:OnClickListener): RecyclerView.Adapter<BlockedAdapter.ViewHolder>() {
  private val onClickListener:OnClickListener=onClickListener
    class ViewHolder(val view: BlockedRecycleviewBinding): RecyclerView.ViewHolder(view.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding= BlockedRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return  ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
      return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.ivRestrictImage.visibility= View.GONE
        CoroutineScope(Dispatchers.Main).launch {
            holder.view.ivUserProfileImage.loadImageAsync(list[position].profile ?: "") }
        holder.view.tvName.text=list[position].username
        holder.view.name.text=list[position].name
        holder.view.tvReport.text="Unblock"

        holder.view.tvReport.setOnClickListener {
            holder.view.tvReport.disableMultiTap()
            onClickListener.onUnblockClick(position,list[position])
        }

        if (list[position].blueTickEnable) {
            holder.view.ivBlueTick.visibility = View.VISIBLE
        } else {
            holder.view.ivBlueTick.visibility = View.GONE
        }

        holder.view.ivUserProfileImage.setOnClickListener {
            holder.view.ivUserProfileImage.disableMultiTap()
            onClickListener.onClickUserProfile(position,list[position])
        }
    }

    interface OnClickListener{
        fun onUnblockClick(position: Int, data: BlockedUserData)
        fun onClickUserProfile(position: Int, data: BlockedUserData)
    }

}