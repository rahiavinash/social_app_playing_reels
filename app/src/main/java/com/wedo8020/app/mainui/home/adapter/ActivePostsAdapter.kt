package com.wedo8020.app.mainui.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.base.PlayerViewAdapter.Companion.getBitmap
import com.wedo8020.app.databinding.LinkedAccountRvListBinding
import com.wedo8020.app.databinding.PostRecycleviewBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.User
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsync
import com.wedo8020.app.utils.Utils.loadImageMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivePostsAdapter (override val layoutId: Int, private val onClickListener: OnClickListener , val otherProfile: Boolean = false,val context: Context):
    BaseRecyclerAdapter<PostRecycleviewBinding, Data>() {
    var selectedPosition = -1
    var longPressTypes = ""
    var longPress = false
    private val postDb by lazy {
        CreatePostDbCreation(context)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(holder: ViewHolder, item: Data, position: Int) {
        if (!list[position].pinTime.isNullOrEmpty()) {
            holder.binding.ivPPin.visibility = View.VISIBLE
        } else {
            holder.binding.ivPPin.visibility = View.GONE
        }

        /** set the video and image */
        if (list[position].type == "video") {
            holder.binding.ivImageIcon.visibility = View.GONE
            holder.binding.playCount.visibility = View.VISIBLE
            holder.binding.playCount.text = list[position].noOfViews.toString()
            if (!list.isNullOrEmpty()) {
                if (list.size > position) {
//                    try {
//                        list[position].videoThumbnail?.let {
//                            CoroutineScope(Dispatchers.Main).launch {
//                                if (list[position].videoThumbnail.isNullOrEmpty()){
//                                    PlayerViewAdapter.getBitmap(list[position].seekTime.toLong(),holder.view.ivPost,list[position].video?:"")
//                                }else{
//                                    holder.view.ivPost.loadImageAsync(
//                                        it
//                                    )
//
//
//                                }
//                            }
//                        }
//                    } catch (e: Exception) {
//
//                    }
                    if (!list[position].videoThumbnail.isNullOrEmpty()) {
                        list[position].videoThumbnail.let {
                            CoroutineScope(Dispatchers.IO).launch {
                                if (it != null) {
                                    Log.d("videoThumbnailIs", it.toString())
                                    holder.binding.ivPost.loadImageAsync(it)
                                }
                            }
                        }

                    }
                    else{
                        Log.d("thumbnailEmpty", "Ur database thumbnail ${list[position].seekTime} video url is ${list[position].video}")
                        CoroutineScope(Dispatchers.Main).launch {
//                                val data = PlayerViewAdapter.getBitmap(
//                                    list[position].seekTime.toDouble(),
//                                    holder.view.ivPost,
//                                    list[position].video ?: ""
//                                )
                            try {
                                getBitmap(list[position].seekTime.toDouble(), holder.binding.ivPost, list[position].video ?: "")
                            } catch (e: Exception) {
                                Log.d("thumbnailEmpty", "Ur database thumbnail exception at list ${e.message}")
                                e.printStackTrace()
                            }


                        }
                    }



                }
            }


        } else if (list[position].type == "image") {

                if (list[position].images!!.size > 1) {
                    holder.binding.ivImageIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            MainApplication.get().applicationContext,
                            R.drawable.ic_multiimage
                        )
                    )
                } else {
                    holder.binding.ivImageIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            MainApplication.get().applicationContext,
                            R.drawable.ic_uploading_image
                        )
                    )
                }
                holder.binding.playCount.text = list[position].noOfViews.toString()
                if (position < list.size ) {
                    Log.e("isNullOrEmpty" , list[position].images.toString())
                    try {
                    if (!list[position].images.isNullOrEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.binding.ivPost.loadImageAsync(
                                    if (!list.isNullOrEmpty()){
                                        list[position].images?.get(0) ?: ""
                                       /* if ((list[position].images?.size ?: -1) ==0){
                                            list[position].images?.get(0) ?: ""
                                        }else {
                                            ""
                                        }*/
                                    }else {""}
                                )

//                        PlayerViewAdapter.getBitmap(list[position].seekTime.toLong(),holder.view.ivPost,list[position].video?:"")

                            }


                    }else{
                        Log.e("longPressTypes", "isEmpty")
                    }
                    }catch (e:IndexOutOfBoundsException){
                        Log.e("longPressTypes", "isEmpty")
                    }
                }
        }

        Log.e("longPressTypes", longPressTypes.toString())
        /** Handle LongPress Feature for Every Post Type*/
        if (longPressTypes == "save") {
            holder.binding.ivRoundEdit.visibility = View.GONE
            holder.binding.ivRoundArchive.visibility = View.GONE
        } else if (longPressTypes == "archive") {
            holder.binding.ivRoundArchive.visibility = View.GONE
            holder.binding.ivRoundUnArchive.visibility = View.VISIBLE
        }
        /** Handle The UnPin Functionality Feature for Every Post Type*/
        /*    holder.view.let {
                if (it.ivPPin.visibility == View.VISIBLE) {
                    it.ivRoundUnPin.visibility = View.VISIBLE
                    it.ivPPin.visibility = View.GONE
                } else {
                    it.ivRoundUnPin.visibility = View.GONE
                    it.ivPPin.visibility = View.VISIBLE
                }
            }*/


        /** Handle the LongPress*/
        holder.binding.clProfileCardView.setOnLongClickListener {
            Log.e("sASAsASAsAS" , selectedPosition.toString())
            Log.e("sASAsASAsAS" , position.toString())
            CoroutineScope(Dispatchers.IO).launch {
                if (!list.isNullOrEmpty() && position<list.size){
                        ifUploadedThenRun(list[position]._id?:""){
                            holder.binding.clProfileCardView.disableMultiTap()
                            if (!otherProfile) {
                                longPress = true
                                if (selectedPosition != position) {
                                    // Deselect the previously selected item (if any)
                                    if (selectedPosition != -1 && selectedPosition < list.size) {
                                        list[selectedPosition].isSelect = false
                                        notifyItemChanged(selectedPosition)
                                    }

                                    // Select the clicked item if it's a valid position
                                    if (position >= 0 && position < list.size) {
                                        selectedPosition = position
                                        list[selectedPosition].isSelect = true
                                        notifyItemChanged(selectedPosition)
                                    } else {
                                        // Handle the case when the clicked position is not valid (e.g., item was removed)
                                        selectedPosition = -1
                                    }
                                }


                                holder.binding.let { it ->
                                    if (it.ivRoundPin.visibility == View.VISIBLE) {
                                        it.clLongClick.visibility = View.GONE
                                        it.playCount.visibility = View.VISIBLE
                                        if (list[position].type == "video") {
                                            it.ivImageIcon.visibility = View.GONE

                                        } else if (list[position].type == "image") {
                                            holder.binding.ivImageIcon.visibility = View.VISIBLE

                                            if ((list[position].images?.size ?: 0) > 1) {
                                                holder.binding.ivImageIcon.setImageDrawable(
                                                    ContextCompat.getDrawable(
                                                        MainApplication.get().applicationContext,
                                                        R.drawable.ic_multiimage
                                                    )
                                                )
                                            } else {
                                                holder.binding.ivImageIcon.setImageDrawable(
                                                    ContextCompat.getDrawable(
                                                        MainApplication.get().applicationContext,
                                                        R.drawable.ic_uploading_image
                                                    )
                                                )
                                            }

                                        }
                                        it.ivPlay.visibility = View.VISIBLE
                                    }

                                }
                                onClickListener.onLongPressClick(it, position, list[position])
                            }
                        }

                }
            }

            true
        }

        if (list[position].isSelect) {
            //  selectedPosition = position
            holder.binding.clLongClick.visibility = View.VISIBLE

            if (holder.binding.ivPPin.visibility == View.VISIBLE) {
                holder.binding.ivRoundUnPin.visibility = View.VISIBLE
                holder.binding.ivPPin.visibility = View.GONE
            } else {
                holder.binding.ivRoundUnPin.visibility = View.GONE
                holder.binding.ivPPin.visibility = View.VISIBLE
            }

            holder.binding.playCount.visibility = View.GONE
            holder.binding.ivImageIcon.visibility = View.GONE

            holder.binding.ivPlay.visibility = View.INVISIBLE
            holder.binding.ivPPin.visibility = View.GONE
        } else {
            holder.binding.clLongClick.visibility = View.GONE

            holder.binding.ivPPin.visibility = View.VISIBLE
            holder.binding.playCount.visibility = View.VISIBLE
            if (list[position].type == "video") {
                holder.binding.ivImageIcon.visibility = View.GONE

            } else if (list[position].type == "image") {
                holder.binding.ivImageIcon.visibility = View.VISIBLE


                if (list[position].images?.size ?: 0 > 1) {
                    holder.binding.ivImageIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            MainApplication.get().applicationContext,
                            R.drawable.ic_multiimage
                        )
                    )
                } else {
                    holder.binding.ivImageIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            MainApplication.get().applicationContext,
                            R.drawable.ic_uploading_image
                        )
                    )
                }
            }
            if (list[position].pinTime != null) {
                holder.binding.ivPPin.visibility = View.VISIBLE
            } else {
                holder.binding.ivPPin.visibility = View.GONE
            }
        }


        holder.binding.clProfileCardView.setOnClickListener {
            holder.binding.clProfileCardView.disableMultiTap()
            holder.binding.clLongClick.visibility = View.GONE

            if (!list.isNullOrEmpty() && position<list.size) {
                if (longPress) {
                    list[selectedPosition].isSelect = false
                    selectedPosition = -1
                    longPress = false
                } else {
                    onClickListener.onItemClick(position, list[position])
                }

                holder.binding.playCount.visibility = View.VISIBLE
                if (list[position].type == "video") {
                    holder.binding.ivImageIcon.visibility = View.GONE

                } else if (list[position].type == "image") {
                    holder.binding.ivImageIcon.visibility = View.VISIBLE


                    if (list[position].images?.size ?: 0 > 1) {
                        holder.binding.ivImageIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                MainApplication.get().applicationContext,
                                R.drawable.ic_multiimage
                            )
                        )
                    } else {
                        holder.binding.ivImageIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                MainApplication.get().applicationContext,
                                R.drawable.ic_uploading_image
                            )
                        )
                    }
                }
                holder.binding.ivPlay.visibility = View.VISIBLE
                /** Handle Pin post for the single and double tap on post*/
                if (list[position].pinTime != null) {
                    holder.binding.ivPPin.visibility = View.VISIBLE
                } else {
                    holder.binding.ivPPin.visibility = View.GONE
                }
            }
        }


        holder.binding.ivRoundPin.setOnClickListener {
            holder.binding.ivRoundPin.disableMultiTap()
            holder.binding.clLongClick.visibility = View.GONE
            onClickListener.onClickPin(it, position, list[position], true)

        }

        holder.binding.ivRoundEdit.setOnClickListener {
            holder.binding.ivRoundEdit.disableMultiTap()
            onClickListener.onClickEdit(it, position, list[position])
        }

        holder.binding.ivRoundDelete.setOnClickListener {
            holder.binding.ivRoundDelete.disableMultiTap()
            onClickListener.onDeletePost(it, position, list[position])
            holder.binding.clLongClick.visibility = View.GONE
        }
        holder.binding.ivRoundArchive.setOnClickListener {
            holder.binding.ivRoundArchive.disableMultiTap()
            onClickListener.onArchive(it, position, list[position])
        }
        holder.binding.ivRoundUnArchive.setOnClickListener {
            holder.binding.ivRoundUnArchive.disableMultiTap()
            onClickListener.onArchive(it, position, list[position])
        }
    }
    interface OnClickListener {
        fun onItemClick(position: Int, data: Data)
        fun onLongPressClick(view: View, position: Int, data: Data)
        fun onClickEdit(view: View, position: Int, data: Data)
        fun onArchive(view: View, position: Int, data: Data)
        fun onDeletePost(view: View, position: Int, data: Data)
        fun onClickPin(view: View, position: Int, data: Data, click: Boolean)


    }


    private suspend fun ifUploadedThenRun(postId: String, run: (() -> Unit)) {
        withContext(Dispatchers.IO){
            if (postDb.getCreatePostData().fetchPost(postId).isNotEmpty()) {
                withContext(Dispatchers.Main){
                    Utils.showToast("Still Processing Upload...")
                }
            } else {
                withContext(Dispatchers.Main){
                    run()
                }
            }
        }
    }

}