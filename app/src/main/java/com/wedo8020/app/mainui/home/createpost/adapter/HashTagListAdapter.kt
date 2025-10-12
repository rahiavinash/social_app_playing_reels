package com.wedo8020.app.mainui.home.createpost.adapter

import androidx.core.content.ContextCompat
import com.wedo8020.app.R
import com.wedo8020.app.databinding.RecyclerHashtagListBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.request.Hashtag

class HashTagListAdapter(override val layoutId: Int, private val hashTagClickListener: HashTagClick ,var type:Boolean=false):
    BaseRecyclerAdapter<RecyclerHashtagListBinding, Hashtag>() {
    override fun bind(holder: ViewHolder, item: Hashtag, position: Int) {
        if(type){
            holder.binding.clHashTagList.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context ,
                    R.color.GreyLight) )
            holder.binding.hashTagText.setTextColor(
                ContextCompat.getColor(holder.itemView.context ,
                    R.color.lightblack) )
        }
        holder.binding.hashTagText.text = item.tag
        holder.itemView.setOnClickListener {
            hashTagClickListener.onClick(item._id,item.tag)
        }
    }


    interface HashTagClick {
        fun onClick(id: String, username: String)
    }
}

