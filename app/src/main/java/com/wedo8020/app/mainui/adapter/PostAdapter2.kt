package com.wedo8020.app.mainui.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.wedo8020.app.R
import com.wedo8020.app.databinding.DemoLayoutBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.Data
import com.wedo8020.app.utils.Hashtag
import com.wedo8020.app.utils.Prefs
import java.util.regex.Matcher
import java.util.regex.Pattern

class PostAdapter2(override val layoutId: Int, val clickListener: PostAdapter.ClickListener) :
    BaseRecyclerAdapter<DemoLayoutBinding, Data?>(), Hashtag.onHashtagClick {
    private var isLHView = Prefs.init().isLHPost
    private val handler = Handler(Looper.getMainLooper())
    override fun bind(viewHolder: ViewHolder, item: Data?, position: Int) {
       /* viewHolder.binding.apply {


            val item = list[position]
//                    Toast.makeText(viewHolder.itemView.context,"as",Toast.LENGTH_SHORT).show()

            *//*if (list[position]!!.type == "video") {
                sliderDots.visibility = View.GONE
                imagePager.visibility = View.GONE
                if (position == 0) {
                    PlayerViewAdapter.setMediaSourceFromFrag(
                        item!!.video,
                        viewHolder.binding.playerView
                    ) {
                        PlayerViewAdapter.playCurrentPlayingVideo()
                    }

                }

                val imageView = TextView(viewHolder.itemView.context)
                seekBar.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            playerView.player!!.playWhenReady = false
                        }             // Hide the thumb when touched

                        MotionEvent.ACTION_MOVE -> {
                            addImageView(
                                imageView,
                                seekBar.progress.toLong(),
                                constraintlayout,
                                event.x,
                                v.y
                            )

                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                            constraintlayout.removeView(imageView)
                            playerView.player!!.playWhenReady = true
                            seekBar.thumb = null// Show the thumb when released or canceled
                            // Replace with your thumb drawable
                        }

                    }
                    false
                }

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        playerView.player!!.seekTo(seekBar.progress.toLong())
                    }
                })

                setPlayerTouchListener(
                    position, viewHolder.itemView.context,
                    item!!.postId,
                    likeBtn,
                    totalPostLike,
                    playerView, this
                )
            }
            else {
                playerView.visibility = View.GONE
                seekBar.visibility = View.GONE

                val viewPagerAdapter = ImageSlide(
                    viewHolder.itemView.context,
                    list[position]!!.images,
                    object : ImageSlide.GestureListener {
                        override fun onDoubleTap(x: Float, y: Float) {
                            val location2 = IntArray(2)
                            likeBtn.getLocationOnScreen(location2)

                            val x2 = location2[0]
                            val y2 = location2[1]
                            val xInConstraintLayout = x2 - likeBtn.left
                            val yInConstraintLayout = y2 - likeBtn.right
                            EventBus.getDefault().post(
                                isDoubleTap(
                                    position,
                                    x,
                                    y + 250,
                                    list[position]!!.postId,
                                    likeBtn,
                                    totalPostLike,
                                    xInConstraintLayout.toFloat(),
                                    yInConstraintLayout.toFloat()
                                )
                            )

                        }

                        override fun onTwoFingerTouchAndHold() {
                            Log.e("onTwoFingerTouchAndHold", "true")
                            isLHView = !isLHView
                            Prefs.init().isLHPost = !Prefs.init().isLHPost
                            *//**//* if (!isLHView){*//**//*
                            //reverse swap constraint
                            reverseVideoItem(this@apply)
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
                            clickListener.tapAndHoldOptionMenu(
                                position,
                                imagePager.currentItem,
                                list[position]!!.options
                            )
                        }
                    })

                imagePager.adapter = viewPagerAdapter
                if (list[position]!!.images.size > 1) {
                    createDotSlider(
                        this,
                        list[position]!!.images.size,
                        viewHolder.itemView.context
                    )
                }
            }*//*
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

            //save sound
            *//*if (item!!.isSoundSaved) {
                saveSoundBtn.imageTintList = ColorStateList.valueOf(
                    viewHolder.itemView.context.getColor(R.color.RedColor)
                )
            } else {
                saveSoundBtn.imageTintList = ColorStateList.valueOf(
                    viewHolder.itemView.context.getColor(R.color.pureWhite)
                )
            }*//*

            //like count visibility
            totalPostLike.visibility = if (item!!.options.like) {
                View.VISIBLE
            } else {
                View.GONE
            }

            totalPostLike.text = item.noOfLikes.toString()

            //comment button visibility
            commentBtn.visibility = if (item.options.comment) {
                View.VISIBLE
            } else {
                View.GONE
            }

            commentCount.text = item.noOfComments.toString()

            //save post
            saveBtn.setImageResource(
                if (item.isSaved) {
                    R.drawable.ic_savepost
                } else {
                    R.drawable.ic_unsavepost
                }
            )

            //like post
            likeBtn.setImageResource(
                if (item.isLike) {
                    R.drawable.ic_like
                } else {
                    R.drawable.ic_unlike
                }
            )

            //follow status handling
            when (item.creator.followStatus) {
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

            saveBtn.setOnClickListener {
                saveBtn.setImageResource(
                    if (item.isSaved) {
                        R.drawable.ic_unsavepost
                    } else {
                        R.drawable.ic_savepost
                    }
                )

                clickListener.saveClick(position)
            }

            *//*saveSoundBtn.setOnClickListener {
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
            }*//*

            shareBtn.setOnClickListener {
                clickListener.shareClick(position)
            }

            likeBtn.setOnClickListener {
                if (list[position]!!.isLike) {
                    likeBtn.setImageResource(R.drawable.ic_unlike)
                    list[position]!!.noOfLikes = list[position]!!.noOfLikes!! - 1
                    totalPostLike.text = list[position]!!.noOfLikes.toString()
                } else {
                    likeBtn.setImageResource(R.drawable.ic_like)
                    list[position]!!.noOfLikes = list[position]!!.noOfLikes!! + 1
                    totalPostLike.text = list[position]!!.noOfLikes.toString()

                }
                clickListener.likeClick(position)
            }

            commentBtn.setOnClickListener {
                clickListener.commentClick(position, item.postId)
            }

            followContainer.setOnClickListener {
                when (item.creator.followStatus) {
                    1 -> {
                        followStatus.text =
                            viewHolder.itemView.context.getText(R.string.follow)
                    }

                    2 -> {
                        if (item.creator.accountType.equals(
                                "private",
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
                if (item.creator.followStatus != 3) {
                    clickListener.followUnfollowClick(position)
                }
            }


            tagContainer.visibility = if (item.tagUsers.size > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }

            tagContainer.setOnClickListener {
                clickListener.tagClick(position)
            }

            profileImg.setOnClickListener {
                clickListener.mentionClick(item.creator._id)
            }

            username.setOnClickListener {
                clickListener.mentionClick(item.creator._id)
            }

            username.text = item.creator.username

            CoroutineScope(Dispatchers.IO).launch {
                profileImg.loadImageAsync(item.creator.profile)
            }

            if (!item.description.isNullOrEmpty()) {
                val usernames = Utils.getUsernames(item.description ?: "")
                val description = Utils.convertDesc(item.description ?: "")
                Log.e("usernameslist", usernames.toString())
                val ss = SpannableString(description)
                val hashtagSpans1: ArrayList<IntArray>? = Utils.getSpans(description, '#')
                val calloutSpans1: ArrayList<IntArray>? = Utils.getSpans(description, '@')
                Utils.setSpanComment(
                    ss,
                    hashtagSpans1!!,
                    viewHolder.itemView.context,
                    this@PostAdapter2
                )
                Utils.setSpanUname(
                    ss,
                    calloutSpans1!!,
                    this@PostAdapter2,
                    item.mentionUsers
                )
                desc.visibility = View.VISIBLE
                desc.movementMethod = LinkMovementMethod.getInstance()
                desc.text = ss
            } else {
                desc.visibility = View.GONE
            }


        }*/
    }

    private fun createDotSlider(binding: DemoLayoutBinding, imageCount: Int, context: Context) {

        binding.sliderDots.removeAllViews()
        val dots = arrayOfNulls<ImageView>(imageCount)
        for (i in 0 until imageCount) {
            dots[i] = ImageView(context)
            dots[i]!!.setImageDrawable(
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
        dots[0]!!.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot))
        binding.imagePager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until imageCount) {
                    dots[i]!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.inactive_dot
                        )
                    )
                }

                try {
                    dots[position]!!.setImageDrawable(
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

    private fun videoGhostMode(binding: DemoLayoutBinding) {
        binding.apply {
            if (Prefs.init().isGhostMode) {
                profileLayout.alpha = 0.0f
                postActivity1.alpha = 0.0f
                tagContainer.alpha = 0.0f
                postActivityGhost.visibility = View.VISIBLE
            } else {
                profileLayout.alpha = 1.0f
                postActivity1.alpha = 1.0f
                tagContainer.alpha = 1.0f
                postActivityGhost.visibility = View.GONE
            }
        }
    }

    private fun reverseVideoItem(homeListItemBinding: DemoLayoutBinding) {
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
                it.applyTo(constraintlayout)
            }


            val param1 = profileLayout.layoutParams as ConstraintLayout.LayoutParams
            val param2 = postActivity.layoutParams as ConstraintLayout.LayoutParams
            if (Prefs.init().isLHPost) {
                param2.also {

                    it.startToEnd = glVerticalStart.id
                    it.bottomToTop = seekBar.id
                }
                param1.also {
                    it.endToStart = glVerticalEnd.id
                    it.startToEnd = postActivity.id
                }
                profileLayout.layoutParams = param1
                postActivity.layoutParams = param2
                profileLayout.gravity = Gravity.END
                desc.gravity = Gravity.END
                profileInfoContainer.gravity = Gravity.END
                container.gravity = Gravity.END
            } else {
                param2.also {

                    it.endToStart = glVerticalEnd.id
                    it.bottomToTop = seekBar.id
                }
                param1.also {
                    it.startToEnd = glVerticalStart.id
                    it.endToStart = postActivity.id
                }

                profileLayout.layoutParams = param1
                postActivity.layoutParams = param2
                profileLayout.gravity = Gravity.START
                desc.gravity = Gravity.START
                profileInfoContainer.gravity = Gravity.START
                container.gravity = Gravity.START
            }
        }
    }

    private fun reverseLinearLayout(linearLayout: LinearLayout) {
        val profileContainerCount = linearLayout.childCount
        for (i in 0 until profileContainerCount / 2) {
            val temp = linearLayout.getChildAt(i)
            linearLayout.removeView(temp)
            linearLayout.addView(temp, profileContainerCount - 1 - i)
        }
    }

    private fun addImageView(
        imageView: TextView,
        duration: Long,
        mainLayout: ConstraintLayout,
        x: Float,
        y: Float
    ) {
        if (imageView.parent == null) {
            val params = RelativeLayout.LayoutParams(
                mainLayout.width / 6,
                mainLayout.height / 6
            )

            imageView.layoutParams = params
            imageView.elevation = 5f
            imageView.x = x + 50
            imageView.y = y - 500
            mainLayout.addView(imageView, 0)
            val typeface = ResourcesCompat.getFont(imageView.context, R.font.poppins)
            imageView.typeface = typeface
        }
        val totalSeconds = (duration / 1000)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val formattedTime = String.format("%d:%02d", minutes, seconds)



        imageView.setTextColor(imageView.context.getColor(R.color.pureWhite))
        imageView.text = formattedTime
        imageView.x = x - 50
        imageView.y = y - 100
        // Set your image resource here

    }

    private fun getUsernames(description: String): ArrayList<String> {
        val mentions = ArrayList<String>()
        val regex = Regex("@\\[([^]]+)]\\(([^)]+)\\)")

        val matches = regex.findAll(description)
        matches.forEach { mention ->
            val mentionText = mention.groupValues[0]
            val mentionName = mentionText.substringAfter("[").substringBefore("]")
                .substringAfter("@")
            mentions.add(mentionName)
        }

        return mentions
        /* val regexPattern = """@\[([a-zA-Z]+)]\([a-zA-Z0-9]+\)""".toRegex()
         val usernames = ArrayList<String>()
         val matches = regexPattern.findAll(description)
         for (match in matches) {
             val username = match.groups[1]?.value
             if (username != null) {
                 usernames.add(username)
             }
         }
         return usernames*/
    }

    private fun getSpans(body: String?, prefix: Char): ArrayList<IntArray> {
        val spans = ArrayList<IntArray>()
        val pattern: Pattern = Pattern.compile("$prefix\\w+")
        val matcher: Matcher = pattern.matcher(body.toString())

        // Check all occurrences
        while (matcher.find()) {
            val currentSpan = IntArray(2)
            currentSpan[0] = matcher.start()
            currentSpan[1] = matcher.end()
            spans.add(currentSpan)
        }
        return spans


    }

    private fun setSpanComment(
        commentsContent: SpannableString,
        hashtagSpans: ArrayList<IntArray>, context: Context
    ) {
        for (i in hashtagSpans.indices) {
            val span = hashtagSpans[i]
            val hashTagStart = span[0]
            val hashTagEnd = span[1]
            commentsContent.setSpan(
                Hashtag( this,""),
                hashTagStart,
                hashTagEnd, 0
            )
        }
    }


    override fun hashTagClick(text: String) {

    }

    override fun mentionClick(user: String) {

    }

    /*@SuppressLint("ClickableViewAccessibility")
    private fun setPlayerTouchListener(
        position: Int,
        context: Context,
        postId: String,
        imgView: ImageView,
        textView: TextView,
        playerView: PlayerView,
        homeListItemBinding: DemoLayoutBinding
    ) {
        val gestureDetectorCompat =
            GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val location2 = IntArray(2)
                    imgView.getLocationOnScreen(location2)

                    val x2 = location2[0]
                    val y2 = location2[1]
                    val xInConstraintLayout = x2 - imgView.left
                    val yInConstraintLayout = y2 - imgView.right
                    EventBus.getDefault()
                        .post(
                            isDoubleTap(
                                position,
                                e.x,
                                e.y,
                                postId,
                                imgView,
                                textView,
                                xInConstraintLayout.toFloat(),
                                yInConstraintLayout.toFloat()
                            )
                        )
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    playerView.player!!.playWhenReady = !playerView.player!!.playWhenReady
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
//                    clickListener.tapAndHoldOptionMenu(position, list[position]!!.options)
                }

            })

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
                                *//* if (!isLHView){*//*
                                //reverse swap constraint
                                reverseVideoItem(homeListItemBinding)
                                //reverse profile info container
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
                    if (p1.pointerCount < 2) {
                        Log.e("onTwoFingerTouchAndHold", "remove")
                        // Less than two fingers are down, cancel the long press timer
                        handler.removeCallbacksAndMessages(null)
//                        handlerOneFinger.removeCallbacksAndMessages(null)
                    }

                }

                else -> {
                    handler.removeCallbacksAndMessages(null)
//                    handlerOneFinger.removeCallbacksAndMessages(null)
                }
            }

            true
        }
    }*/
    fun doubleTapLike(position: Int) {
        list[position]!!.isLike = true
        list[position]!!.noOfLikes = list[position]!!.noOfLikes!! + 1
    }
}