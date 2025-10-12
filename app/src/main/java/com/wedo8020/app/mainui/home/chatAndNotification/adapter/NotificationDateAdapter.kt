package com.wedo8020.app.mainui.home.chatAndNotification.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.databinding.NotificationdateRecycleviewBinding
import com.wedo8020.app.response.NotificationListData
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.daysDiff
import com.wedo8020.app.utils.Utils.yearMonthDate

class NotificationDateAdapter ( var list: ArrayList<NotificationListData>
) : RecyclerView.Adapter<NotificationDateAdapter.ViewHolder>() {
//    private lateinit

    class ViewHolder(val view: NotificationdateRecycleviewBinding) :
        RecyclerView.ViewHolder(view.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding =
            NotificationdateRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ResourceType", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dateTimeString = list[position].createdAt
        if (!dateTimeString.isNullOrEmpty()) {
            Utils.convertDateAndTime(dateTimeString)
        }
        if (daysDiff<1L){
            holder.view.tvToday.text = "Today"
            holder.view.tvToday.visibility=View.VISIBLE
            holder.view.tvDayMonth.visibility=View.GONE
        }else if (daysDiff==1L){
            holder.view.tvToday.text = "Yesterday"
            holder.view.tvToday.visibility=View.VISIBLE
            holder.view.tvDayMonth.visibility=View.GONE
        }else{
            holder.view.tvToday.visibility=View.GONE
            holder.view.tvDayMonth.visibility=View.VISIBLE
            holder.view.tvDayMonth.text = yearMonthDate
        }
    /*    val adapters: NotificationAdapter = NotificationAdapter(list,this@NotificationDateAdapter)
        Utils.printLog(tag = "NotificationAdapter", msg = list.size.toString())
        holder.view.rvNotificationList.adapter = adapters
        adapters.notifyDataSetChanged()*/

    }


}