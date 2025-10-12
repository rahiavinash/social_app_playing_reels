package com.wedo8020.app.mainui.home.profiletab.likeGivenAndReceive.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.LikeGivenRecyclelistBinding
import com.wedo8020.app.response.LikeListResponseItem
import com.wedo8020.app.utils.AppConstant.PRIVATE
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GivenLikeAdapter (var list:ArrayList<LikeListResponseItem>, onClickListener:OnClickListener): RecyclerView.Adapter<GivenLikeAdapter.ViewHolder>() {
    private val onClickListener:OnClickListener=onClickListener
    class ViewHolder(val view: LikeGivenRecyclelistBinding): RecyclerView.ViewHolder(view.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding= LikeGivenRecyclelistBinding.inflate(LayoutInflater.from(parent.context))
        return  ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.ivRestrictImage.visibility = View.GONE
        val item = list.getOrNull(position)

        if (item?.profile != null) {
            CoroutineScope(Dispatchers.Main).launch {
                holder.view.ivUserProfileImage.loadImageAsyncMessage(item.profile ?: "")
            }
        }

        holder.view.tvName.text=list[position].username
        list[position].let {
            when {
                it.isFriend || (it.isOtherFollowing && it.followStatus <= 1) -> {
                    holder.view.name.text = "your friend"
                }
                it.isOtherFollowing -> {
                    holder.view.name.text = "follows you"
                }
                it.followStatus == 1 -> {
                    holder.view.name.text = "you follow"
                }
                else -> {
                    holder.view.name.text = list[position].name
                }
            }
        }

        holder.view.tvLikeCount.text=list[position].actionLikes.toString()
        if (list[position].blueTickEnable) {
            holder.view.ivBlueTick.visibility = View.VISIBLE
        } else {
            holder.view.ivBlueTick.visibility = View.GONE
        }


        /** follow status handling*/
        when(list[position].followStatus){
            2 -> {
                holder.view.tvFollow.visibility = View.VISIBLE
                holder.view.tvFollow.text = holder.itemView.context.getText(R.string.follow)
            }
            3 -> {
                holder.view.tvFollow.visibility = View.VISIBLE
                holder.view.tvFollow.text =
                    holder.itemView.context.getText(R.string.requested)
                holder.view.tvFollow.setTextColor(ContextCompat.getColor(holder.itemView.context ,R.color.Grey))
            }
            else -> {
                holder.view.tvFollow.visibility = View.GONE
            }
        }


        holder.view.ivUserProfileImage.setOnClickListener {
            onClickListener.onUserClick(position,list[position])
        }
        holder.view.ivHearts.setOnClickListener {
            holder.view.clLikeCount.disableMultiTap()
            onClickListener.onHearts(position,list[position])
        }
        holder.view.clLikeCount.setOnClickListener {
            holder.view.clLikeCount.disableMultiTap()
            onClickListener.onHearts(position,list[position])
        }

        holder.view.tvFollow.setOnClickListener {
            when (list[position].followStatus) {
                2 -> {
                    if (list[position].accountType.equals(
                            PRIVATE,
                            ignoreCase = true
                        )
                    ) {
                        holder.view.tvFollow.text =
                            holder.itemView.context.getText(R.string.requested)
                        holder.view.tvFollow.setTextColor(ContextCompat.getColor(holder.itemView.context ,R.color.Grey))
                    } else {
                        holder.view.name.text ="you follows"
                        holder.view.name.setTextColor(ContextCompat.getColor(holder.itemView.context ,R.color.Grey))
                        holder.view.tvFollow.visibility=View.GONE
                    }
                    onClickListener.onFollow(position,list[position])
                }
                else -> {

                }
            }


        }
    }

    interface OnClickListener{
        fun onUserClick(position: Int, data: LikeListResponseItem)
        fun onFollow(position: Int, data: LikeListResponseItem)
        fun onHearts(position: Int, data: LikeListResponseItem)
    }
    fun removeItem(index: Int) {
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, list.size)
        }
    }
}