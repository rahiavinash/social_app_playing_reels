package com.wedo8020.app.mainui.home.chatAndNotification.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.apachat.swipereveallayout.core.SwipeLayout
import com.apachat.swipereveallayout.core.interfaces.Swipe
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.databinding.DateLayoutBinding
import com.wedo8020.app.databinding.ImageReceiverLayoutBinding
import com.wedo8020.app.databinding.ImageSenderLayoutBinding
import com.wedo8020.app.databinding.ReceiveMessageLayoutBinding
import com.wedo8020.app.databinding.SendMessageLayoutBinding
import com.wedo8020.app.enumClasses.ChatType
import com.wedo8020.app.response.UserMessageData
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.loadImageMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MessageAdapter(
    override var layoutId: HashMap<ChatType, Int>, onClickListener: OnClickListener
) : BaseChatRecyclerAdapter<UserMessageData>() {
    var onClickListener: OnClickListener = onClickListener
    var otherUserId = ""
    var isMessageOn = false
    var textView: TextView? = null

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun bind(holder: ViewHolder, item: UserMessageData, position: Int) {

        when (holder.itemViewType) {
            ChatType.MESSAGE_SENDER.ordinal -> {
                (holder.binding as SendMessageLayoutBinding).apply {
                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        optionButton,
                        item,
                        "main"
                    )
                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        tvUserSendMessage,
                        item,
                        "senderMessage"
                    )
                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        clReply,
                        item,
                        "senderMessageReply"
                    )



                    SwipeReveal.setSwipeListener(object : Swipe {
                        override fun onClosed(view: SwipeLayout?) {
                        }
                        override fun onOpened(view: SwipeLayout?) {
                            if (item.message == "Unsent" ) {
                                SwipeReveal.close(true)
                            }else if (!isMessageOn) {
                                SwipeReveal.close(true)
                            }else if(item.isMessageHide) {
                                SwipeReveal.close(true)
                            }else

                             {
                                onClickListener.swipeReply(position, item, "senderMessage")
                                Handler(Looper.getMainLooper()).postDelayed({
                                    SwipeReveal.close(true)
                                }, 300)
                            }
                        }

                        override fun onSlide(view: SwipeLayout?, slideOffset: Float) {
                        }
                    })

                    /** set the Emoji reaction here */
                    if (item.reactions?.isNotEmpty() == true) {
                        reactionLayout.visibility = View.VISIBLE
                        val finalEmoji: String
                        if (item.reactions!!.size > 1) {

                            val emojis1 = item.reactions!![0].react
                            emoji1.text = getEmoji(emojis1)
                            if (item.reactions!!.size > 1) {
                                val emojis2 = item.reactions!![1].react
                                emoji2.visibility = View.VISIBLE
                                emoji2.text = getEmoji(emojis2)
                            } else {
                                emoji2.visibility = View.GONE
                            }
                        } else {
                            val emojis = item.reactions!![0].react
                            val unicodeInt = emojis.codePointAt(0)
                            finalEmoji = getEmojiByUnicode(unicodeInt)
                            emoji1.text = finalEmoji
                        }

                    } else {
                        reactionLayout.visibility = View.GONE
                    }


                    if (item.message == "Unsent") {
                        tvUserSendMessage.visibility = View.INVISIBLE
                        clSentMessageDate.visibility = View.GONE
                        tvDate.visibility = View.GONE
                        tvSeen.visibility = View.GONE
                        reactionLayout.visibility = View.GONE
                        emoji1.visibility = View.GONE
                        emoji2.visibility = View.GONE
                        tvUnsent.visibility = View.VISIBLE

                    } else {
                        if (item.isMessageHide){
                            tvUserSendMessage.visibility = View.INVISIBLE
                            clSentMessageDate.visibility = View.GONE
                            tvDate.visibility = View.GONE
                            tvSeen.visibility = View.GONE
                            reactionLayout.visibility = View.GONE
                            emoji1.visibility = View.GONE
                            emoji2.visibility = View.GONE
                            tvUnsent.visibility = View.VISIBLE
                        }else{
                            if (item.parentMessage != null) {

                                clReply.visibility = View.VISIBLE
                                tvUserSendMessage.visibility = View.GONE
                                when (item.parentMessage.type) {
                                    "text" -> {
                                        tvSendReplyMainMessage.text = item.parentMessage.message
                                        tvSendReplyMainMessage.visibility = View.VISIBLE
                                        ivReplyPostImage.visibility = View.GONE
                                        ivReplyImage.visibility = View.GONE
                                    }

                                    "sharePost" -> {
                                        if (item.parentMessage.post?.type=="image"){
                                            if (!item.parentMessage.post.images.isNullOrEmpty()){
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    item.parentMessage.post.images?.get(0)?.let {
                                                        ivReplyImage.loadImageMessage(
                                                            it
                                                        )
                                                    }
                                                }
                                            }

                                        }else{
                                            CoroutineScope(Dispatchers.Main).launch {
                                                item.parentMessage.post?.videoThumbnail?.let {
                                                    ivReplyImage.loadImageMessage(
                                                        it
                                                    )
                                                }
                                            }
                                        }

                                        ivReplyPostImage.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                holder.itemView.context,
                                                R.drawable.video_icon
                                            )
                                        )
                                        ivReplyPostImage.visibility = View.VISIBLE
                                        ivReplyImage.visibility = View.VISIBLE
                                        tvSendReplyMainMessage.visibility = View.GONE
                                    }

                                    "image" -> {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            item.parentMessage.file?.let {
                                                ivReplyImage.loadImageMessage(
                                                    it
                                                )
                                            }
                                        }
                                        ivReplyPostImage.visibility = View.VISIBLE
                                        ivReplyImage.visibility = View.VISIBLE
                                        tvSendReplyMainMessage.visibility = View.GONE
                                    }


                                    "video" -> {
                                        // ivReplyPostImage.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, R.drawable.video_icon))
                                        CoroutineScope(Dispatchers.Main).launch {
                                            item.parentMessage.file?.let {
                                                ivReplyImage.loadImageMessage(
                                                    it
                                                )
                                            }
                                        }
                                        ivReplyPostImage.visibility = View.VISIBLE
                                        ivReplyImage.visibility = View.VISIBLE
                                        tvSendReplyMainMessage.visibility = View.GONE
                                    }
                                }


                                /* if (!item.parentMessage.file.isNullOrEmpty()) {
                                     CoroutineScope(Dispatchers.Main).launch {
                                         ivReplyPostImage.loadImageAsync(item.parentMessage.file)
                                     }
                                     tvSendReplyMainMessage.visibility = View.GONE
                                 } else {
                                     tvSendReplyMainMessage.text = item.parentMessage.message
                                     ivReplyPostImage.visibility = View.GONE
                                 }*/
                                tvSendReply.text = item.message

                            } else {
                                clReply.visibility = View.GONE
                                tvUserSendMessage.visibility = View.VISIBLE
                                tvUnsent.visibility = View.GONE
                                tvUserSendMessage.text = item.message
                            }
                        }


                    }

                    Utils.convertDateAndTime(item.createdAt)
                    tvDate.text = Utils.time
                    if (!item.seenTime.isNullOrEmpty()) {
                        tvSent.text = "Read"
                        tvSentDate.visibility = View.VISIBLE
                        tvSentTime.visibility = View.VISIBLE
                        Utils.convertDateAndTime(item.seenTime)
                        tvSentDate.text = Utils.messageDateMonth
                        tvSentTime.text = Utils.time
                    } else {
                        tvSentDate.visibility = View.GONE
                        tvSentTime.visibility = View.GONE
                        tvSent.text = "Sent"
                    }

                    /**To Handle the OnPressClickListener For Reaction , Copy ,Unused And Reply.*/
                    /*  tvUserSendMessage.setOnLongClickListener {
                          onClickListener.onLongPressOnSenderMessage(position, item, Utils.time)
                          true
                      }
                      clReply.setOnLongClickListener {
                          onClickListener.onLongPressOnSenderMessage(position, item, Utils.time)
                          true
                      }*/

                    /**Set here the last seen by otherUser*/
                    if (item.receiver == otherUserId && item.seenStatus == "seen" && item.readReceipt && item.showSeen) {
                        tvSeen.visibility = View.VISIBLE
                    } else {
                        tvSeen.visibility = View.GONE

                    }


                }


            }

            ChatType.MESSAGE_RECEIVER.ordinal -> {
                (holder.binding as ReceiveMessageLayoutBinding).apply {
                    SwipeReveal.setSwipeListener(object : Swipe {
                        override fun onClosed(view: SwipeLayout?) {
                        }
                        override fun onOpened(view: SwipeLayout?) {
                            if (!isMessageOn) {
                                SwipeReveal.close(true)
                            }else if(item.isMessageHide) {
                                SwipeReveal.close(true)
                            }else{
                                onClickListener.swipeReply(position, item, "receiverMessage")
                                Handler(Looper.getMainLooper()).postDelayed({
                                    SwipeReveal.close(true)
                                }, 300)
                            }

                        }
                        override fun onSlide(view: SwipeLayout?, slideOffset: Float) {
                        }

                    })

                    if (item.message == "Deleted" || item.message == "Unsent"  ) {
                        SwipeReveal.visibility = View.GONE
                    }
                    if (item.parentMessage != null) {
                        /*  clReply.visibility = View.VISIBLE
                          tvUserMessage.visibility = View.GONE
                          if (!item.parentMessage.file.isNullOrEmpty()) {
                              CoroutineScope(Dispatchers.Main).launch {
                                  ivReplyPostImage.loadImageAsync(item.parentMessage.file)
                              }
                              tvSendReplyMainMessage.visibility = View.GONE
                          } else {
                              tvSendReplyMainMessage.text = item.parentMessage.message
                              ivReplyPostImage.visibility = View.GONE
                          }*/

                        if (item.isMessageHide){
                            SwipeReveal.visibility = View.GONE
                            tvUserMessage.visibility = View.GONE
                        }else{
                            clReply.visibility = View.VISIBLE
                            tvUserMessage.visibility = View.GONE
                            when (item.parentMessage.type) {


                                "text" -> {
                                    tvSendReplyMainMessage.text = item.parentMessage.message
                                    tvSendReplyMainMessage.visibility = View.VISIBLE
                                    ivReplyPostImage.visibility = View.GONE
                                    ivReplyImage.visibility = View.GONE
                                }

                                /*"document" -> {
                                    tvSendReplyMainMessage.setText(R.string.download_data_message)
                                    llDocumentImage.visibility = View.VISIBLE
                                    ivReplyPostImage.visibility = View.GONE
                                    ivReplyImage.visibility = View.GONE
                                }*/

                                "sharePost" -> {
                                    if (item.parentMessage.post?.type=="image"){
                                        if (!item.parentMessage.post.images.isNullOrEmpty()){
                                            CoroutineScope(Dispatchers.Main).launch {
                                                item.parentMessage.post.images?.get(0)?.let {
                                                    ivReplyImage.loadImageMessage(
                                                        it
                                                    )
                                                }
                                            }
                                        }

                                    }else {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            item.parentMessage.post?.videoThumbnail?.let {
                                                ivReplyImage.loadImageMessage(
                                                    it
                                                )
                                            }
                                        }
                                    }
                                    ivReplyPostImage.visibility = View.VISIBLE
                                    ivReplyImage.visibility = View.VISIBLE
                                    ivReplyPostImage.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            holder.itemView.context,
                                            R.drawable.video_icon
                                        )
                                    )
                                    tvSendReplyMainMessage.visibility = View.GONE
                                }

                                "image" -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        item.parentMessage.file?.let {
                                            ivReplyImage.loadImageMessage(
                                                it
                                            )
                                        }
                                    }
                                    ivReplyPostImage.visibility = View.VISIBLE
                                    ivReplyImage.visibility = View.VISIBLE
                                    tvSendReplyMainMessage.visibility = View.GONE
                                }


                                "video" -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        item.parentMessage.file?.let {
                                            ivReplyPostImage.loadImageMessage(
                                                it
                                            )
                                        }
                                    }
                                    ivReplyPostImage.visibility = View.VISIBLE
                                    ivReplyImage.visibility = View.VISIBLE
                                    //  ivReplyPostImage.setImageDrawable(ContextCompat.getDrawable(holder.itemView.context, R.drawable.video_icon))
                                    tvSendReplyMainMessage.visibility = View.GONE
                                }
                            }
                            tvSendReply.text = item.message
                        }



                    } else {
                        if (item.isMessageHide){
                            SwipeReveal.visibility = View.GONE
                            tvUserMessage.visibility = View.GONE
                        }else {

                            clReply.visibility = View.GONE
                            tvUserMessage.visibility = View.VISIBLE
                            tvUserMessage.text = item.message
                        }
                    }

                    if (list[position].type == "document") {
                        SwipeReveal.close(true)
                        tvUserMessage.setText(R.string.download_data_message)
                        llDocumentImage.visibility = View.VISIBLE
                        ivReplyPostImage.visibility = View.GONE
                        ivReplyImage.visibility = View.GONE
                    } else {
                        tvUserMessage.text = item.message
                    }
                    Utils.convertDateAndTime(item.createdAt)
                    tvDates.text = Utils.time

                    /**To Handle the OnPressClickListener For Reaction , Copy ,Unused And Reply.*/
                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        main,
                        item,
                        "main"
                    )

                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        tvUserMessage,
                        item,
                        "receiverMessage"
                    )
                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        clReply,
                        item,
                        "receiverMessageReply"
                    )

                    /** set the Emoji reaction here */
                    if (item.reactions?.isNotEmpty() == true) {
                        reactionLayout.visibility = View.VISIBLE
                        val finalEmoji: String
                        if (item.reactions!!.size > 1) {
                            val emojis1 = item.reactions!![0].react
                            emoji1.text = getEmoji(emojis1)
                            if (item.reactions!!.size > 1) {
                                val emojis2 = item.reactions!![1].react
                                emoji2.visibility = View.VISIBLE
                                emoji2.text = getEmoji(emojis2)
                            } else {
                                emoji2.visibility = View.GONE
                            }
                        } else {
                            val emojis = item.reactions!![0].react
                            val unicodeInt = emojis.codePointAt(0)
                            finalEmoji = getEmojiByUnicode(unicodeInt)
                            emoji1.text = finalEmoji
                        }

                    } else {
                        reactionLayout.visibility = View.GONE
                    }

                    /*  clReply.setOnLongClickListener {
                          onClickListener.onLongPressOnReceiverMessage(position, item, Utils.time)
                          true
                      }*/

                    llDocumentImage.setOnClickListener {
                        SwipeReveal.close(true)
                        onClickListener.onDownloadDecryptFile(position , list[position])
                    }
                }


            }

            ChatType.IMAGE_SENDER.ordinal -> {
                (holder.binding as ImageSenderLayoutBinding).apply {
                    SwipeReveal.setSwipeListener(object : Swipe {
                        override fun onClosed(view: SwipeLayout?) {
                        }

                        override fun onOpened(view: SwipeLayout?) {
                            if (item.message == "Unsent") {
                                SwipeReveal.close(true)
                            }else if (!isMessageOn) {
                                SwipeReveal.close(true)
                            } else {
                                onClickListener.swipeReply(position, item, "senderImage")
                                Handler(Looper.getMainLooper()).postDelayed({
                                    SwipeReveal.close(true)
                                }, 300)
                            }
                        }

                        override fun onSlide(view: SwipeLayout?, slideOffset: Float) {
                        }

                    })
                    /** set the Emoji reaction here */
                    if (item.reactions?.isNotEmpty() == true) {
                        reactionLayout.visibility = View.VISIBLE
                        val finalEmoji: String
                        if (item.reactions!!.size > 1) {

                            val emojis1 = item.reactions!![0].react
                            emoji1.text = getEmoji(emojis1)
                            if (item.reactions!!.size > 1) {
                                val emojis2 = item.reactions!![1].react
                                emoji2.visibility = View.VISIBLE
                                emoji2.text = getEmoji(emojis2)
                            } else {
                                emoji2.visibility = View.GONE
                            }
                        } else {
                            val emojis = item.reactions!![0].react
                            val unicodeInt = emojis.codePointAt(0)
                            finalEmoji = getEmojiByUnicode(unicodeInt)
                            emoji1.text = finalEmoji
                        }

                    } else {
                        reactionLayout.visibility = View.GONE
                    }

                    /**  Handle here Unsent Message functionality   */
                    if (item.message == "Unsent") {
                        llImage.visibility = View.GONE
                        tvDate.visibility = View.GONE
                        tvSeen.visibility = View.GONE
                        clImage.visibility = View.GONE
                        tvUnsent.visibility = View.VISIBLE
                        reactionLayout.visibility = View.GONE
                    } else {
                        if (item.isMessageHide){
                            llImage.visibility = View.GONE
                            clImage.visibility = View.GONE
                            tvDate.visibility = View.GONE
                            tvUnsent.visibility = View.VISIBLE
                           // tvUserMessage.visibility = View.GONE
                        }else{
                            if (item.post != null) {
                                if (item.post.type=="image"){
                                    if(!item.post.images.isNullOrEmpty()){
                                        CoroutineScope(Dispatchers.Main).launch {
                                            senderImage.loadImageMessage(item.post.images[0])
                                        }
                                    }else{
                                        Glide.with(holder.itemView).load(R.drawable.post_not_found).into(postNotFound)
                                        llImage.visibility=View.INVISIBLE
                                    }

                                }else{
                                    CoroutineScope(Dispatchers.Main).launch {
                                        senderImage.loadImageMessage(item.post.videoThumbnail)
                                    }
                                }

                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    senderImage.loadImageMessage(item.file)
                                }
                            }
                        }



                    }

                    Utils.convertDateAndTime(item.createdAt)
                    tvDate.text = Utils.time
                    if (!item.seenTime.isNullOrEmpty()) {
                        tvSent.text = "Read"
                        tvSentDate.visibility = View.VISIBLE
                        tvSentTime.visibility = View.VISIBLE
                        Utils.convertDateAndTime(item.seenTime)
                        tvSentDate.text = Utils.messageDateMonth
                        tvSentTime.text = Utils.time
                    } else {
                        tvSent.text = "Sent"
                    }
                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        optionButtons,
                        item,
                        "main"
                    )
                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        clSenderImage,
                        item,
                        "imageSender"
                    )
                    /* messageLayout.setOnClickListener {
                         onClickListener.onClickImage(position, item, item.type)
                         true
                     }*/
                    /**To Handle the OnPressClickListener For Reaction , Copy ,Unused And Reply.*/
                    /* messageLayout.setOnLongClickListener {
                         onClickListener.onLongPressOnSenderImage(position, item, Utils.time)
                         true
                     }*/


                }
            }

            ChatType.IMAGE_RECEIVER.ordinal -> {
                (holder.binding as ImageReceiverLayoutBinding).apply {


                    SwipeReveal.setSwipeListener(object : Swipe {
                        override fun onClosed(view: SwipeLayout?) {
                        }

                        override fun onOpened(view: SwipeLayout?) {
                            if (!isMessageOn) {
                                SwipeReveal.close(true)
                            }else{
                                onClickListener.swipeReply(position, item, "receiverImage")
                                Handler(Looper.getMainLooper()).postDelayed({
                                    SwipeReveal.close(true)
                                }, 300)
                            }
                        }
                        override fun onSlide(view: SwipeLayout?, slideOffset: Float) {
                        }

                    })


                    if (item.message == "Deleted" || item.message == "Unsent") {
                        SwipeReveal.visibility = View.GONE
                    }

                    if (item.isMessageHide){
                        SwipeReveal.visibility = View.GONE
                        // tvUserMessage.visibility = View.GONE
                    }else{
                        if (item.post != null) {
                            if (item.post.type=="image"){
                                if(!item.post.images.isNullOrEmpty()){
                                    CoroutineScope(Dispatchers.Main).launch {
                                        receiveOtherImage.loadImageMessage(item.post.images[0])
                                    }
                                }else{
                                    llImage.visibility=View.INVISIBLE
                                        Glide.with(holder.itemView).load(R.drawable.post_not_found).into(postNotFound)
                                }
                            }else {

                                Glide.with(holder.itemView.context).load(item.post.videoThumbnail).error(R.drawable.post_not_found)
                                    .into(receiveOtherImage)
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                receiveOtherImage.loadImageMessage(item.file)
                            }
                        }
                        Utils.convertDateAndTime(item.createdAt)
                        tvDate.text = Utils.time
                    }

                    /**To Handle the OnPressClickListener For Reaction , Copy ,Unused And Reply.*/


                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        clUserDp,
                        item,
                        "receiverImage"
                    )
                    /* clUserDp.setOnClickListener {
                         onClickListener.onClickImage(position, item, item.type)
                         true
                     }
 */


                    /* clUserDp.setOnLongClickListener {
                         onClickListener.onLongPressOnReceiverImage(position, item, Utils.time)
                         true
                     }*/

                    /** set the Emoji reaction here */
                    if (item.reactions?.isNotEmpty() == true) {
                        reactionLayout.visibility = View.VISIBLE
                        val finalEmoji: String
                        if (item.reactions!!.size > 1) {

                            val emojis1 = item.reactions!![0].react
                            emoji1.text = getEmoji(emojis1)
                            if (item.reactions!!.size > 1) {
                                val emojis2 = item.reactions!![1].react
                                emoji2.visibility = View.VISIBLE
                                emoji2.text = getEmoji(emojis2)
                            } else {
                                emoji2.visibility = View.GONE
                            }
                        } else {
                            val emojis = item.reactions!![0].react
                            val unicodeInt = emojis.codePointAt(0)
                            finalEmoji = getEmojiByUnicode(unicodeInt)
                            emoji1.text = finalEmoji
                        }

                    } else {
                        reactionLayout.visibility = View.GONE
                    }

                }
            }

            ChatType.DATE.ordinal -> {
                (holder.binding as DateLayoutBinding).apply {
                    tvMessageDate.text = item.createdAt

                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        dateLayout,
                        item,
                        "date"
                    )


                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var type = ChatType.MESSAGE_SENDER
        type = if (list[position].type == "text") {
            if (list[position].sender == Prefs.init().userId) {
                ChatType.MESSAGE_SENDER
            } else {
                ChatType.MESSAGE_RECEIVER
            }
        } else if (list[position].type == "date") {
            ChatType.DATE
        } else if (list[position].type == "document") {
            if (list[position].sender == Prefs.init().userId) {
                ChatType.MESSAGE_SENDER
            } else {
                ChatType.MESSAGE_RECEIVER
            }
        } else {
            if (list[position].sender == Prefs.init().userId) {
                ChatType.IMAGE_SENDER
            } else {
                ChatType.IMAGE_RECEIVER
            }
        }



        return type.ordinal
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return list[position]._id.toLong()
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun getEmoji(unicode: String): String {
        val unicodeInt = unicode.codePointAt(0)
        return getEmojiByUnicode(unicodeInt)
    }

    interface OnClickListener {
        fun onLongPressOnSenderMessage(
            view: View,
            position: Int,
            data: UserMessageData,
            time: String
        )

        fun onLongPressOnReceiverMessage(position: Int, data: UserMessageData, time: String)
        fun onLongPressOnSenderImage(position: Int, data: UserMessageData, time: String)
        fun onLongPressOnReceiverImage(position: Int, data: UserMessageData, time: String)
        fun swipeReply(position: Int, data: UserMessageData, type: String)
        fun onClickImage(position: Int, data: UserMessageData, type: String)
        fun onClickHideKeyboard(position: Int)

        fun onDoubleTapClick(position: Int, data: UserMessageData)
        fun onDownloadDecryptFile(position: Int, data: UserMessageData)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @SuppressLint("ClickableViewAccessibility")
    private fun setPlayerTouchListener(
        position: Int,
        context: Context,
        view: View,
        data: UserMessageData,
        type: String

    ) {
        val gestureDetectorCompat =
            GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    onClickListener.onDoubleTapClick(position, data)
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (type == "imageSender") {
                        if (data.message == "Unsent" || data.message == "Deleted" ) {

                        } else {
                            onClickListener.onClickImage(position, data, data.type)
                        }
                        //      onClickListener.onClickImage(position, data, data.type)
                    } else if (type == "receiverImage") {
                        onClickListener.onClickImage(position, data, data.type)
                    }




                    /*  playerView.player?.playWhenReady = !playerView.player!!.playWhenReady*/
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    when (type) {
                        "receiverMessage" -> {
                            onClickListener.onLongPressOnReceiverMessage(position, data, Utils.time)
                        }

                        "receiverMessageReply" -> {
                            onClickListener.onLongPressOnReceiverMessage(position, data, Utils.time)
                        }

                        "senderMessageReply" -> {
                            onClickListener.onLongPressOnSenderMessage(view,position, data, Utils.time)
                        }

                        "senderMessage" -> {
                            onClickListener.onLongPressOnSenderMessage(view, position, data, Utils.time)
                        }

                        "imageSender" -> {
                            onClickListener.onLongPressOnSenderImage(position, data, Utils.time)
                            //      onClickListener.onClickImage(position, data, data.type)
                        }

                        "receiverImage" -> {
                            onClickListener.onLongPressOnReceiverImage(position, data, Utils.time)
                        }
                    }
                }

            })
        /*  tvUserSendMessage.setOnLongClickListener {
                              onClickListener.onLongPressOnSenderMessage(position, item, Utils.time)
                              true
                          }
                          clReply.setOnLongClickListener {
                              onClickListener.onLongPressOnSenderMessage(position, item, Utils.time)
                              true
                          }*/

        view.setOnTouchListener { _, p1 ->
            gestureDetectorCompat.onTouchEvent(p1)
            onClickListener.onClickHideKeyboard(position)
            true
        }
    }
}



