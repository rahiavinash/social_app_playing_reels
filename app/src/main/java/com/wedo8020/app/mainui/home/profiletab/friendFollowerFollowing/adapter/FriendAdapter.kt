package com.wedo8020.app.mainui.home.profiletab.friendFollowerFollowing.adapter

import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FriendListRecycleviewBinding
import com.wedo8020.app.response.FriendListData
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FriendAdapter(var list: ArrayList<FriendListData>, onClickListener: OnClickListener,var userID: String = "") :
    RecyclerView.Adapter<FriendAdapter.ViewHolder>() {
    private val onClickListener: OnClickListener = onClickListener
    var type = ""
    var followEnableForPrivate = ""
    private var clickHandled = false
    class ViewHolder(val view: FriendListRecycleviewBinding) : RecyclerView.ViewHolder(view.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding = FriendListRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        clickHandled = false
        Log.d("friendAdapter", "Friend Adapter List is $list")
        holder.view.ivRestrictImage.visibility = View.GONE


        holder.view.tvName.text = list[position].username

        holder.view.name.text = list[position].overallLikes.toString()
        if (list[position].blueTickEnable) {
            holder.view.ivBlueTick.visibility = View.VISIBLE
        } else {
            holder.view.ivBlueTick.visibility = View.GONE
        }
        if (!userID.isNullOrEmpty()){
            holder.view.tvFollow.visibility = View.GONE
        }else{
            holder.view.tvFollow.visibility = View.VISIBLE
        }
        if (type == "follower") {
            holder.view.tvFollow.text = "Remove"
            if (list[position].isFriend){
                holder.view.tvFriendFollow.text="Friend"
                holder.view.tvFriendFollow.textSize=holder.itemView.context.resources.getDimensionPixelSize(
                    com.intuit.sdp.R.dimen._5sdp).toFloat()
            }else {

                when (list[position].followStatus){
                    1-> {
                        holder.view.tvFriendFollow.text="Unfollow"
                    }
                    2-> {
                        holder.view.tvFriendFollow.text="Follow"
                    }
                    3-> {
                        holder.view.tvFriendFollow.text="Requested"
                    }
                }
            }
        }
        else if (type =="following"){
            holder.view.tvFriendFollow.visibility=View.GONE
            if (list[position].isFriend){
                holder.view.tvFriendFollow.text="Friend"
                holder.view.tvFriendFollow.textSize=holder.itemView.context.resources.getDimensionPixelSize(
                    com.intuit.sdp.R.dimen._5sdp).toFloat()
            }else{
                holder.view.tvFriendFollow.text=""
            }

            when (list[position].followStatus){
                1-> {
                    holder.view.tvFollow.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.lightblack
                        )
                    )
                    holder.view.tvFollow.text="Unfollow"
                }
                2-> {
                    holder.view.tvFollow.text="Follow"
                    holder.view.tvFollow.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.appBlue
                        )
                    )
                }
                3-> {
                    holder.view.tvFollow.text="Requested"
                    holder.view.tvFollow.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.Grey
                        )
                    )
                }
            }


        }else{
            when (list[position].followStatus){
                1-> {
                    holder.view.tvFollow.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.lightblack
                        )
                    )
                    holder.view.tvFollow.text="Unfollow"
                }
                2-> {
                    holder.view.tvFollow.text="Follow"
                    holder.view.tvFollow.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.appBlue
                        )
                    )
                }
                3-> {
                    holder.view.tvFollow.text="Requested"
                    holder.view.tvFollow.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.Grey
                        )
                    )
                }
            }

        }

        holder.view.tvFriendFollow.setOnClickListener {
            if (holder.view.tvFriendFollow.text=="Follow"){
                if (list[position].accountType=="private"){
                    holder.view.tvFriendFollow.text = "Requested"
                    holder.view.tvFriendFollow.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.appBlue
                        )
                    )
                    holder.view.tvFriendFollow.textSize=holder.itemView.context.resources.getDimensionPixelSize(
                        com.intuit.sdp.R.dimen._4sdp).toFloat()
                }else {
                    holder.view.tvFriendFollow.text = "Friend"
                    holder.view.tvFriendFollow.textSize=holder.itemView.context.resources.getDimensionPixelSize(
                            com.intuit.sdp.R.dimen._5sdp).toFloat()
                    holder.view.tvFriendFollow.setTextColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.appBlue
                        )
                    )
                }
                onClickListener.onFollowAndUnfollow(position, list[position])
            }else{
                holder.view.tvFriendFollow.text = "Follow"
                holder.view.tvFriendFollow.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.appBlue
                    )
                )
                holder.view.tvFriendFollow.textSize=holder.itemView.context.resources.getDimensionPixelSize(
                    com.intuit.sdp.R.dimen._4sdp).toFloat()
                onClickListener.onRequestCancel(position, list[position])
            }

        }


        holder.view.ivUserProfileImage.setOnClickListener {
            onClickListener.onUserClick(position, list[position])
        }

        holder.view.tvFollow.setOnClickListener {
            holder.view.tvFollow.disableMultiTap()
            if (!clickHandled) {
                clickHandled = true
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    clickHandled = false
                }, 1000)
                var accountType = ""
                when (holder.view.tvFollow.text) {
                    "Remove" -> {

                    }

                    else -> {
                        if (holder.view.tvFollow.text == "Follow") {
                            if (list[position].accountType == "private") {
                                holder.view.tvFollow.text = "Requested"
                                holder.view.tvFollow.setTextColor(
                                    ContextCompat.getColor(
                                        holder.itemView.context,
                                        R.color.Grey
                                    )
                                )
                            } else {
                                holder.view.tvFollow.text = "Unfollow"
                                holder.view.tvFollow.setTextColor(
                                    ContextCompat.getColor(
                                        holder.itemView.context,
                                        R.color.LightBlack
                                    )
                                )
                            }
                        } else if (holder.view.tvFollow.text == "Requested") {
                            holder.view.tvFollow.text = "Follow"
                            holder.view.tvFollow.setTextColor(
                                ContextCompat.getColor(
                                    holder.itemView.context,
                                    R.color.appBlue
                                )
                            )
                        } else {
                            if (list[position].accountType == "private") {
                                accountType = "private"
                                holder.view.tvFollow.text = "Unfollow"
                                holder.view.tvFollow.setTextColor(
                                    ContextCompat.getColor(
                                        holder.itemView.context,
                                        R.color.LightBlack
                                    )
                                )
                            } else {
                                holder.view.tvFollow.text = "Follow"
                                holder.view.tvFollow.setTextColor(
                                    ContextCompat.getColor(
                                        holder.itemView.context,
                                        R.color.appBlue
                                    )
                                )
                            }
                        }

                    }
                }
                onClickListener.onFollow(
                    position,
                    list[position],
                    holder.view.tvFollow.text.toString(),
                    accountType
                )
            }
        }
        if (list.size>0){
            if (position<list.size &&  !list[position].profile.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    list[position].profile?.let {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(
                            it
                        )
                    }
                }
            }
        }

    }

    interface OnClickListener {
        fun onUserClick(position: Int, data: FriendListData)
        fun onFollowAndUnfollow(position: Int, data: FriendListData)
        fun onRequestCancel(position: Int, data: FriendListData)
        fun onFollow(position: Int, data: FriendListData ,requestStatus:String , accountType:String)
    }

    fun removeItem(index: Int) {
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, list.size)
        }
    }

    fun updateItem(index: Int , followStatus:Int, profile:String) {
        Log.e("list505", followStatus.toString())
        Log.e("list505", index.toString())
        if (index != -1 && index < list.size) {
            list[index].followStatus = followStatus
            list[index].profile = profile
            notifyItemChanged(index)
            Log.e("list505", list.toString())
        }
    }



}