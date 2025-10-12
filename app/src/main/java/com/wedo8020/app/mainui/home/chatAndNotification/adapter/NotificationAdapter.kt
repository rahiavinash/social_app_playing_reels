package com.wedo8020.app.mainui.home.chatAndNotification.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
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
import com.wedo8020.app.databinding.NotificationlistRecycleviewBinding
import com.wedo8020.app.response.NotificationListData
import com.wedo8020.app.utils.Hashtag
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.isInternetAvailables
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import com.wedo8020.app.utils.Utils.loadImageMessage
import com.wedo8020.app.utils.Utils.runWithDelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class NotificationAdapter(
    private val recyclerView: RecyclerView,
    var list: ArrayList<NotificationListData>,
    onClickListener: OnClickListener

) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>(), Hashtag.onHashtagClick {
    var onClickListener: OnClickListener = onClickListener
    private var swipeLayout: SwipeLayout? = null
    private var selectedItemPosition: Int = -1


    class ViewHolder(val view: NotificationlistRecycleviewBinding) :
        RecyclerView.ViewHolder(view.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding =
            NotificationlistRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ResourceType", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.view.ivRestrictImage.visibility = View.GONE

        list[position].let { it1 ->
            when (list[position].type) {
                "dailyAr" -> {
                    val ar = list[position].ar
                    val boldText = "<b>$ar AR </b>"
                    val formattedText = "$boldText has been added to your wallet"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE
                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }

                "followRequest" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml("<b>${it1.sender[0].name}</b>", Html.FROM_HTML_MODE_LEGACY)

                    holder.view.tvRequestStatus.text = "Requested to follow you."
                    holder.view.ivCoin.visibility = View.INVISIBLE
                    holder.view.tvAccept.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }
                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }
                    /*holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickAccept(position, list[position])
                    }*/
                    holder.view.tvAccept.setOnClickListener {
                        if (isInternetAvailables(holder.itemView.context)) {
                            onClickListener.onClickAccept(position, list[position])
                            holder.view.clCoin.visibility = View.GONE
                            holder.view.tvRequestStatus.text = "Started to following you."
                        } else {
                            Utils.showToast("Please check your internet connection.")
                        }

                    }
                }

                "followUser" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml("<b>${it1.sender[0].name}</b>", Html.FROM_HTML_MODE_LEGACY)
                    holder.view.clCoin.visibility = View.GONE
                    holder.view.tvRequestStatus.text = "Started to following you."
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }

                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }
                }

                "postLike" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }
                    var otherCount = it1.sender.size
                    if (otherCount > 1) {
                        otherCount -= 1
                        if (otherCount == 1) {
                            holder.view.tvArAddedAmount.text =
                                Html.fromHtml(
                                    "<b>${it1.sender[0].name} </b> and <b>${otherCount}</b> other ",
                                    Html.FROM_HTML_MODE_LEGACY
                                )
                        } else {
                            holder.view.tvArAddedAmount.text =
                                Html.fromHtml(
                                    "<b>${it1.sender[0].name} </b> and <b>${otherCount}  </b> others ",
                                    Html.FROM_HTML_MODE_LEGACY
                                )
                        }
                    } else {
                        holder.view.tvArAddedAmount.text =
                            Html.fromHtml(
                                "<b>${it1.sender[0].name} </b>",
                                Html.FROM_HTML_MODE_LEGACY
                            )
                    }

                    holder.view.tvRequestStatus.text = "liked your Drop."
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }
                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageMessage(it1.post.videoThumbnail)
                        }
                    }else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }

                    }

                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }
                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }

                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }

                "comment" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }

                    val description =
                        it1.comment?.description?.let { it2 -> Utils.convertDesc(it2).trim() }
                    //val name = it.sender[0].name

                    /* it.comment?.mentionUsers?.forEach {it->
                         if (!mentionUserHash.containsKey("@" + it.username)) {
                             mentionUserHash["@" + it.username] = it._id
                         }
                     }
                     it.comment?.hashtags?.forEach {it->
                         if (!hashtagUserHash.containsKey("#" + it.tag)) {
                             hashtagUserHash["#" + it.tag] = it._id
                         }
                     }

                     val ss = SpannableString(description)

                     val hashtagSpans1: ArrayList<IntArray> = Utils.getSpans(description, '#')
                     val callOutSpans1: ArrayList<IntArray> = Utils.getSpans(description, '@')
                     Utils.setSpanComment(ss,
                         hashtagSpans1,this ,
                         it.comment?.hashtags?.let { it1 -> ArrayList(it1) } ?: ArrayList()
                     )
                     Utils.setSpanUname(
                         ss,
                         callOutSpans1,
                         this,
                         it.comment?.mentionUsers
                     )
                      holder.view.tvArAddedAmount.movementMethod = LinkMovementMethod.getInstance()
                     */

                    var otherCount = it1.sender.size
                    if (otherCount > 1) {
                        otherCount -= 1
                        if (otherCount == 1) {
                            holder.view.tvArAddedAmount.text =
                                Html.fromHtml(
                                    "<b>${it1.sender[0].name} </b> and <b>${otherCount}</b> other commented, ",
                                    Html.FROM_HTML_MODE_LEGACY
                                )
                        } else {
                            holder.view.tvArAddedAmount.text =
                                Html.fromHtml(
                                    "<b>${it1.sender[0].name} </b> and <b>${otherCount}  </b> others commented, ",
                                    Html.FROM_HTML_MODE_LEGACY
                                )
                        }
                    } else {
                        holder.view.tvArAddedAmount.text =
                            Html.fromHtml(
                                "<b>${it1.sender[0].name} </b> commented,",
                                Html.FROM_HTML_MODE_LEGACY
                            )
                    }

                    /*       holder.view.tvArAddedAmount.text = Html.fromHtml(
                               "<b>$name</b> commented, ",
                               Html.FROM_HTML_MODE_LEGACY
                           )*/

                    holder.view.tvRequestStatus.text = Html.fromHtml(
                        "<b>\" $description \"</b>",
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageMessage(it1.sender[0].profile.toString())
                    }
                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }
                    }
                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }
                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }

                "commentReply" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }
                    holder.view.tvArAddedAmount.text = it1.sender[0].name
                    holder.view.ivCoin.visibility = View.INVISIBLE
                    holder.view.tvRequestStatus.text = " reply to your comment."
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }
                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }
                    }

                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }

                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }

                "newPost" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml("<b>${it1.sender[0].name}</b>", Html.FROM_HTML_MODE_LEGACY)
                    holder.view.ivCoin.visibility = View.INVISIBLE
                    holder.view.tvRequestStatus.text = " just Dropped a post."
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }
                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }
                    }
                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }

                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }

                }

                "tagPost" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml("<b>${it1.sender[0].name}</b>", Html.FROM_HTML_MODE_LEGACY)
                    holder.view.tvRequestStatus.text = " tagged you in a Drop."
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }

                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }
                    }
                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }
                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }

                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }

                "mentionPost" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    val name = it1.sender[0].name
                    holder.view.tvArAddedAmount.text = Html.fromHtml(
                        "<b>$name</b> mentioned you in a Drop.",
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    holder.view.ivCoin.visibility = View.INVISIBLE
                  //  holder.view.tvRequestStatus.visibility = View.GONE
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }

                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }
                    }

                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }
                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }

                "mentionComment" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    val name = it1.sender[0].name
                    holder.view.tvArAddedAmount.text = Html.fromHtml(
                        "<b>$name</b> mentioned you in a comment.",
                        Html.FROM_HTML_MODE_LEGACY
                    )
                //    holder.view.tvRequestStatus.visibility = View.GONE
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }

                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }
                    }
                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }
                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }

                "mentionBio" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    val name = it1.sender[0].name
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml("<b>$name</b>", Html.FROM_HTML_MODE_LEGACY)
                    holder.view.clCoin.visibility = View.GONE
                    holder.view.tvRequestStatus.visibility=View.VISIBLE
                    holder.view.tvRequestStatus.text = "mentioned you in their Bio."
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }

                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }
                    /* holder.view.ivUserProfileImage.setOnClickListener {
                         onClickListener.onClickProfile(position, list[position])
                     }*/

                }

                "appReport" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    holder.view.tvArAddedAmount.text = it1.sender[0].name
                    holder.view.ivCoin.visibility = View.GONE
                    holder.view.tvRequestStatus.visibility=View.VISIBLE
                    holder.view.tvRequestStatus.text = "report th app."
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }
                }
                /* "feedback"->{
                     holder.view.rlRequest.visibility=View.VISIBLE
                     holder.view.tvArAddedAmount.text = it.sender[0].name
                     holder.view.ivCoin.visibility=View.INVISIBLE
                     holder.view.tvRequestStatus.text = " started to following you"
                     CoroutineScope(Dispatchers.Main).launch {
                         holder.view.ivUserProfileImage.loadImageAsync(it.sender[0].profile.toString())
                     }
                 }*/
                "react" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    val name = it1.sender[0].name
                    holder.view.tvArAddedAmount.text = Html.fromHtml(
                        "<b>$name</b> reacted to your in Drop.",
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    holder.view.ivCoin.visibility = View.INVISIBLE
                 //   holder.view.tvRequestStatus.visibility = View.GONE
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }

                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }

                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }


                /*  "message"->{
                      holder.view.rlRequest.visibility=View.VISIBLE
                      holder.view.tvArAddedAmount.text = it.sender[0].name
                      holder.view.ivCoin.visibility=View.INVISIBLE
                      holder.view.tvRequestStatus.text = " sent you a new message."
                      CoroutineScope(Dispatchers.Main).launch {
                          holder.view.ivUserProfileImage.loadImageAsync(it.sender[0].profile.toString())
                      }
                  }*/
                "blueTickPurchase" -> {
                    val formattedText = "Your verification is on the way!"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.tvRequestStatus.visibility=View.VISIBLE
                    holder.view.tvRequestStatus.text ="Hi there,"

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivPostImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                holder.itemView.context,
                                R.drawable.bluetick
                            )
                        )
                    }


                }

                "blueTickBonus" -> {
                    val formattedText = "WeDo 80"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.ivPostImage.visibility = View.GONE
                    holder.view.ivCoin.visibility = View.GONE
                    holder.view.ivBlueTick.visibility = View.VISIBLE

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }

                    CoroutineScope(Dispatchers.Main).launch {

                    }
                }

                "wdPurchase" -> {
                    val ar = list[position].ar
                    val boldText = "<b>$ar AR </b>"
                    val formattedText = "$boldText has been added to your wallet"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }



                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }

                "adminMessage" -> {
                    val ar = list[position].ar
                    val boldText = "<b>$ar AR </b>"
                    val formattedText = "$boldText has been added to your wallet"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }




                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }

                "personalInfo" -> {
                    val ar = list[position].ar
                    val boldText = "<b>$ar AR </b>"
                    val formattedText = "$boldText has been added to your wallet"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }



                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }

                "treeBonus" -> {
                    val ar = list[position].ar
                    val boldText = "<b>$ar AR </b>"
                    val formattedText = "$boldText has been added to your wallet"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE
                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }


                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }

                "noAdsBonus" -> {
                    val ar = list[position].ar
                    val boldText = "<b>$ar AR </b>"
                    val formattedText = "$boldText has been added to your wallet"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }


                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }

                "aiBonus" -> {
                    val ar = list[position].ar
                    val boldText = "<b>$ar AR </b>"
                    val formattedText = "$boldText has been added to your wallet"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }



                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }

                "wdTransfer" -> {
                    val ar = list[position].ar
                    val boldText = "<b>$ar AR </b>"
                    val formattedText = "$boldText has been added to your wallet"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }



                    holder.view.userNotification.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }

                "referral" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    holder.view.tvArAddedAmount.text = it1.sender[0].name
                    holder.view.ivCoin.visibility = View.GONE
                    holder.view.tvRequestStatus.text = "shared your referral."
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }

                }
                "referralBonus" -> {
                    val ar = list[position].ar
                    val senderName = list[position].sender[0].name
                    val boldText = "<b>$ar AR </b>"
                    val senderNames = "<b>$senderName AR </b>"
                    val formattedText = "You earn $boldText AR by $senderNames as a referral bonus"
                    holder.view.tvArAddedAmount.text =
                        Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)
                    holder.view.rlRequest.visibility = View.VISIBLE

                    if (it1.sender.isNotEmpty()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                        }
                    }

                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }


                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }

                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickUserNotification(position, list[position])
                    }
                }
                "commentLike" -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    /*if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }*/
                    val description =
                        it1.comment?.description?.let { it2 -> Utils.convertDesc(it2).trim() }
                    val name = it1.sender[0].name
                    holder.view.tvArAddedAmount.text = Html.fromHtml(
                        "<b>$name</b> liked your comment ",
                        Html.FROM_HTML_MODE_LEGACY)
                    /*holder.view.tvArAddedAmount.text = Html.fromHtml(
                        "<b>$name</b> liked your comment : $description",
                        Html.FROM_HTML_MODE_LEGACY
                    )*/
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }
                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }
                    }

                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }

                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }
                "adminNotification"->{
                    holder.view.rlRequest.visibility = View.VISIBLE
                    holder.view.clCoin.visibility = View.GONE
                    if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }
                    val description =
                        it1.comment?.description?.let { it2 -> Utils.convertDesc(it2).trim() }
                    val name = it1.sender[0].name
                    holder.view.tvArAddedAmount.text = Html.fromHtml(
                        "<b>$name</b>"  ,
                        Html.FROM_HTML_MODE_LEGACY
                    )
                    holder.view.tvRequestStatus.visibility=View.VISIBLE
                    holder.view.tvRequestStatus.text="test notification"

                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }
                  /*  if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post?.images.toString())
                        }
                    }*/

                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }

                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickAdminNotification(position, list[position])
                    }
                   /* holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }*/
                }

                else -> {
                    holder.view.rlRequest.visibility = View.VISIBLE
                    if (it1.sender[0].blueTickEnable) {
                        holder.view.ivBlueTick.visibility = View.VISIBLE
                    } else {
                        holder.view.ivBlueTick.visibility = View.GONE
                    }
                    val description =
                        it1.comment?.description?.let { it2 -> Utils.convertDesc(it2).trim() }
                    val name = it1.sender[0].name
                    holder.view.tvArAddedAmount.text = Html.fromHtml(
                        "<b>$name</b>" ,
                        Html.FROM_HTML_MODE_LEGACY
                    )

                    holder.view.tvRequestStatus.visibility=View.VISIBLE
                    if (it1.message.isNullOrEmpty()){
                        holder.view.tvRequestStatus.text=it1.message
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        holder.view.ivUserProfileImage.loadImageAsyncMessage(it1.sender[0].profile.toString())
                    }
                    if (it1.post?.type == "video") {
                        CoroutineScope(Dispatchers.Main).launch {
                            holder.view.ivPostImage.loadImageAsyncMessage(it1.post.videoThumbnail)
                        }
                    } else {
                        if (!it1.post?.images.isNullOrEmpty()){
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.view.ivPostImage.loadImageMessage(it1.post?.images?.get(0).toString())
                            }
                        }else{
                            holder.view.ivCoin.visibility=View.INVISIBLE
                            holder.view.ivPostImage.setImageResource(R.drawable.post_not_found)
                        }
                    }

                    holder.view.ivUserProfileImage.setOnClickListener {
                        onClickListener.onClickProfile(position, list[position])
                    }

                    holder.view.llUserName.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivCoin.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                    holder.view.ivPostImage.setOnClickListener {
                        onClickListener.onClickLikePost(position, list[position])
                    }
                }
            }
        }

        /** Handling the swipeListener and also handle the swipe animation show for 1 to 2 sec*/
        if (position == 0) {
            runWithDelay(1000) {
                holder.view.SwipeReveal.open(true)
            }
            runWithDelay(1500) {
                holder.view.SwipeReveal.close(true)
            }
        }
        holder.view.SwipeReveal.setSwipeListener(object : Swipe {
            override fun onClosed(view: SwipeLayout) {
            }
            override fun onOpened(view: SwipeLayout) {
               /** Close the previously open item*/
                if (swipeLayout != null && swipeLayout != holder.view.SwipeReveal) {
                    swipeLayout?.close(true)
                }
                swipeLayout = holder.view.SwipeReveal


            }

            override fun onSlide(view: SwipeLayout, slideOffset: Float) {
                // Handle sliding events if needed

            }
        })

        holder.view.rlSwipeDeleteButton.setOnClickListener {
            if (isInternetAvailables(holder.itemView.context)) {
                selectedItemPosition = position
                onClickListener.onDeleteClick(position, list[position])
                if (list.size<=1){
                }else{
                    removeItem(position)
                }

            } else {
                Utils.showToast("Please check your internet connection.")
            }

        }
        val dateTimeString = list[position].createdAt
        if (!dateTimeString.isNullOrEmpty()) {
            Utils.convertDateAndTime(dateTimeString)
        }
        holder.view.tvDate.text = Utils.time

        if (list[position].isSame) {
            holder.view.tvDayMonth.visibility = View.GONE
        } else {
            Log.e("tvDayMonth", Utils.hours.toString())
            holder.view.tvDayMonth.text = list[position].createdDate
            holder.view.tvDayMonth.visibility = View.VISIBLE
            holder.view.tvDayMonth.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }

    }

    private fun removeItem(index: Int) {
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, list.size)
        }
    }


    interface OnClickListener {
        fun onDeleteClick(position: Int, data: NotificationListData)
        fun onClickUserNotification(position: Int, data: NotificationListData)
        fun onClickProfile(position: Int, data: NotificationListData)
        fun onClickLikePost(position: Int, data: NotificationListData)
        fun onClickAccept(position: Int, data: NotificationListData)
        fun onClickAdminNotification(position: Int, data: NotificationListData)
    }

    override fun hashTagClick(text: String) {

    }

    override fun mentionClick(text: String) {

    }
}