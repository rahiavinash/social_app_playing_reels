package com.wedo8020.app.mainui.adapter

import com.wedo8020.app.R
import com.wedo8020.app.databinding.TagUserListItemBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.TagUser
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagUserAdapter(override val layoutId: Int,
    val clickListener: ClickListener) : BaseRecyclerAdapter<TagUserListItemBinding,TagUser>() {
    override fun bind(holder: ViewHolder, item: TagUser, position: Int) {
        holder.binding.apply {
           CoroutineScope(Dispatchers.Main).launch {
               profileImage.loadImageAsync(item.profile)
           }
            username.text = item.username
            followCount.text = item.noOfFollowers.toString()+" "+ holder.itemView.context.getString(R.string.followers)
            totalLike.text = item.overallLikes.toString()
            totalPlay.text = item.overallPosts.toString()
            when(item.followStatus){

                1->{
                    followBtn.text = holder.itemView.context.getText(R.string.following)
                }
                2->{
                    followBtn.text = holder.itemView.context.getText(R.string.follow)
                }
                3->{
                    followBtn.text = holder.itemView.context.getText(R.string.requested)
                }
            }
            followBtn.setOnClickListener {
                when(item.followStatus){
                    1,3->{
                        followBtn.text = holder.itemView.context.getText(R.string.follow)
                    }
                    2->{
                        if (item.accountType.equals("private",ignoreCase = true)){
                            followBtn.text = holder.itemView.context.getText(R.string.requested)
                        }else{
                            followBtn.text = holder.itemView.context.getText(R.string.following)
                        }
                    }
                }
                clickListener.followClick(position)
            }
        }
    }
    interface ClickListener{
        fun followClick(position:Int)
    }
}