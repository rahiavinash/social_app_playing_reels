package com.wedo8020.app.mainui.home.profiletab.mentionAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.TagAndCollabSelectRecycleviewBinding
import com.wedo8020.app.response.UserList
import com.bumptech.glide.Glide

class Tag_Collab_Adapter(
    var list: ArrayList<UserList>,
    onclickLisner: onClickListner,
    var Taglist: ArrayList<UserList>
) : RecyclerView.Adapter<Tag_Collab_Adapter.Viewholder>() {
    var onclickLisner: onClickListner = onclickLisner
    var selectedItemPosition: Int = -1
    var isTagActiveValue = 0
    var isTagActiveId = ""
    var SFtagListData: ArrayList<UserList>? = null
    var RemoveTag = false
    class Viewholder(val view: TagAndCollabSelectRecycleviewBinding) :
        RecyclerView.ViewHolder(view.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val view = TagAndCollabSelectRecycleviewBinding.inflate(LayoutInflater.from(parent.context))

        return Viewholder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        Glide.with(holder.itemView.context).load(list[position].profile)
            .error(R.drawable.profile_dummy).into(holder.view.profileImage)
        holder.view.tagdata = list[position]

        /**  to get user have blueTick */
        if (list[position].blueTickEnable) {
            holder.view.ivBlueTick.visibility = View.VISIBLE
        } else {
            holder.view.ivBlueTick.visibility = View.GONE
        }
        /**  For selected Collab user*/
       /* if (list[position].isCollabActive) {
            holder.view.ivWhiteCollab.visibility = View.VISIBLE
        }*/

        Log.e("selectedList", list[position].name + "::::" + list[position].isTagActive)
        /** For selected Tag user*/
        if (!isTagActiveId.isNullOrEmpty()) {
            if (list[position]._id == isTagActiveId) {
             //   list[position].isTagActive = false
                list[position].isTagSelected=false

            }
        }


        /*if (list[position].isTagActive) {
            holder.view.ivWhiteTag.visibility = View.VISIBLE
        }*/

       /* if (Prefs.init().TagList.length > 0) {
            Taglist = SFtagListData!!
        }*/



        Taglist.forEach { tagItem ->
            if (list[position]._id == tagItem._id) {
                list[position].isTagSelected=true
            }
        }

      /*  if (list[position].isTagSelected){
            holder.view.ivBlueTag.visibility = View.VISIBLE
        }else{
            holder.view.ivBlueTag.visibility = View.GONE
        }*/


/*

        holder.view.ivWhiteTag.setOnClickListener {

            holder.view.let {
                if (it.ivBlueTag.visibility == View.VISIBLE) {
                    it.ivBlueTag.visibility = View.GONE
                } else {
                    it.ivBlueTag.visibility = View.VISIBLE
                }
            }
            onclickLisner.onClickTag(position, list[position])

        }*/



       /* holder.view.ivWhiteCollab.setOnClickListener {
            holder.view.let {
                if (it.ivBlueCollab.visibility == View.VISIBLE) {
                    it.ivBlueCollab.visibility = View.GONE
                } else {
                    it.ivBlueCollab.visibility = View.VISIBLE
                }
            }
            //    onclickLisner.onClickTag(it,position,list[position])
        }*/
    }

    interface onClickListner {
        fun onClickTag(position: Int, data: UserList)
    }



}