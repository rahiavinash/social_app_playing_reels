package com.wedo8020.app.mainui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import androidx.databinding.DataBindingUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.wedo8020.app.R
import com.wedo8020.app.base.PlayerViewAdapter
import com.wedo8020.app.databinding.HomeListAdBinding
import com.wedo8020.app.databinding.HomeListItemBinding
import com.wedo8020.app.databinding.HomeListVideoItemBinding
import com.wedo8020.app.eventBus.isDoubleTap
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.Options
import com.wedo8020.app.utils.AppConstant.PRIVATE
import com.wedo8020.app.utils.Hashtag
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.squareup.picasso.Picasso
import com.wedo8020.app.MainApplication
import com.wedo8020.app.eventBus.HomePagerScroll
import com.wedo8020.app.eventBus.isGhostMode
import com.wedo8020.app.eventBus.isInnerPageScroll
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Utils.disableMultiTap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.util.Integers
import org.greenrobot.eventbus.EventBus


class PostAdapter(
    var list: ArrayList<Data?>, val clickListener: ClickListener, val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Hashtag.onHashtagClick {
    private var updateSeekBar = true
    private var homeListItemBinding: HomeListItemBinding? = null
    private var homeListAdBinding: HomeListAdBinding? = null
    private var homeListVideoBinding: HomeListVideoItemBinding? = null
    private var adView: AdView? = null
    private var isLHView = Prefs.init().isLHPost
    private val handler = Handler(Looper.getMainLooper())
    private val handlerOneFinger = Handler(Looper.getMainLooper())
    private val postDb by lazy {
        CreatePostDbCreation(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: RecyclerView.ViewHolder?
        when (viewType) {
            AD_TYPE -> {
                homeListAdBinding =
                    DataBindingUtil.inflate(inflater, R.layout.home_list_ad, parent, false)
                viewHolder = AdHolder(homeListAdBinding!!)
            }

            VIDEO -> {
                homeListVideoBinding =
                    DataBindingUtil.inflate(inflater, R.layout.home_list_video_item, parent, false)
                viewHolder = VideoViewHolder(homeListVideoBinding!!)
            }

            else -> {
                homeListItemBinding =
                    DataBindingUtil.inflate(inflater, R.layout.home_list_item, parent, false)
                viewHolder = ViewHolder(homeListItemBinding!!)
            }
        }
        return viewHolder
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        Log.e("holder_pos", position.toString())

        when (holder.itemViewType) {
            /* AD_TYPE -> {
                 val viewHolder = holder as AdHolder
                 adView = viewHolder.binding.adViewStory
                 //ad load work on recycler scroll
             }*/

            VIDEO -> {
                val viewHolder = holder as VideoViewHolder

                viewHolder.binding.apply {

                    val item = list[position]

                    //GHOST MODE
                    videoGhostMode(this)

                    //For LHS & RHS handling
                    reverseVideoItem(this)
                    if (!Prefs.init().isLHPost && profileInfoContainer.getChildAt(0).id != profileImg.id) {
                        reverseLinearLayout(profileInfoContainer)
                        reverseLinearLayout(profileNameInfoContainer)
                    } else if (Prefs.init().isLHPost && profileInfoContainer.getChildAt(0).id != container.id) {
                        reverseLinearLayout(profileInfoContainer)
                        reverseLinearLayout(profileNameInfoContainer)
                    }


                    data = item



                    thumbnail.setImageResource(R.drawable.black_placeholder)
                    thumbnail.visibility = View.VISIBLE
                    //save sound
                    /*if (item!!.isSoundSaved) {
                        saveSoundBtn.imageTintList = ColorStateList.valueOf(
                            viewHolder.itemView.context.getColor(R.color.RedColor)
                        )
                    } else {
                        saveSoundBtn.imageTintList = ColorStateList.valueOf(
                            viewHolder.itemView.context.getColor(R.color.pureWhite)
                        )
                    }*/

                    //like count visibility
                    totalPostLike.visibility = if (item?.options?.like == true) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }

//                    totalPostLike.text = item?.noOfLikes?.toString() ?: ""

                    //comment button visibility
                    if (item?.options?.comment == true) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }.also {
                        commentBtn.visibility = it
                        commentBtnGhost.visibility = it
                    }

                    item?.noOfComments?.let {
                        if (it < 1) {
                            commentCount.text = ""
                        } else {
                            commentCount.text = item?.noOfComments?.toString() ?: ""
                        }
                    }

                    if (item?.noOfComments == null) {
                        commentCount.text = ""
                    }
                    //save post
                    saveBtn.setImageResource(
                        if (item?.isSaved == true) {
                            R.drawable.ic_savepost
                        } else {
                            R.drawable.ic_unsavepost
                        }
                    )

                    //like post
                    likeBtn.setImageResource(
                        if (item?.isLike == true) {
                            R.drawable.ic_like
                        } else {
                            R.drawable.ic_unlike
                        }
                    )

                    if (list[position]?.creator?._id == Prefs.init().userId) {
                        overallContainer.visibility = View.VISIBLE
                    } else {
                        overallContainer.visibility = View.GONE
                    }

                    //follow status handling
                    when (item?.creator?.followStatus) {
                        0 -> {
                            followContainer.visibility = View.GONE
                        }

                        1 -> {
                            followContainer.visibility = View.VISIBLE
                            followStatus.text =
                                viewHolder.itemView.context.getText(R.string.following)
                        }

                        2 -> {
                            followContainer.visibility = View.VISIBLE
                            followStatus.text = viewHolder.itemView.context.getText(R.string.follow)
                        }

                        3 -> {
                            followContainer.visibility = View.VISIBLE
                            followStatus.text =
                                viewHolder.itemView.context.getText(R.string.requested)
                        }
                    }
                    list[position]?.noOfSaved?.let {
                        if (it < 1) {
                            saveText.text = ""
                        } else {
                            saveText.text = it.toString()
                        }
                    }
                    if (list[position]?.noOfSaved == null) {
                        saveText.text = ""
                    }

                    saveBtn.setOnClickListener {
                        if (Utils.isInternetAvailables(holder.itemView.context)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                ifUploadedThenRun(list[position]?.postId ?: "") {
                                    if (list[position]?.isSaved == true) {
                                        saveBtn.setImageResource(R.drawable.ic_unsavepost)
                                        list[position]?.noOfSaved =
                                            list[position]?.noOfSaved?.minus(1) ?: 0
                                    } else {
                                        saveBtn.setImageResource(R.drawable.ic_savepost)
                                        list[position]?.noOfSaved =
                                            list[position]?.noOfSaved?.plus(1) ?: 0
                                    }
                                    list[position]?.noOfSaved?.let {
                                        if (it < 1) {
                                            saveText.text = ""
                                        } else {
                                            saveText.text = it.toString()
                                        }
                                    }
                                    if (list[position]?.noOfSaved == null) {
                                        saveText.text = ""
                                    }
                                    clickListener.saveClick(position)
                                    it.disableMultiTap()
                                }
                            }
                        } else {
                            Utils.showToast(holder.itemView.context.getString(R.string.no_internet_message))
                        }
                    }


                    /*saveSoundBtn.setOnClickListener {
                        saveSoundBtn.imageTintList = ColorStateList.valueOf(
                            viewHolder.itemView.context.getColor(
                                if (item.isSoundSaved) {
                                    R.color.pureWhite
                                } else {
                                    android.R.color.holo_red_dark
                                }
                            )
                        )
                        clickListener.saveSoundClick(position)
                    }*/


                    ivGhostBtn.setOnClickListener {
                        Prefs.init().isGhostMode = false
                        EventBus.getDefault().post(isGhostMode(Prefs.init().isGhostMode))
                        it.disableMultiTap()
//                        videoGhostMode(this)
                    }
                    list[position]?.noOfShares?.let {
                        shareText.text = if (it > 0) {
                            it.toString()
                        } else {
                            ""
                        }
                    }
                    if (list[position]?.noOfShares == null) {
                        shareText.text = ""
                    }
                    shareContainer.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            ifUploadedThenRun(item?.postId ?: "") {
                                it.disableMultiTap()
                                clickListener.shareClick(position, shareText)
                            }
                        }


                    }
                    list[position]?.noOfLikes?.let {
                        if (it < 1) {
                            totalPostLike.text = ""
                        } else {
                            totalPostLike.text = it.toString()
                        }
                    }
                    if (list[position]?.noOfLikes == null) {
                        totalPostLike.text = ""
                    }


                    likeBtn.setOnClickListener {
                        it.disableMultiTap(1500)
                        if (Utils.isInternetAvailables(viewHolder.itemView.context)) {
                            if (list[position]?.isLike == true) {
                                likeBtn.setImageResource(R.drawable.ic_unlike)
                                list[position]?.noOfLikes = list[position]?.noOfLikes?.minus(1)
                                list[position]?.creator?.overallLikes =
                                    list[position]?.creator?.overallLikes?.minus(1) ?: 0

                                overallLike.text =
                                    list[position]?.creator?.overallLikes?.toString() ?: "0"
                                list[position]?.noOfLikes?.let {
                                    if (it < 1) {
                                        totalPostLike.text = ""
                                    } else {
                                        totalPostLike.text = it.toString()
                                    }
                                }
                                if (list[position]?.noOfLikes == null) {
                                    totalPostLike.text = ""
                                }
                                clickListener.likeClick(position)
                            } else {
                                val windowManager =
                                    viewHolder.itemView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                // Get the DisplayMetrics
                                val displayMetrics = DisplayMetrics()
                                windowManager.defaultDisplay.getMetrics(displayMetrics)

                                // Calculate the center coordinates
                                val centerX = displayMetrics.widthPixels / 2
                                val centerY = displayMetrics.heightPixels / 2
                                addViewAndAnimate(
                                    centerX.toFloat(), centerY.toFloat(), holder.itemView.context,
                                    constraintlayout, postActivity
                                )

                                Log.e("asasdffgdf", "likeBtnv")
                                EventBus.getDefault().post(
                                    isDoubleTap(
                                        position,
                                        0.toFloat(),
                                        0.toFloat(),
                                        list[position]?.postId ?: list[position]?._id ?: "",
                                        likeBtn,
                                        totalPostLike,
                                        overallLike,
                                        0.0f,
                                        0.0f
                                    )
                                )
                                if (list[position]?.creator?._id == Prefs.init().userId) {
                                    overallContainer.visibility = View.VISIBLE
                                }
                                /*likeBtn.setImageResource(R.drawable.ic_like)
                                list[position]?.noOfLikes = list[position]?.noOfLikes?.plus(1)
                                list[position]?.creator?.overallLikes =
                                    list[position]?.creator?.overallLikes?.plus(1) ?: 0*/
                            }


                        } else {
                            Utils.showToast(viewHolder.itemView.context.getString(R.string.no_internet_message))
                        }

                    }

                    commentBtn.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            ifUploadedThenRun(item?.postId ?: "") {
                                commentBtn.disableMultiTap()
                                clickListener.commentClick(
                                    position,
                                    item?.postId ?: item?._id ?: "",
                                    commentCount
                                )
                            }
                        }

                    }

                    followContainer.setOnClickListener {
                        if (Utils.isInternetAvailables(holder.itemView.context)) {
                            when (item?.creator?.followStatus) {
                                1, 3 -> {
                                    followStatus.text =
                                        viewHolder.itemView.context.getText(R.string.follow)
                                }

                                2 -> {
                                    if (item.creator.accountType.equals(
                                            PRIVATE,
                                            ignoreCase = true
                                        )
                                    ) {
                                        followStatus.text =
                                            viewHolder.itemView.context.getText(R.string.requested)
                                    } else {
                                        followStatus.text =
                                            viewHolder.itemView.context.getText(R.string.following)
                                    }
                                }
                            }
//                        if (item?.creator?.followStatus != 3) {
                            clickListener.followUnfollowClick(position)
                            it.disableMultiTap()
                        } else {
                            holder.itemView.context.getString(R.string.no_internet_message)
                        }

//                        }
                    }

                    if (!item?.tagUsers.isNullOrEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }.also {
                        tagBtn.visibility = it
                        tagBtnGhost.visibility = it
                    }



                    icVerified.visibility = if (item?.creator?.blueTickEnable == true) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                    tagBtn.setOnClickListener {
                        clickListener.tagClick(position)
                        it.disableMultiTap()
                    }

                    profileImg.setOnClickListener {
                        clickListener.mentionClick(item?.creator?._id ?: "")
                        it.disableMultiTap()
                    }

                    username.setOnClickListener {
                        clickListener.mentionClick(item?.creator?._id ?: "")
                        it.disableMultiTap()
                    }

                    username.text = item?.creator?.username ?: ""


                    CoroutineScope(Dispatchers.IO).launch {

                        thumbnail.loadImageAsync(
                            item?.videoThumbnail ?: "",
                            R.drawable.black_placeholder
                        )

                        profileImg.loadImageAsync(item?.creator?.profile ?: "")
                    }
//                    thumbnail.visibility = View.VISIBLE
                    if (!item?.description.isNullOrEmpty()) {
//                        val usernames = Utils.getUsernames(item?.description ?: "")
                        val description = Utils.convertDesc(item?.description ?: "")

                        val ss = SpannableString(description)
                        val hashtagSpans1: ArrayList<IntArray> = Utils.getSpans(description, '#')
                        val calloutSpans1: ArrayList<IntArray> = Utils.getSpans(description, '@')
                        Utils.setSpanComment(
                            ss,
                            hashtagSpans1,
                            this@PostAdapter, item?.hashtags?.let { ArrayList(it) } ?: ArrayList()
                        )
                        Utils.setSpanUname(
                            ss,
                            calloutSpans1,
                            this@PostAdapter,
                            item?.mentionUsers
                        )
                        desc.visibility = View.VISIBLE
                        desc.movementMethod = LinkMovementMethod.getInstance()
                        desc.text = ss
                        desc.maxLines = 1
                    } else {
                        desc.visibility = View.GONE
                    }
                    desc.setOnClickListener {
                        if (desc.maxLines == 1) {
                            desc.maxLines = Integer.MAX_VALUE
                        } else {
                            desc.maxLines = 1
                        }
                    }
                    desc.setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (desc.maxLines > 1) {
                                    EventBus.getDefault().post(isInnerPageScroll(false))
                                }
                            }             // Hide the thumb when touched


                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                EventBus.getDefault().post(isInnerPageScroll(true))
                            }

                        }
                        false
                    }
                    /*if (position == 0) {
                        PlayerViewAdapter.setMediaSourceFromFrag(
                            item.video,
                            item.postId,
                            holder.binding.playerView
                        ) {
                            PlayerViewAdapter.playCurrentPlayingVideo()
                        }

                    }*/

                    // creating textView to dynamically show above seekbar

                    val textView = TextView(viewHolder.itemView.context)
                    seekBar.setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                playerView.player?.playWhenReady = false
                                EventBus.getDefault().post(HomePagerScroll(false))
                            }             // Hide the thumb when touched

                            MotionEvent.ACTION_MOVE -> {
                                //show time in textView
                                addImageView(
                                    textView,
                                    seekBar.progress.toLong(),
                                    constraintlayout,
                                    event.x,
                                    seekBarMainLayout.y
                                )

                            }

                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                                EventBus.getDefault().post(HomePagerScroll(true))
                                constraintlayout.removeView(textView)
                                playerView.player?.playWhenReady = true
                                seekBar.thumb = null// Show the thumb when released or canceled
                                // Replace with your thumb drawable
                            }

                        }
                        false
                    }

                    seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                Log.e("sadfas", progress.toString())
                                playerView.player?.seekTo(seekBar.progress.toLong())
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {
                            updateSeekBar = false
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar) {
                            updateSeekBar = true
                            updateSeekBarProgressAutomatically(seekBar, playerView)
//                            playerView.player?.seekTo(seekBar.progress.toLong())
                        }
                    })

                    //Player touch gestures functionality implemented here
                    setPlayerTouchListener(
                        position,
                        holder.itemView.context,
                        item?.postId ?: item?._id ?: "",
                        likeBtn,
                        totalPostLike, overallLike,
                        playerView, this
                    )
                }
            }

            else -> {
                val viewHolder = holder as ViewHolder
                viewHolder.binding.apply {


                    ghostMode(this)
                    reverseImageItem(this)

                    if (!Prefs.init().isLHPost && profileInfoContainer.getChildAt(0).id != profileImg.id) {
                        reverseLinearLayout(profileInfoContainer)
                        reverseLinearLayout(profileNameInfoContainer)
                    } else if (Prefs.init().isLHPost && profileInfoContainer.getChildAt(0).id != container.id) {
                        reverseLinearLayout(profileInfoContainer)
                        reverseLinearLayout(profileNameInfoContainer)
                    }


                    data = list[position]


                    if (list[position]?.isLike == true) {
                        likeBtn.setImageResource(R.drawable.ic_like)
                    } else {
                        likeBtn.setImageResource(R.drawable.ic_unlike)
                    }
                    when (list[position]?.creator?.followStatus) {
                        0 -> {
                            followContainer.visibility = View.GONE
                        }

                        1 -> {
                            followContainer.visibility = View.VISIBLE
                            followStatus.text =
                                viewHolder.itemView.context.getText(R.string.following)
                        }

                        2 -> {
                            followContainer.visibility = View.VISIBLE
                            followStatus.text = viewHolder.itemView.context.getText(R.string.follow)
                        }

                        3 -> {
                            followContainer.visibility = View.VISIBLE
                            followStatus.text =
                                viewHolder.itemView.context.getText(R.string.requested)
                        }
                    }

                    // verified icon
                    icVerified.visibility = if (list[position]?.creator?.blueTickEnable == true) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                    //save post
                    saveBtn.setImageResource(
                        if (list[position]?.isSaved == true) {
                            R.drawable.ic_savepost
                        } else {
                            R.drawable.ic_unsavepost
                        }
                    )
                    if (list[position]?.options?.comment == true) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }.also {
                        commentBtn.visibility = it
                        commentBtnGhost.visibility = it
                    }
                    list[position]?.noOfComments?.let {
                        if (it < 1) {
                            commentCount.text = ""
                        } else {
                            commentCount.text = it.toString()
                        }
                    }
                    if (list[position]?.noOfComments == null) {
                        commentCount.text = ""
                    }
                    totalPostLike.visibility = if (list[position]?.options?.like == true) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                    if (!list[position]?.tagUsers.isNullOrEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }.also {
                        tagBtn.visibility = it
                        tagBtnGhost.visibility = it
                    }
                    list[position]?.noOfComments?.let {
                        if (it < 1) {
                            commentCount.text = ""
                        } else {
                            commentCount.text = it.toString()
                        }
                    }
                    if (list[position]?.noOfComments == null) {
                        commentCount.text = ""
                    }

                    //Overall like
                    if (list[position]?.creator?._id == Prefs.init().userId) {
                        overallContainer.visibility = View.VISIBLE
                    } else {
                        overallContainer.visibility = View.GONE
                    }
                    ivGhostBtn.setOnClickListener {
                        Prefs.init().isGhostMode = false
                        EventBus.getDefault().post(isGhostMode(Prefs.init().isGhostMode))
                        it.disableMultiTap()
//                        ghostMode(this)
                    }
                    followContainer.setOnClickListener {
                        if (Utils.isInternetAvailables(holder.itemView.context)) {
                            when (list[position]?.creator?.followStatus) {
                                1, 3 -> {
                                    followStatus.text =
                                        viewHolder.itemView.context.getText(R.string.follow)
                                }

                                2 -> {
                                    if (list[position]?.creator?.accountType?.equals(
                                            "private",
                                            ignoreCase = true
                                        ) == true
                                    ) {
                                        followStatus.text =
                                            viewHolder.itemView.context.getText(R.string.requested)
                                    } else {
                                        followStatus.text =
                                            viewHolder.itemView.context.getText(R.string.following)
                                    }
                                }

                                /* 3 -> {
                                     followStatus.text =
                                         viewHolder.itemView.context.getText(R.string.follow)
                                 }*/

                            }
//                        if (list[position]!!.creator.followStatus != 3) {
                            clickListener.followUnfollowClick(position)
                            it.disableMultiTap()
                        } else {
                            Utils.showToast(holder.itemView.context.getString(R.string.no_internet_message))
                        }
//                        }
                    }
                    list[position]?.noOfLikes?.let {
                        if (it < 1) {
                            totalPostLike.text = ""
                        } else {
                            totalPostLike.text = it.toString()
                        }
                    }
                    if (list[position]?.noOfLikes == null) {
                        totalPostLike.text = ""
                    }

                    likeBtn.setOnClickListener {
                        it.disableMultiTap(1500)
                        if (Utils.isInternetAvailables(viewHolder.itemView.context)) {
                            if (list[position]?.isLike == true) {
                                likeBtn.setImageResource(R.drawable.ic_unlike)
                                list[position]?.noOfLikes = list[position]?.noOfLikes?.minus(1)
                                list[position]?.creator?.overallLikes =
                                    list[position]?.creator?.overallLikes?.minus(1) ?: 0

                                overallLike.text =
                                    list[position]?.creator?.overallLikes?.toString() ?: "0"
                                list[position]?.noOfLikes?.let {
                                    if (it < 1) {
                                        totalPostLike.text = ""
                                    } else {
                                        totalPostLike.text = it.toString()
                                    }
                                }
                                if (list[position]?.noOfLikes == null) {
                                    totalPostLike.text = ""
                                }
                                clickListener.likeClick(position)
                            } else {
                                val windowManager =
                                    viewHolder.itemView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                // Get the DisplayMetrics
                                val displayMetrics = DisplayMetrics()
                                windowManager.defaultDisplay.getMetrics(displayMetrics)

                                // Calculate the center coordinates
                                val centerX = displayMetrics.widthPixels / 2
                                val centerY = displayMetrics.heightPixels / 2
                                addViewAndAnimate(
                                    centerX.toFloat(), centerY.toFloat(), holder.itemView.context,
                                    constraintLayout, postActivity
                                )
                                Log.e("asasdffgdf", "likeBtnim")
                                EventBus.getDefault().post(
                                    isDoubleTap(
                                        position,
                                        0.toFloat(),
                                        0.toFloat(),
                                        list[position]?.postId ?: list[position]?._id ?: "",
                                        likeBtn,
                                        totalPostLike,
                                        overallLike,
                                        0.0f,
                                        0.0f
                                    )
                                )
                                if (list[position]?.creator?._id == Prefs.init().userId) {
                                    overallContainer.visibility = View.VISIBLE
                                }


                                /*                        likeBtn.setImageResource(R.drawable.ic_like)
                                                        list[position]?.noOfLikes = list[position]?.noOfLikes?.plus(1)
                                                        list[position]?.creator?.overallLikes =
                                                            list[position]?.creator?.overallLikes?.plus(1) ?: 0*/

                            }


                        } else {
                            Utils.showToast(viewHolder.itemView.context.getString(R.string.no_internet_message))
                        }

                    }
                    profileImg.setOnClickListener {
                        clickListener.mentionClick(list[position]?.creator?._id ?: "")
                        it.disableMultiTap()
                    }

                    username.setOnClickListener {
                        clickListener.mentionClick(list[position]?.creator?._id ?: "")
                        it.disableMultiTap()
                    }
                    commentBtn.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            ifUploadedThenRun(list[position]?.postId ?: "") {
                                commentBtn.disableMultiTap()
                                clickListener.commentClick(
                                    position,
                                    list[position]?.postId ?: list[position]?._id ?: "",
                                    commentCount
                                )
                            }
                        }
                    }
                    list[position]?.noOfSaved?.let {
                        if (it < 1) {
                            saveText.text = ""
                        } else {
                            saveText.text = it.toString()
                        }
                    }
                    if (list[position]?.noOfSaved == null) {
                        saveText.text = ""
                    }

                    saveBtn.setOnClickListener {
                        if (Utils.isInternetAvailables(holder.itemView.context)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                ifUploadedThenRun(list[position]?.postId ?: "") {
                                    if (list[position]?.isSaved == true) {
                                        saveBtn.setImageResource(R.drawable.ic_unsavepost)
                                        list[position]?.noOfSaved =
                                            list[position]?.noOfSaved?.minus(1) ?: 0
                                    } else {
                                        saveBtn.setImageResource(R.drawable.ic_savepost)
                                        list[position]?.noOfSaved =
                                            list[position]?.noOfSaved?.plus(1) ?: 0
                                    }
                                    list[position]?.noOfSaved?.let {
                                        if (it < 1) {
                                            saveText.text = ""
                                        } else {
                                            saveText.text = it.toString()
                                        }
                                    }
                                    if (list[position]?.noOfSaved == null) {
                                        saveText.text = ""
                                    }
                                    clickListener.saveClick(position)
                                    it.disableMultiTap()
                                }
                            }
                        } else {
                            Utils.showToast(holder.itemView.context.getString(R.string.no_internet_message))
                        }

                    }
                    list[position]?.noOfShares?.let {
                        shareText.text = if (it > 0) {
                            it.toString()
                        } else {
                            ""
                        }
                    }
                    if (list[position]?.noOfShares == null) {
                        shareText.text = ""
                    }
                    shareContainer.setOnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            ifUploadedThenRun(list[position]?.postId ?: "") {
                                it.disableMultiTap()
                                clickListener.shareClick(position, shareText)
                            }
                        }

                    }

                    tagBtn.setOnClickListener {
                        clickListener.tagClick(position)
                        it.disableMultiTap()
                    }
                    list.get(position)?.images?.let {
                        if (it.size > 1) {
                            sliderDots.visibility = View.VISIBLE
                            tvPhotos.text = "Photos"
                        } else {
                            sliderDots.visibility = View.GONE
                            tvPhotos.text = "Photo"
                        }
                    }
                    val viewPagerAdapter = list[position]?.images?.let {
                        ImageSlide(
                            holder.itemView.context,
                            it,
                            object : ImageSlide.GestureListener {
                                override fun onDoubleTap(x: Float, y: Float) {
                                    if (Utils.isInternetAvailables(holder.itemView.context)) {
                                        likeBtn.disableMultiTap(1500)
                                        val location2 = IntArray(2)
                                        likeBtn.getLocationOnScreen(location2)
                                        val x2 = location2[0]
                                        val y2 = location2[1]
                                        val xInConstraintLayout = x2 - likeBtn.left
                                        val yInConstraintLayout = y2 - likeBtn.right
                                        addViewAndAnimate(
                                            x, y + 250, holder.itemView.context,
                                            constraintLayout, postActivity
                                        )
                                        Log.e("asasdffgdf", "likeim")
                                        EventBus.getDefault().post(
                                            isDoubleTap(
                                                position,
                                                x,
                                                y + 250,
                                                list[position]?.postId ?: list[position]?._id ?: "",
                                                likeBtn,
                                                totalPostLike,
                                                overallLike,
                                                xInConstraintLayout.toFloat(),
                                                yInConstraintLayout.toFloat()
                                            )
                                        )

                                    } else {
                                        Utils.showToast(holder.itemView.context.getString(R.string.no_internet_message))
                                    }


                                }

                                override fun onTwoFingerTouchAndHold() {
                                    Log.e("onTwoFingerTouchAndHold", "true")
                                    isLHView = !isLHView
                                    Prefs.init().isLHPost = !Prefs.init().isLHPost
                                    /* if (!isLHView){*/
                                    //reverse swap constraint
                                    reverseImageItem(this@apply)
                                    //reverse profile info container
                                    reverseLinearLayout(profileInfoContainer)
                                    reverseLinearLayout(profileNameInfoContainer)
                                    Utils.vibrateOnce(viewHolder.itemView.context, 1, 50)
                                    for (i in 0 until itemCount) {
                                        if (i != position) {
                                            notifyItemChanged(i)
                                        }
                                    }
                                }

                                override fun onLongPress() {
                                    try {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            ifUploadedThenRun(list[position]?.postId ?: "") {
                                                EventBus.getDefault().post(isInnerPageScroll(false))
                                                EventBus.getDefault().post(HomePagerScroll(false))
                                                clickListener.tapAndHoldOptionMenu(
                                                    position,
                                                    imagePager.currentItem,
                                                    list[position]!!.options
                                                )
                                            }
                                        }
                                    } catch (_: Exception) {
                                    }

                                }
                            })
                    }

                    imagePager.adapter = viewPagerAdapter
                    if ((list[position]?.images?.size ?: 0) > 1) {
                        createDotSlider(
                            this,
                            list[position]?.images?.size ?: 0,
                            viewHolder.itemView.context
                        )
                    }
                    if (!list[position]?.description.isNullOrEmpty()) {
                        val usernames = Utils.getUsernames(list[position]?.description ?: "")
                        Log.e("usernamesList", usernames.toString())
                        val description = Utils.convertDesc(list[position]?.description ?: "")
                        Log.d("description", description.toString())
                        val ss = SpannableString(description)
                        val hashtagSpans1: ArrayList<IntArray> = Utils.getSpans(description, '#')
                        val calloutSpans1: ArrayList<IntArray> = Utils.getSpans(description, '@')
                        Utils.setSpanComment(
                            ss,
                            hashtagSpans1,
                            this@PostAdapter,
                            list[position]?.hashtags?.let { ArrayList(it) } ?: ArrayList()
                        )
//                        Log.e("mentionClickcomment", list[position]!!.mentionUsers.toString())
                        Utils.setSpanUname(
                            ss,
                            calloutSpans1,
                            this@PostAdapter,
                            list[position]?.mentionUsers
                        )
                        desc.movementMethod = LinkMovementMethod.getInstance()
                        desc.text = ss
                        desc.maxLines = 1
                    } else {
                        desc.visibility == View.GONE
                    }
                    desc.setOnClickListener {
                        if (desc.maxLines == 1) {
                            desc.maxLines = Integer.MAX_VALUE
                        } else {
                            desc.maxLines = 1
                        }
                    }
                    desc.setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                if (desc.maxLines > 1) {
                                    EventBus.getDefault().post(isInnerPageScroll(false))
                                }
                            }             // Hide the thumb when touched


                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                EventBus.getDefault().post(isInnerPageScroll(true))
                            }

                        }
                        false
                    }
                }
            }
        }
    }

    private fun updateSeekBarProgressAutomatically(seekBar: SeekBar, playerView: PlayerView) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (updateSeekBar) {
                    seekBar.progress = playerView.player?.currentPosition?.toInt() ?: 0
                    handler.postDelayed(this, 100) // Update every second for smoother performance
                }
            }
        }, 100)
    }

    fun ghostMode(binding: HomeListItemBinding) {
        binding.apply {
            if (Prefs.init().isGhostMode) {
                profileLayout.alpha = 0.0f
                postActivity1.alpha = 0.0f
                tagBtn.alpha = 0.0f
                tagBtnGhost.alpha = 1.0f
                binding.ivGhostBtn.visibility = View.VISIBLE
                postActivityGhost.visibility = View.VISIBLE
            } else {
                profileLayout.alpha = 1.0f
                postActivity1.alpha = 1.0f
                tagBtn.alpha = 1.0f
                tagBtnGhost.alpha = 0.0f
                binding.ivGhostBtn.visibility = View.GONE
                postActivityGhost.visibility = View.GONE
            }
        }
    }

    private suspend fun ifUploadedThenRun(postId: String, run: (() -> Unit)) {

        withContext(Dispatchers.IO) {
            if (postDb.getCreatePostData().fetchPost(postId).isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    Utils.showToast("Still Processing Upload...")
                }
            } else {
                withContext(Dispatchers.Main) {
                    run()
                }
            }
        }
    }

    suspend fun isPostUploaded(postId: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (postDb.getCreatePostData().fetchPost(postId).isNullOrEmpty().not()) {
                Utils.showToast(postDb.getCreatePostData().fetchPost(postId).toString())
                false
            } else {

                true
            }
        }
    }

    fun videoGhostMode(binding: HomeListVideoItemBinding) {
        binding.apply {
            if (Prefs.init().isGhostMode) {
                profileLayout.alpha = 0.0f
                postActivity1.alpha = 0.0f
                tagBtn.alpha = 0.0f
                tagBtnGhost.alpha = 1.0f
                binding.ivGhostBtn.visibility = View.VISIBLE
                postActivityGhost.visibility = View.VISIBLE
            } else {
                profileLayout.alpha = 1.0f
                postActivity1.alpha = 1.0f
                tagBtn.alpha = 1.0f
                tagBtnGhost.alpha = 0.0f
                binding.ivGhostBtn.visibility = View.GONE
                postActivityGhost.visibility = View.GONE
            }
        }
    }

    fun reverseImageItem(homeListItemBinding: HomeListItemBinding) {

        homeListItemBinding.apply {
            val constraintSet = ConstraintSet()
            constraintSet.also {
                it.clone(constraintLayout)
                // Remove horizontal constraints for the button
                it.clear(postActivity.id, ConstraintSet.END)
                it.clear(postActivity.id, ConstraintSet.START)
                it.clear(postActivity.id, ConstraintSet.BOTTOM)

                // Remove vertical constraints for the button
                it.clear(profileLayout.id, ConstraintSet.TOP)
                it.clear(profileLayout.id, ConstraintSet.START)
                it.clear(profileLayout.id, ConstraintSet.END)
                it.clear(profileLayout.id, ConstraintSet.BOTTOM)
                it.clear(ivGhostBtn.id, ConstraintSet.END)
                it.clear(ivGhostBtn.id, ConstraintSet.START)
                it.applyTo(constraintLayout)
            }

            val param1 = profileLayout.layoutParams as ConstraintLayout.LayoutParams
            val param2 = postActivity.layoutParams as ConstraintLayout.LayoutParams
            val param3 = ivGhostBtn.layoutParams as ConstraintLayout.LayoutParams

            if (Prefs.init().isLHPost) {
                param2.also {

                    it.startToEnd = glVerticalStart.id
                    it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                param1.also {
                    it.endToStart = glVerticalEnd.id
                    it.startToEnd = postActivity.id
                    it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                param3.also {
                    it.startToEnd = postActivity.id
                    it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                profileLayout.layoutParams = param1
                postActivity.layoutParams = param2
                ivGhostBtn.layoutParams = param3
                profileLayout.gravity = Gravity.END
                desc.gravity = Gravity.END
                profileInfoContainer.gravity = Gravity.END
                container.gravity = Gravity.END
            } else {
                param2.also {

                    it.endToStart = glVerticalEnd.id
                    it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                param1.also {
                    it.startToEnd = glVerticalStart.id
                    it.endToStart = postActivity.id
                    it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                param3.also {
                    it.endToStart = postActivity.id
                    it.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                }
                profileLayout.layoutParams = param1
                postActivity.layoutParams = param2
                ivGhostBtn.layoutParams = param3
                profileLayout.gravity = Gravity.START
                desc.gravity = Gravity.START
                profileInfoContainer.gravity = Gravity.START
                container.gravity = Gravity.START
            }
        }
    }

    fun reverseVideoItem(homeListItemBinding: HomeListVideoItemBinding) {
        homeListItemBinding.apply {
            val constraintSet = ConstraintSet()
            constraintSet.also {
                it.clone(constraintlayout)

                // Remove horizontal constraints for the button
                it.clear(postActivity.id, ConstraintSet.END)
                it.clear(postActivity.id, ConstraintSet.START)

                // Remove vertical constraints for the button
                it.clear(profileLayout.id, ConstraintSet.TOP)
                it.clear(profileLayout.id, ConstraintSet.START)
                it.clear(profileLayout.id, ConstraintSet.END)
                it.clear(ivGhostBtn.id, ConstraintSet.END)
                it.clear(ivGhostBtn.id, ConstraintSet.START)

                it.applyTo(constraintlayout)
            }


            val param1 = profileLayout.layoutParams as ConstraintLayout.LayoutParams
            val param2 = postActivity.layoutParams as ConstraintLayout.LayoutParams
            val param3 = ivGhostBtn.layoutParams as ConstraintLayout.LayoutParams
            if (Prefs.init().isLHPost) {
                param2.also {

                    it.startToEnd = glVerticalStart.id
                    it.bottomToTop = seekBarMainLayout.id
                }
                param1.also {
                    it.endToStart = glVerticalEnd.id
                    it.startToEnd = postActivity.id
                }
                param3.also {
                    it.startToEnd = postActivity.id
                    it.bottomToTop = seekBarMainLayout.id
                }
                profileLayout.layoutParams = param1
                postActivity.layoutParams = param2
                ivGhostBtn.layoutParams = param3
                profileLayout.gravity = Gravity.END
                desc.gravity = Gravity.END
                profileInfoContainer.gravity = Gravity.END
                container.gravity = Gravity.END
            } else {
                param2.also {

                    it.endToStart = glVerticalEnd.id
                    it.bottomToTop = seekBarMainLayout.id
                }
                param1.also {
                    it.startToEnd = glVerticalStart.id
                    it.endToStart = postActivity.id
                }
                param3.also {
                    it.endToStart = postActivity.id
                    it.bottomToTop = seekBarMainLayout.id
                }
                profileLayout.layoutParams = param1
                postActivity.layoutParams = param2
                ivGhostBtn.layoutParams = param3
                profileLayout.gravity = Gravity.START
                desc.gravity = Gravity.START
                profileInfoContainer.gravity = Gravity.START
                container.gravity = Gravity.START
            }
        }
    }

    fun reverseLinearLayout(linearLayout: LinearLayout) {
        val profileContainerCount = linearLayout.childCount
        for (i in 0 until profileContainerCount / 2) {
            val temp = linearLayout.getChildAt(i)
            linearLayout.removeView(temp)
            linearLayout.addView(temp, profileContainerCount - 1 - i)
        }
    }

    private fun addImageView(
        textView: TextView,
        duration: Long,
        mainLayout: ConstraintLayout,
        x: Float,
        y: Float
    ) {
        if (textView.parent == null) {
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

//            textView.setBackgroundColor(Color.BLUE)
            textView.layoutParams = params
            textView.elevation = 5f
            textView.setShadowLayer(10f, 0f, 0f, Color.BLACK)
            textView.x = (mainLayout.width / 2).toFloat() - 50
            textView.y = y - 100
            mainLayout.addView(textView, 0)
            val typeface = ResourcesCompat.getFont(textView.context, R.font.poppins)
            textView.typeface = typeface
        }

        val totalSeconds = (duration / 1000)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val formattedTime = String.format("%d:%02d", minutes, seconds)


        textView.setTextColor(textView.context.getColor(R.color.pureWhite))
        textView.text = formattedTime

        // Update the position after setting the text
        textView.x = (mainLayout.width / 2).toFloat() - 50

    }


    private fun getId(position: Int, text: String): String {

        val mentionUser = list[position]?.mentionUsers?.filter { it.username == text }
        return mentionUser?.get(0)?.username ?: ""
    }

    private fun getIds(description: String): List<String> {
        val regexForIds = """\([a-zA-Z0-9]+\)""".toRegex()
        val idMatches = regexForIds.findAll(description)
        return idMatches.map { it.value.substring(1, it.value.length - 1) }.toList()
    }


    //create dot slider for mutliple images
    private fun createDotSlider(binding: HomeListItemBinding, imageCount: Int, context: Context) {

        binding.sliderDots.removeAllViews()
        val dots = arrayOfNulls<ImageView>(imageCount)
        for (i in 0 until imageCount) {
            dots[i] = ImageView(context)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.inactive_dot
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            binding.sliderDots.addView(dots[i], params)
        }
        dots[0]?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot))
        binding.imagePager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until imageCount) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.inactive_dot
                        )
                    )
                }

                try {
                    dots[position]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.active_dot
                        )
                    )
                } catch (e: Exception) {
                }

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    inner class ViewHolder(itemView: HomeListItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        var binding: HomeListItemBinding = itemView
    }

    inner class AdHolder(itemView: HomeListAdBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        var binding = itemView

    }

    class VideoViewHolder(
        val binding: HomeListVideoItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val AD_TYPE = 2
        private const val CONTENT_TYPE = 1
        private const val VIDEO = 3
        private const val PAGINATION = 10
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] == null) {
            AD_TYPE
        } else if (list[position]?.type == "image") {
            CONTENT_TYPE
        } else {
            VIDEO
        }
    }

    fun setNewItems(newItems: ArrayList<Data?>) {
        list = newItems
        notifyDataSetChanged()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addNewAd() {
        if (adView != null) {
            val adRequest = AdRequest.Builder().build()
            GlobalScope.launch(Dispatchers.Main) {
                adView?.loadAd(adRequest)
                adView?.visibility = View.VISIBLE
            }
        }
    }

    fun addNewItem(item: Data) {
        list.add(item)
        notifyItemInserted(list.size - 1)
    }

    fun addNewItems(newItems: ArrayList<Data?>) {
        val oldPos = list.size + 1

        list.addAll(newItems)
        notifyItemRangeInserted(oldPos, newItems.size)
        /*if (newItems.size > 0) {
            if (list.filterNotNull().size % 10 != 0) {
                newItems.forEachIndexed { index, data ->
                    if (!list.contains(data)) {
                        list.add(data)
                        if (index == 2) {
                            //list.add(null)
                        }
                    }
                }
            } else {
                list.addAll(newItems)
            }
            notifyItemRangeInserted(oldPos, newItems.size)
        }*/
    }

    fun clear() {
//        val size: Int = list.size
        list.clear()
        notifyDataSetChanged()
    }

    fun doubleTapLike(position: Int) {
        list[position]?.isLike = true
        list[position]?.noOfLikes = list[position]?.noOfLikes!! + 1
        list[position]?.creator?.overallLikes = list[position]?.creator?.overallLikes?.plus(1) ?: 0
    }


    @androidx.annotation.OptIn(UnstableApi::class)
    @SuppressLint("ClickableViewAccessibility")
    private fun setPlayerTouchListener(
        position: Int,
        context: Context,
        postId: String,
        imgView: ImageView,
        textView: TextView,
        overallLike: TextView,
        playerView: PlayerView,
        homeListItemBinding: HomeListVideoItemBinding
    ) {
        val gestureDetectorCompat =
            GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (Utils.isInternetAvailables(imgView.context)) {
                        imgView.disableMultiTap(1500)
                        val location2 = IntArray(2)
                        textView.getLocationOnScreen(location2)

                        val x2 = location2[0]
                        val y2 = location2[1]

                        addViewAndAnimate(
                            e.x,
                            e.y,
                            imgView.context,
                            homeListItemBinding.constraintlayout,
                            homeListItemBinding.postActivity
                        )
                        Log.e("asasdffgdf", "likev")
                        EventBus.getDefault()
                            .post(
                                isDoubleTap(
                                    position,
                                    e.x,
                                    e.y,
                                    postId,
                                    imgView,
                                    textView,
                                    overallLike,
                                    x2.toFloat(),
                                    y2.toFloat()
                                )
                            )

                    } else {
                        Utils.showToast(imgView.context.getString(R.string.no_internet_message))
                    }

                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    playerView.player?.playWhenReady = !playerView.player!!.playWhenReady
                    if (playerView.player?.playWhenReady == true) {
                        homeListItemBinding.playBigButton.visibility = View.GONE
                    } else {
                        homeListItemBinding.playBigButton.visibility = View.VISIBLE
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    try {
                        CoroutineScope(Dispatchers.IO).launch {
                            ifUploadedThenRun(list[position]?.postId ?: "") {
                                EventBus.getDefault().post(isInnerPageScroll(false))
                                EventBus.getDefault().post(HomePagerScroll(false))
                                clickListener.tapAndHoldOptionMenu(
                                    position,
                                    list[position]!!.options
                                )
                            }
                        }
                    } catch (_: Exception) {
                    }

                }

            })

        playerView.hideController()
        playerView.setOnTouchListener { _, p1 ->
            gestureDetectorCompat.onTouchEvent(p1)
            when (p1.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    Log.e("onTouchImage", p1.pointerCount.toString())
                    if (p1.pointerCount >= 2) {
                        // Two or more fingers are down, start the long press timer
                        handler.postDelayed({
                            if (p1.pointerCount == 2) {
                                Log.e("onTwoFingerTouchAndHold", "send")
                                Log.e("onTwoFingerTouchAndHold", "true")
                                isLHView = !isLHView
                                Prefs.init().isLHPost = !Prefs.init().isLHPost

                                reverseVideoItem(homeListItemBinding)
                                reverseLinearLayout(homeListItemBinding.profileInfoContainer)
                                reverseLinearLayout(homeListItemBinding.profileNameInfoContainer)
                                Utils.vibrateOnce(context, 1, 50)
                                for (i in 0 until itemCount) {
                                    if (i != position) {
                                        notifyItemChanged(i)
                                    }
                                }
                            }
                        }, 1000)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_MOVE -> {
                    if (p1.actionMasked == MotionEvent.ACTION_UP) {
                        EventBus.getDefault().post(isInnerPageScroll(true))
                        EventBus.getDefault().post(HomePagerScroll(true))
                    }

                    if (p1.pointerCount < 2) {
                        Log.e("onTwoFingerTouchAndHold", "remove")
                        // Less than two fingers are down, cancel the long press timer
                        handler.removeCallbacksAndMessages(null)
                        handlerOneFinger.removeCallbacksAndMessages(null)
                    }

                }

                else -> {
                    handler.removeCallbacksAndMessages(null)
                    handlerOneFinger.removeCallbacksAndMessages(null)
                }
            }

            true
        }
    }

    private fun addViewAndAnimate(
        x: Float, y: Float, context: Context,
        constraintLayout: ConstraintLayout,
        targetView: View
    ) {
        val location2 = IntArray(2)
        targetView.getLocationOnScreen(location2)

        val x2 = location2[0]
        val y2 = location2[1]
        val newView = ImageView(context) // Use your context here
        newView.setImageResource(R.drawable.double_tap_like) // Set the image resource or background as needed

        val layoutParams = ConstraintLayout.LayoutParams(
            300,
            300
        )
        newView.layoutParams = layoutParams

        val parentLayout = constraintLayout
        parentLayout.addView(newView)

        // Set the new view's position at the touch coordinates

        newView.x = x - 300 / 2
        newView.y = y - 300 / 2




        Utils.printLog(
            tag = "sagh",
            msg = targetView.x.toString() + "   " + targetView.y.toString()
        )
        Utils.printLog(
            tag = "sagh",
            msg = targetView.x.toString() + "   " + targetView.y.toString()
        )
        Utils.printLog(
            tag = "sagh",
            msg = targetView.width.toString() + "   " + targetView.height.toString()
        )
        val targetX = targetView.x + (targetView.width / 2) - (300 / 2)
        val scaleX = targetView.width.toFloat() / 300
//        val h = 300*scaleX
        val targetY =
            targetView.y - targetView.width // Placing new view at the top of the target view

//        val scaleY = targetView.height.toFloat() / 300
        val translationY = targetY - newView.y
        newView.animate()
            .translationX(targetX.toFloat())
            .translationY(targetY - 11.0f)
            .scaleX(scaleX)
            .scaleY(scaleX)
            .setDuration(500)
            .start()
        Utils.runWithDelay(500) {
            parentLayout.removeView(newView)
        }
    }

    interface ClickListener {
        fun likeClick(position: Int)
        fun commentClick(position: Int, postId: String, view: TextView)
        fun shareClick(position: Int, textView: TextView)
        fun saveClick(position: Int)
        fun saveSoundClick(position: Int)
        fun followUnfollowClick(position: Int)
        fun tagClick(position: Int)
        fun tapAndHoldOptionMenu(position: Int, options: Options)
        fun tapAndHoldOptionMenu(position: Int, imagePosition: Int, options: Options)
        fun hashTagClick(text: String)
        fun mentionClick(text: String)
    }

    override fun hashTagClick(text: String) {
        clickListener.hashTagClick(text)
    }

    override fun mentionClick(text: String) {
        clickListener.mentionClick(text)
    }

}