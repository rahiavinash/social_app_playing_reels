package com.wedo8020.app.mainui.home.profilesetting.settingAdapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.databinding.BlockedRecycleviewBinding
import com.wedo8020.app.response.UserData
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.wedo8020.app.utils.Utils.loadImageMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestrictReportAdapter(var list: ArrayList<UserData>, onClickListener: OnClickListener) :
    RecyclerView.Adapter<RestrictReportAdapter.ViewHolder>() {
    private val onClickListener: OnClickListener = onClickListener
    var type = ""

    class ViewHolder(val view: BlockedRecycleviewBinding) : RecyclerView.ViewHolder(view.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding = BlockedRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (list[position].reportedType == "user") {
            if (list[position].isRestricted) {
                holder.view.tvName.text = list[position].username
                holder.view.name.text = list[position].name
            }
            if (!list.isNullOrEmpty() && position < list.size) {
                if (!list[position].profile.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        list[position].profile?.let {
                            holder.view.ivUserProfileImage.loadImageAsync(
                                it
                            )
                        }
                    }
                }
            }
        } else {
            if (list[position].creator != null) {
                if (!list.isNullOrEmpty() && position < list.size) {
                    if (!list[position].creator?.profile.isNullOrEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            list[position].creator?.profile?.let {
                                holder.view.ivUserProfileImage.loadImageAsync(
                                    it
                                )
                            }
                        }
                    }
                }

                if (list[position].creator?.blueTickEnable == true) {
                    holder.view.ivBlueTick.visibility = View.VISIBLE
                } else {
                    holder.view.ivBlueTick.visibility = View.GONE
                }

                if (list[position].creator != null) {
                    holder.view.tvName.text = list[position].creator?.username
                    holder.view.name.text = list[position].creator?.name
                }
            }
            if (list[position].type == "image") {
                if (!list[position].images.isNullOrEmpty()) {
                    holder.view.ivRestrictImage.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivRestrictImage.loadImageMessage(list[position].images[0])
                    }
                }
            } else {
                if (!list[position].videoThumbnail.isNullOrEmpty()) {
                    holder.view.ivRestrictImage.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivRestrictImage.loadImageMessage(list[position].videoThumbnail!!)
                    }
                }
            }

        }

Log.e("typeeee" , type.toString())
        if (type == "restrict") {
            holder.view.tvReport.text = "Unrestrict"
        } else {
            holder.view.tvReport.text = "Un-Report"
        }

        holder.view.tvReport.setOnClickListener {
            holder.view.tvReport.disableMultiTap()
            holder.view.ivRestrictImage.disableMultiTap()
            holder.view.ivRestrictImage.disableMultiTap()
            onClickListener.onClick(position, list[position])
        }

        holder.view.ivUserProfileImage.setOnClickListener {
            holder.view.tvReport.disableMultiTap()
            holder.view.ivRestrictImage.disableMultiTap()
            holder.view.ivRestrictImage.disableMultiTap()
            onClickListener.onClickUserProfile(position, list[position])
        }

        holder.view.ivRestrictImage.setOnClickListener {
            holder.view.tvReport.disableMultiTap()
            holder.view.ivRestrictImage.disableMultiTap()
            holder.view.ivRestrictImage.disableMultiTap()
            onClickListener.onClickUserProfile(position, list[position])
        }
    }

    interface OnClickListener {
        fun onClick(position: Int, data: UserData)
        fun onClickUserProfile(position: Int, data: UserData)
    }

}