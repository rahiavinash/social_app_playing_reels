package com.wedo8020.app.mainui.home.profiletab.mentionAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.TagAndCollabSelectRecycleviewBinding
import com.wedo8020.app.response.UserList
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TagCollaborationAdapter(
    var list: ArrayList<UserList>,
    onclickListener: OnClickListener,
    private var tagList: ArrayList<UserList>
) : RecyclerView.Adapter<TagCollaborationAdapter.ViewHolder>() {
    private var onclickListener: OnClickListener = onclickListener
    var isTagActiveId = ""
    var removeTag = false

    class ViewHolder(val view: TagAndCollabSelectRecycleviewBinding) :
        RecyclerView.ViewHolder(view.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TagAndCollabSelectRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*Glide.with(holder.itemView.context).load(list[position].profile)
            .error(R.drawable.profile_dummy).into(holder.view.profileImage)*/
        Log.e("asaSAsASAsA" , list.size.toString())
        if (list.isNullOrEmpty()) {
            if (list.size > position) {
                CoroutineScope(Dispatchers.Main).launch {
                    holder.view.profileImage.loadImageAsyncMessage(list[position].profile)
                }
            }

        }
        holder.view.username.text = list[position].username
        holder.view.followCount.text = list[position].name
        /**  to get user have blueTick */

        if (list[position].blueTickEnable) {
            holder.view.ivBlueTick.visibility = View.VISIBLE
        } else {
            holder.view.ivBlueTick.visibility = View.GONE
        }

        Log.e("selectedList", list[position].name + "::::" + list[position].isTagActive)
        /** For selected Tag user*/
        if (!isTagActiveId.isNullOrEmpty()) {
            if (list[position]._id == isTagActiveId) {
                //   list[position].isTagActive = false
                list[position].isTagSelected = false

            }
        }


        if (list[position].isTagActive) {
            holder.view.ivWhiteTag.visibility = View.VISIBLE
            holder.view.username.setTextColor(
                ContextCompat.getColor(
                    MainApplication.get().applicationContext,
                    R.color.white
                )
            )
        } else {
            holder.view.username.setTextColor(
                ContextCompat.getColor(
                    MainApplication.get().applicationContext,
                    R.color.Grey3
                )
            )
            holder.view.followCount.setTextColor(
                ContextCompat.getColor(
                    MainApplication.get().applicationContext,
                    R.color.Grey3
                )
            )
        }



        tagList.forEach { tagItem ->
            if (list[position]._id == tagItem._id) {
                list[position].isTagSelected = true
            }
        }

        if (list[position].isTagSelected) {
            holder.view.ivBlueTag.visibility = View.VISIBLE
        } else {
            holder.view.ivBlueTag.visibility = View.GONE
        }




        holder.view.ivWhiteTag.setOnClickListener {

            holder.view.let {
                if (it.ivBlueTag.visibility == View.VISIBLE) {
                    it.ivBlueTag.visibility = View.GONE
                } else {
                    it.ivBlueTag.visibility = View.VISIBLE
                }
            }
            onclickListener.onClickTag(position, list[position])

        }


        /*  holder.view.ivWhiteCollab.setOnClickListener {
              holder.view.let {
                  if (it.ivBlueCollab.visibility == View.VISIBLE) {
                      it.ivBlueCollab.visibility = View.GONE
                  } else {
                      it.ivBlueCollab.visibility = View.VISIBLE
                  }
              }
          }*/
    }

    fun addItems(newList: ArrayList<UserList>) {
        val oldPos = list.size + 1
        list.addAll(newList)
        notifyItemRangeInserted(oldPos, newList.size)
    }

    fun setNewItem(newList: ArrayList<UserList>) {
        list.clear()
        list = newList
        notifyDataSetChanged()
    }

    interface OnClickListener {
        fun onClickTag(position: Int, data: UserList)
    }


}