package com.wedo8020.app.mainui.home.exchangescreen.adapter

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.wedo8020.app.databinding.PurchaseHistoryRecyclerBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.Transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PurchaseHistoryAdapter(override val layoutId: Int):
    BaseRecyclerAdapter<PurchaseHistoryRecyclerBinding, Transaction>() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun bind(holder: ViewHolder, item: Transaction, position: Int) {

//        if(item.otherUser == null){
//            holder.binding.otherUserType.text = "WeDo 80,"
//            holder.binding.reason.text = item.reason
//        }
//        else {
//            holder.binding.otherUserType.text = "${item.otherUser.name} ,"
//            holder.binding.reason.text = item.reason
//        }

        holder.binding.reason.text = item.reason
        if(item.action == "credit") {
            holder.binding.debitedAr.visibility = View.GONE
            holder.binding.creditedAr.visibility = View.VISIBLE
            holder.binding.creditedAr.text = "${item.ar} AR"
        }
        if(item.action == "debit") {
            holder.binding.debitedAr.visibility = View.VISIBLE
            holder.binding.creditedAr.visibility = View.GONE
            holder.binding.debitedAr.text = "- ${item.ar} AR"
        }
        val dateTimeString = item.date

        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(dateTimeString, formatter)

        val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH)

        val formattedDate = dateTime.format(dateFormatter)
        val formattedTime = dateTime.format(timeFormatter)
        Log.d("dataTime", " $formattedTime, $formattedDate")
        holder.binding.dateText.text = formattedDate.toString()
        holder.binding.timeText.text = formattedTime.toString()


    }


}