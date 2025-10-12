package com.wedo8020.app.mainui.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.apachat.swipereveallayout.core.SwipeLayout
import com.apachat.swipereveallayout.core.interfaces.Swipe
import com.wedo8020.app.R
import com.wedo8020.app.databinding.CommentItemLayoutBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.Comment
import com.wedo8020.app.utils.Hashtag
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils

class


CommentsAdapter(
    override val layoutId: Int,
    private val itemClickListener: ClickListener,
    private var swipeLayout: SwipeLayout? = null, private val postCreator: Boolean
) :
    BaseRecyclerAdapter<CommentItemLayoutBinding, Comment>(), Hashtag.onHashtagClick {

        var moreReplyContainer: ConstraintLayout? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun bind(holder: ViewHolder, item: Comment, position: Int) {
        holder.binding.apply {
            commentData = item
            if (item.parentComment.toString().equals("null")){
                replyBtn.visibility=View.VISIBLE
                moreRepliesBtn.visibility=View.VISIBLE
            } else {
                replyBtn.visibility=View.GONE
                moreRepliesBtn.visibility=View.GONE
            }
            (if (item.noOfSubComments == 0) {
                "No Reply"
            } else {
                if (item.noOfSubComments == 1) {
                    "${item.noOfSubComments} Reply"
                } else {
                    "${item.noOfSubComments} Replies"
                }
            }).also { moreRepliesTxt.text = it }
            val isSubComment: Boolean
            if (item.parentComment != null) {
                mainLayout.setBackgroundColor(holder.itemView.context.getColor(R.color.bottomsheet_bg_color))
                clOpenChat.setBackgroundColor(holder.itemView.context.getColor(R.color.bottomsheet_bg_color))
                rlSwipePinButton.visibility = View.GONE
                isSubComment = true
            } else {
                isSubComment = false
                if (postCreator){
                    rlSwipePinButton.visibility = View.VISIBLE
                }else {
                    rlSwipePinButton.visibility = View.GONE
                }
            }
            if (postCreator || item.creator._id == Prefs.init().userId){
                rlSwipeDeleteButton.visibility = View.VISIBLE
            } else {
                rlSwipeDeleteButton.visibility = View.GONE
            }
            if (item.creator._id != Prefs.init().userId){
                rlSwipeReportButton.visibility = View.VISIBLE
            }else{
                rlSwipeReportButton.visibility = View.GONE
            }
            if (item.isReported == true){
                tvReport.text = "Un-\nreport"
            }else{
                tvReport.text = tvReport.context.getString(R.string.report)
            }
            if (item.noOfSubComments > 0 && !item.subComments.isNullOrEmpty()) {
                Log.e("updateItem", item.toString())
            }
            Log.d(
                "recyclerComment",
                " hahahah ${item.creator.username} ${Prefs.init().username} ${item.pinTime}"
            )
            if (item.creator.username == Prefs.init().username) {
                rlSwipeReportButton.visibility = View.GONE
            } else {
                rlSwipeReportButton.visibility = View.VISIBLE
            }
            if (item.pinTime.isNullOrEmpty()) {
                pinnedComment.visibility = View.GONE
                pinComment.text = "Pin"
            } else {
                pinnedComment.visibility = View.VISIBLE
                pinComment.text = "Unpin"
            }
            mainLayout.setOnTouchListener { view, motionEvent ->
                Log.e("q221214214214","yes")
                itemClickListener.hideKeyboards()
                true
            }




            profileImg.setOnClickListener {
                itemClickListener.mentionClick(item.creator._id)
            }
            rlSwipeReportButton.setOnClickListener {
                itemClickListener.onCommentReport(item._id, position,tvReport)
            }
            rlSwipeDeleteButton.setOnClickListener {
//                pinnedComment.visibility = View.VISIBLE
                itemClickListener.onDeleteComment(item._id, position, item.description, item.parentComment?:"" )
                SwipeReveal.close(false)
            }
            rlSwipePinButton.setOnClickListener {
                itemClickListener.onPin(item._id, position, item.description)
                SwipeReveal.close(false)
            }
            SwipeReveal.setSwipeListener(object : Swipe {
                override fun onClosed(view: SwipeLayout?) {

                }

                override fun onOpened(view: SwipeLayout?) {
                    if (swipeLayout != null && swipeLayout != SwipeReveal) {
                        swipeLayout?.close(true)
                    }
                    if (moreReplyContainer!=null && moreReplyContainer?.visibility == View.VISIBLE){
                        moreReplyContainer?.visibility = View.GONE
                    }
                    swipeLayout = SwipeReveal
                }

                override fun onSlide(view: SwipeLayout?, slideOffset: Float) {

                }
            })

            if (list[position].isLike) {
                likeBtn.setImageResource(R.drawable.ic_like)
                likeBtn.imageTintList = null
            } else {
                likeBtn.setImageResource(R.drawable.ic_unlike)
                likeBtn.imageTintList =
                    ColorStateList.valueOf(likeBtn.context.getColor(R.color.Grey))

            }
            likeBtn.setOnClickListener {
                if (list[position].isLike) {
                    likeBtn.setImageResource(R.drawable.ic_unlike)
                    likeBtn.imageTintList =
                        ColorStateList.valueOf(likeBtn.context.getColor(R.color.Grey))
                    list[position].noOfLikes = list[position].noOfLikes - 1
                    totalLike.text = list[position].noOfLikes.toString()
                } else {
                    likeBtn.setImageResource(R.drawable.ic_like)
                    likeBtn.imageTintList = null
                    list[position].noOfLikes = list[position].noOfLikes + 1
                    totalLike.text = list[position].noOfLikes.toString()

                }
                itemClickListener.likeClick(position, item._id)
                list[position].isLike = !list[position].isLike
            }
            replyBtn.setOnClickListener {
                itemClickListener.replyClick(item._id, position, this)
            }
//            dateCreated.text = Utils.convertDateAndTime(item.createdAt)
            moreRepliesContainer.visibility = View.GONE
            val commentsAdapter =
                CommentsAdapter(R.layout.comment_item_layout, itemClickListener, swipeLayout,postCreator)
            moreRepliesRecyclerView.adapter = commentsAdapter
            moreRepliesBtn.setOnClickListener {
                Log.e("moreRepliesBtn", item.toString())
                moreReplyContainer = moreRepliesContainer
                swipeLayout?.close(true)
                if (item.noOfSubComments > 0) {
                    if (moreRepliesContainer.visibility == View.GONE) {
                        itemClickListener.showMoreReply(item._id, position, this)
                    } else {
                        moreRepliesContainer.visibility = View.GONE
                    }
                } else {
                    if (!item.subComments.isNullOrEmpty()) {
                        if (item.subComments!!.size > 0 && moreRepliesContainer.visibility == View.GONE) {
                            itemClickListener.showMoreReply(item._id, position, this)
                        } else {
                            moreRepliesContainer.visibility = View.GONE
                        }
                    }
                }
            }
            /** Here we set the Date */
            val dateTimeString = item.createdAt
            if (!dateTimeString.isNullOrEmpty()) {
                Utils.convertDateAndTime(dateTimeString)
                if (Utils.daysDiff == 0L && Utils.minutes == 0L && Utils.hours == 0L) {
                    //   holder.view.tvTime.text= minutes.toString() + " minutes ago"
                    dateCreated.text = "a few second ago"
                } else if (Utils.minutes < 60L && Utils.hours < 1L) {
                    dateCreated.text = "${Utils.minutes} minutes ago"
                } else if (Utils.daysDiff == 0L && Utils.hours == 1L) {
                    dateCreated.text = "${Utils.hours} hour ago"
                } else if (Utils.daysDiff == 0L && Utils.hours <= 24L) {
                    dateCreated.text = "${Utils.hours} hours ago"
                } else if (Utils.daysDiff == 1L) {
                    dateCreated.text = "${Utils.daysDiff} day ago"
                } else if (Utils.monthsDiff == 1L) {
                    dateCreated.text = "a month ago"
                } else {
                    dateCreated.text = "${Utils.daysDiff} days ago"
                }
            } else {
                dateCreated.text = ""
            }
            totalLike.text = item.noOfLikes.toString()
            if (item.description.isNotEmpty()) {
                val usernames = Utils.getUsernames(item.description)
                val description = Utils.convertDesc(item.description)
                Log.e("usernameslist", usernames.toString())
                val ss = SpannableString(description)
                val hashtagSpans1: ArrayList<IntArray> = Utils.getSpans(description, '#')
                val calloutSpans1: ArrayList<IntArray> = Utils.getSpans(description, '@')
                Utils.setSpanComment(ss, hashtagSpans1, this@CommentsAdapter, item.hashtags?:ArrayList())
                Log.e("mentionClickcomment", item.toString())

                Utils.setSpanUname(ss, calloutSpans1, this@CommentsAdapter, item.mentionUsers)
                commentTxt.movementMethod = LinkMovementMethod.getInstance()
                commentTxt.text = ss
            }

        }
    }


    interface ClickListener {
        fun replyClick(commentId: String, position: Int, binding: CommentItemLayoutBinding)
        fun showMoreReply(commentId: String, position: Int, binding: CommentItemLayoutBinding)
        fun likeClick(position: Int, id: String)
        fun hasTagClick(text: String)
        fun mentionClick(user: String)

        fun onCommentReport(commentId: String,position:Int, view: TextView)
        fun onDeleteComment(commentId: String, position: Int, comment: String, parentComment: String)

        fun onPin(commentId: String, position: Int, comment: String)

        fun hideKeyboards()
    }

    override fun hashTagClick(text: String) {
        itemClickListener.hasTagClick(text)
    }

    override fun mentionClick(text: String) {
        Log.e("mentionClickcomment", text)
        itemClickListener.mentionClick(text)
    }
}