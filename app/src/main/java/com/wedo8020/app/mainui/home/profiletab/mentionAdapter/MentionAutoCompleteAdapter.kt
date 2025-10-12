package com.wedo8020.app.mainui.home.profiletab.mentionAdapter
import android.view.View
import androidx.core.content.ContextCompat
import com.wedo8020.app.R
import com.wedo8020.app.databinding.MentionListRecycleviewBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.MentionUserData
import com.bumptech.glide.Glide

class MentionListAdapter(override val layoutId: Int, private val mentionClickListener: mentionClick ,var type: Boolean =false):
    BaseRecyclerAdapter<MentionListRecycleviewBinding, MentionUserData>() {

    interface mentionClick {
        fun onClick(id: String,username : String)
    }

    override fun bind(holder: ViewHolder, item: MentionUserData, position: Int) {
        if(type){
            holder.binding.clMentionList.setBackgroundColor(ContextCompat.getColor(holder.itemView.context ,R.color.GreyLight) )
            holder.binding.MentionNames.setTextColor(ContextCompat.getColor(holder.itemView.context ,R.color.lightblack) )
            holder.binding.followCount.setTextColor(ContextCompat.getColor(holder.itemView.context ,R.color.PureGrey) )
        }
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
}







