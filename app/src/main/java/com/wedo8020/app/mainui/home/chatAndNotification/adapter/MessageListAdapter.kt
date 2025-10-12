package com.wedo8020.app.mainui.home.chatAndNotification.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.apachat.swipereveallayout.core.SwipeLayout
import com.apachat.swipereveallayout.core.interfaces.Swipe
import com.wedo8020.app.R
import com.wedo8020.app.databinding.BlockedRecycleviewBinding
import com.wedo8020.app.databinding.MessagelistRecycleviewBinding
import com.wedo8020.app.response.BlockedUserData
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.MessageListData
import com.wedo8020.app.utils.Extension
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.daysDiff
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.hours
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.wedo8020.app.utils.Utils.minutes
import com.wedo8020.app.utils.Utils.monthsDiff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class MessageListAdapter(
    var list: ArrayList<MessageListData>? = null,
    onClickListener: OnClickListener
) : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {
    private val onClickListener: OnClickListener = onClickListener
    var swipeLayout: SwipeLayout? = null

    class ViewHolder(val view: MessagelistRecycleviewBinding) : RecyclerView.ViewHolder(view.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding = MessagelistRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return list?.size!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ResourceType")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.ivRestrictImage.visibility = View.GONE
        if (!list.isNullOrEmpty()) {
            if (list?.get(position)?.noOfUnseenMessages!! > 0) {
                val messageCount = list?.get(position)?.noOfUnseenMessages
                if (messageCount != null) {
                    if (messageCount > 99) {
                        holder.view.tvMessageCount.setText(R.string.more_than_99)
                    } else {
                        holder.view.tvMessageCount.text =
                            list?.get(position)?.noOfUnseenMessages.toString()
                    }
                }
                holder.view.tvMessageCount.visibility = View.VISIBLE

            } else {
                holder.view.tvMessageCount.visibility = View.GONE
            }

            /** Handling the blue tick functionality*/
            if (list!![position].otherUser?.blueTickEnable == true) {
                holder.view.ivBlueTick.visibility = View.VISIBLE
            } else {
                holder.view.ivBlueTick.visibility = View.GONE
            }

            /** Handling the Pin functionality*/
            if (!list!![position].pinTime.isNullOrEmpty()) {
                holder.view.tvPinUnPin.text = "Unpin"
                holder.view.pinMsg.setImageResource(R.drawable.ic_unpinwhite)
            } else {
                holder.view.tvPinUnPin.text = "Pin"
                holder.view.pinMsg.setImageResource(R.drawable.ic_pin)
            }

            if (position < list?.size!! && !list.isNullOrEmpty()) {
                if (list!![position].otherUser!=null){
                    if (!list!![position].otherUser?.profile.isNullOrEmpty()){
                        CoroutineScope(Dispatchers.Main).launch {
                            list?.get(position)?.otherUser?.profile?.let {
                                holder.view.ivUserProfileImage.loadImageAsync(
                                    it
                                )
                            }
                        }
                    }else{
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.setImageResource(R.drawable.ic_greyuser)
                        }
                    }
                }
            }
            else{
                CoroutineScope(Dispatchers.Main).launch {
                    holder.view.ivUserProfileImage.setImageResource(R.drawable.ic_greyuser)
                }
            }

            holder.view.tvName.text = list?.get(position)?.otherUser?.username
            when (list?.get(position)?.lastMessage?.type) {
                "sharePost" -> {
                    if (list?.get(position)?.lastMessage?.sender == Prefs.init().profileId) {
                        if (list?.get(position)?.lastMessage?.message=="Unsent"){
                            holder.view.name.text = "Unsent"
                        }else {
                            if (list?.get(position)?.lastMessage?.isMessageHide==true){
                                holder.view.name.text = "Unsent"
                            }else{
                                holder.view.name.text = "You : Shared a Drop"
                            }
                        }
                    } else {
                        if (list?.get(position)?.lastMessage?.message=="Unsent"){
                            holder.view.name.text = "Unsent"
                        }else {
                            if (list?.get(position)?.lastMessage?.isMessageHide==true){
                                holder.view.name.text = "Unsent"
                            }else{
                                holder.view.name.text = "Shared a Drop with you"
                            }

                        }
                    }
                }
                "video" -> {
                    if (list?.get(position)?.lastMessage?.sender == Prefs.init().profileId) {
                        if (list?.get(position)?.lastMessage?.message=="Unsent"){
                            holder.view.name.text = "Unsent"
                        }else{
                            if (list?.get(position)?.lastMessage?.isMessageHide==true){
                                holder.view.name.text = "Unsent"
                            }else{
                                holder.view.name.text = "You : Shared a video"
                            }

                        }


                    } else {
                        if (list?.get(position)?.lastMessage?.message=="Unsent"){
                            holder.view.name.text = "Unsent"
                        }else{
                            if (list?.get(position)?.lastMessage?.isMessageHide==true){
                                holder.view.name.text = "Unsent"
                            }else{
                                holder.view.name.text = "Shared a video with you"
                            }

                        }

                    }
                }

                "text" -> {
                    if (list?.get(position)?.lastMessage?.sender == Prefs.init().profileId) {
                        Log.e("sASAsaSAss" , "sender")
                        Log.e("sASAsaSAss" , list?.get(position)?.lastMessage?.message.toString())
                        if (list?.get(position)?.lastMessage?.isMessageHide==true){
                            holder.view.name.text = "Unsent"
                        }else{
                            holder.view.name.text =
                                ("You : " + list?.get(position)?.lastMessage?.message)
                        }

                    } else {
                        if (list?.get(position)?.lastMessage?.isMessageHide==true){
                            holder.view.name.text = "Unsent"
                        }else{
                            holder.view.name.text = list?.get(position)?.lastMessage?.message ?: ""
                        }
                        Log.e("sASAsaSAss" , "sender1")

                    }


                }

                "image" -> {
                    if (list?.get(position)?.lastMessage?.sender == Prefs.init().profileId) {
                        if (list?.get(position)?.lastMessage?.message=="Unsent"){
                            holder.view.name.text = "Unsent"
                        }else{
                            if (list?.get(position)?.lastMessage?.isMessageHide==true){
                                holder.view.name.text = "Unsent"
                            }else{
                                holder.view.name.text = "You : Image"
                            }

                        }

                    } else {
                        if (list?.get(position)?.lastMessage?.message=="Unsent"){
                            holder.view.name.text = "Unsent"
                        }else{
                            if (list?.get(position)?.lastMessage?.isMessageHide==true){
                                holder.view.name.text = "Unsent"
                            }else{
                                holder.view.name.text = "Image"
                            }

                        }

                    }
                }

                "document" -> {
                    if (list?.get(position)?.lastMessage?.sender == Prefs.init().profileId) {
                        if (list?.get(position)?.lastMessage?.isMessageHide==true){
                            holder.view.name.text = "Unsent"
                        }else{
                            holder.view.name.text = "File"
                        }

                    } else {
                        if (list?.get(position)?.lastMessage?.isMessageHide==true){
                            holder.view.name.text = "Unsent"
                        }else{
                            holder.view.name.text = "File"
                        }

                    }
                }
            }

            /*
                        if (!list?.get(position)?.lastMessage?.file.isNullOrEmpty()) {


                        } else if(list?.get(position)?.lastMessage?.post!=null){
                            if (list?.get(position)?.lastMessage?.sender==Prefs.init().profileId){
                                holder.view.name.text = "You : Image"
                            }else{
                                holder.view.name.text = "Image"
                            }
                        }else
                         {
                            holder.view.name.text = list?.get(position)?.lastMessage?.message ?: ""
                        }*/

            /** Handling the swipe functionality here (in which only one time one swipe item is open other is closed )*/
            holder.view.SwipeReveal.setSwipeListener(object : Swipe {
                override fun onClosed(view: SwipeLayout) {
                }

                override fun onOpened(view: SwipeLayout) {
                    // Close the previously open item
                    //    closeOpenItem()
                    if (swipeLayout != null && swipeLayout != holder.view.SwipeReveal) {
                        swipeLayout?.close(true)
                    }
                    swipeLayout = holder.view.SwipeReveal
                }

                override fun onSlide(view: SwipeLayout, slideOffset: Float) {
                    // Handle sliding events if needed

                }

            })
        }

        /** Here we set the Date */
        val dateTimeString = list?.get(position)?.lastMessage?.createdAt
        if (!dateTimeString.isNullOrEmpty()) {
            Utils.convertDateAndTime(dateTimeString)
            holder.view.tvReport.textSize = 10f
            if (daysDiff == 0L && minutes == 0L && hours == 0L) {
                //   holder.view.tvTime.text= minutes.toString() + " minutes ago"
                holder.view.tvReport.text = "A few second ago"
            } else if (minutes < 60L && hours < 1L) {
                holder.view.tvReport.text = "$minutes minutes ago"
            } else if (daysDiff == 0L && hours == 1L) {
                holder.view.tvReport.text = "$hours hour ago"
            } else if (daysDiff == 0L && hours <= 24L) {
                holder.view.tvReport.text = "$hours hours ago"
            } else if (daysDiff == 1L) {
                holder.view.tvReport.text = "$daysDiff day ago"
            } else if (monthsDiff == 1L) {
                holder.view.tvReport.text = "a month ago"
            } else {
                holder.view.tvReport.text = "$daysDiff days ago"
            }
        } else {
            holder.view.tvReport.text = ""
        }
        holder.view.clOpenChats.setOnClickListener {
            holder.view.clOpenChats.disableMultiTap()
            if (list?.isNotEmpty() == true) {
                list?.get(position)?.let { it1 ->
                    onClickListener.onClickChat(
                        position,
                        it1
                    )
                }
            }

        }

        holder.view.rlSwipeBlockButton.setOnClickListener {
            holder.view.rlSwipeBlockButton.disableMultiTap()
            (list?.get(position) ?: null)?.let { it1 ->
                onClickListener.onCLickBlock(
                    position,
                    it1
                )
            }
            swipeLayout?.close(true)
        }
        holder.view.rlSwipeDeleteButton.setOnClickListener {
            holder.view.rlSwipeDeleteButton.disableMultiTap()
            (list?.get(position) ?: null)?.let { it1 ->
                onClickListener.onClickDelete(
                    position,
                    it1
                )
            }
            swipeLayout?.close(true)
        }
        holder.view.rlSwipeClearButton.setOnClickListener {
            holder.view.rlSwipeClearButton.disableMultiTap()
            list?.get(position)?.let { it1 ->
                onClickListener.onClickClear(
                    position,
                    it1
                )
            }
            swipeLayout?.close(true)
        }
        holder.view.rlSwipePinButton.setOnClickListener {
            holder.view.rlSwipePinButton.disableMultiTap()
            list?.get(position)?.let { it1 -> onClickListener.onClickPin(position, it1) }
            swipeLayout?.close(true)
        }
        //  holder.view.tvReport.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.Blue))


    }

    interface OnClickListener {
        fun onClickChat(position: Int, data: MessageListData)
        fun onCLickBlock(position: Int, data: MessageListData)
        fun onClickDelete(position: Int, data: MessageListData)
        fun onClickClear(position: Int, data: MessageListData)
        fun onClickPin(position: Int, data: MessageListData)

    }

    fun setNewItem(newItems: List<MessageListData>) {
        list?.clear()
        list?.addAll(newItems)
        notifyDataSetChanged()
    }
    fun addNewItem(newItems: List<MessageListData>) {
        val oldPos = list?.size?.plus(1)
        list?.addAll(newItems)
        if (oldPos != null) {
            notifyItemRangeInserted(oldPos, newItems.size)
        }
    }
    fun updateItem(newItem: MessageListData, predicate: (MessageListData) -> Boolean) {
        val targetPos = list?.indexOfFirst(predicate)

        if (targetPos != -1) {
            if (targetPos != null) {
                list?.set(targetPos, newItem)
            }
//            notifyItemChanged(targetPos)
        }
    }
}