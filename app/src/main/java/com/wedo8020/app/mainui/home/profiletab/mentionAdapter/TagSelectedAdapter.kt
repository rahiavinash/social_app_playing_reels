package com.wedo8020.app.mainui.home.profiletab.mentionAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.databinding.TagProfileRecycleviewBinding
import com.wedo8020.app.response.UserList
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagSelectedAdapter(var list:ArrayList<UserList>,onclickListeners: OnClickListener):RecyclerView.Adapter<TagSelectedAdapter.ViewHolder>() {
    private var selectedItemPosition: Int =-1
    private var onclickListener: OnClickListener =onclickListeners
    class ViewHolder(val view :TagProfileRecycleviewBinding):RecyclerView.ViewHolder(view.root) {
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
     val view=TagProfileRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
if (!list.isNullOrEmpty()){
    CoroutineScope(Dispatchers.Main).launch {
        holder.view.profileImage.loadImageAsync(list[position].profile)
    }
}




        holder.view.ivClose.setOnClickListener {
            selectedItemPosition=position
            onclickListener.onRemoveTag(position,list[position])
            list.removeAt(selectedItemPosition)
            notifyItemRemoved(selectedItemPosition)

        }
    }

    override fun getItemCount(): Int {
       return list.size

    }

    interface  OnClickListener{
        fun onRemoveTag( position: Int, data:UserList)
    }

}