package com.wedo8020.app.mainui.home.chatAndNotification.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.ShowMessageRecycleviewBinding
import com.wedo8020.app.enumClasses.ChatType
import com.wedo8020.app.response.UserMessageData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.TimeZone


class UserChatAdapter(val messageList :ArrayList<UserMessageData>, clickListener: onClicklistner,):
    RecyclerView.Adapter<UserChatAdapter.ViewHolder>() {
    val clickListener: onClicklistner = clickListener
    var date = ""

    class ViewHolder(val view:ShowMessageRecycleviewBinding): RecyclerView.ViewHolder(view.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding=
            ShowMessageRecycleviewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(mBinding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        /*  Log.e("23434234234",messageList[position].isSameDate.toString()+"::::::"+position)*/
        holder.view.dateLayout.visibility = View.VISIBLE
        if (messageList[position].createdAt == getCurrentDate()) {
            holder.view.tvMessageDate.text = "Today"
        } else {
            holder.view.tvMessageDate.text =
                getServerFormattedOnlyDate(messageList[position].createdAt)
        }

/*        if (messageList[position].isSameDate != null || messageList[position].isSameDate.equals("yes") || position == 0){
            holder.view.dateLayout.visibility= View.GONE
        }
        else{
            holder.view.dateLayout.visibility= View.VISIBLE
            if (messageList[position].createdAt.equals(getCurrentDate())){
                holder.view.tvMessageDate.text="Today"
            }else{
                holder.view.tvMessageDate.text=getServerFormattedOnlyDate(messageList[position].createdAt)
            }*/



        holder.view.childMessageAdapter.setOnTouchListener { v, event ->

          //  clickListener!!.removeReaction()
            true
        }


        /*      if(position==0){
                  holder.view.dateLayout.visibility = View.GONE
              }*/
       /* val childAdapter =  MessageAdapter(getLayoutsMap(),clickListener!! ,)
        val list = ArrayList<UserMessageData>()
        list.addAll(messageList)


        childAdapter.list=list*/

/*        holder.view.childMessageAdapter.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
        }
        holder.view.childMessageAdapter.adapter = childAdapter*/

    }

    @SuppressLint("SimpleDateFormat")
    fun getServerFormattedOnlyDate(time: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateFormat.timeZone = TimeZone.getDefault()
        var currentTime = ""
        var date: Date? = null
        try {
            date = dateFormat.parse(time)
        } catch (e: java.lang.Exception) {
            Log.d("Exception", "getFormattedDate: unable to parse date.")
        }

        if (date != null) {
            currentTime = SimpleDateFormat("MMMM d, yyyy").format(date)
        }
        return currentTime
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
    interface onClicklistner {
        fun onClickMessageProfileImage(view: View, position: Int)
/*        fun onClickDeletee(msg : Int)
        fun onClickMentionedFriend(userId : Int)
        fun onSendReaction(msgId : Int,unicode : String)
       *//* fun showReactionMenu(msgId : Int, view: View, position: Int, y2 : Int, y: Int, senderId : Int,
                             reactionList : ArrayList<Reaction>, emojiLayout: RelativeLayout, textView: TextView
        )*//*
*//*        fun showFullImage(item : Message, view: View, position: Int)*//*
        fun onGetReactions(msgId : Int,position : Int,y2 : Int,y: Int,height : Int,senderId : Int)*/
     /*   fun removeReaction()*/


    }


    private fun getLayoutsMap(): HashMap<ChatType, Int> {
        val layoutMap = HashMap<ChatType, Int>()
        layoutMap[ChatType.MESSAGE_SENDER] = R.layout.send_message_layout
        layoutMap[ChatType.MESSAGE_RECEIVER] = R.layout.receive_message_layout
        layoutMap[ChatType.IMAGE_SENDER] = R.layout.image_sender_layout
        layoutMap[ChatType.IMAGE_RECEIVER] = R.layout.image_receiver_layout
        return layoutMap
    }

    private fun getCurrentDate() : String{
        val calendar = Calendar.getInstance()
        val timeZone = TimeZone.getDefault()
        calendar.timeZone = timeZone
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(calendar.time)
        return currentDate
    }



}