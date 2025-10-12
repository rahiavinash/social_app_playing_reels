package com.wedo8020.app.mainui.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.PostRecycleviewBinding
import com.wedo8020.app.response.Data
import com.wedo8020.app.roomdb.CreatePostDbCreation
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivePostAdapter(
    var list: ArrayList<Data>,
    onClickListener: OnClickListener,
    val otherProfile: Boolean = false, val context: Context
) :
    RecyclerView.Adapter<ActivePostAdapter.ViewHolder>() {
    private val onClickListener: OnClickListener = onClickListener
    var selectedPosition = -1
    var longPressTypes = ""
    private var longPress = false
    private val postDb by lazy {
        CreatePostDbCreation(context)
    }

    class ViewHolder(val view: PostRecycleviewBinding) : RecyclerView.ViewHolder(view.root) {

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        if (!list[position].pinTime.isNullOrEmpty()) {
            holder.view.ivPPin.visibility = View.VISIBLE
        } else {
            holder.view.ivPPin.visibility = View.GONE
        }

        /** set the video and image */
        if (list[position].type == "video") {
            holder.view.ivImageIcon.visibility = View.GONE
            holder.view.playCount.visibility = View.VISIBLE
            holder.view.playCount.text = list[position].noOfViews.toString()
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
                                    holder.view.ivPost.loadImageAsync(it)
                                }
                            }
                        }

                    } else {
                        Log.d(
                            "thumbnailEmpty",
                            "Ur database thumbnail ${list[position].seekTime} video url is ${list[position].video}"
                        )
                        CoroutineScope(Dispatchers.Main).launch {
//                                val data = PlayerViewAdapter.getBitmap(
//                                    list[position].seekTime.toDouble(),
//                                    holder.view.ivPost,
//                                    list[position].video ?: ""
//                                )
                            try {
                                getBitmap(
                                    list[position].seekTime.toDouble(),
                                    holder.view.ivPost,
                                    list[position].video ?: ""
                                )
                            } catch (e: Exception) {
                                Log.d(
                                    "thumbnailEmpty",
                                    "Ur database thumbnail exception at list ${e.message}"
                                )
                                e.printStackTrace()
                            }


                        }
                    }


                }
            }


        } else if (list[position].type == "image") {
            if (list[position].images!!.size > 1) {
                holder.view.ivImageIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        MainApplication.get().applicationContext,
                        R.drawable.ic_multiimage
                    )
                )
            } else {
                holder.view.ivImageIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        MainApplication.get().applicationContext,
                        R.drawable.ic_uploading_image
                    )
                )
            }
            holder.view.playCount.text = list[position].noOfViews.toString()
            if (!list[position].images.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (!list[position].images.isNullOrEmpty()) {
//                        PlayerViewAdapter.getBitmap(list[position].seekTime.toLong(),holder.view.ivPost,list[position].video?:"")
                        holder.view.ivPost.loadImageAsync(list[position].images?.get(0) ?: "")
                    }

                }
            }
        }

        /** Handle LongPress Feature for Every Post Type*/
        if (longPressTypes == "save") {
            holder.view.ivRoundEdit.visibility = View.GONE
            holder.view.ivRoundArchive.visibility = View.GONE
        } else if (longPressTypes == "archive") {
            holder.view.ivRoundArchive.visibility = View.GONE
            holder.view.ivRoundUnArchive.visibility = View.VISIBLE
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
        holder.view.clProfileCardView.setOnLongClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                if (!list.isNullOrEmpty()) {


                    if (list.size > position) {
                        ifUploadedThenRun(list[position]._id ?: "") {
                            holder.view.clProfileCardView.disableMultiTap()
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


                                holder.view.let { it ->
                                    if (it.ivRoundPin.visibility == View.VISIBLE) {
                                        it.clLongClick.visibility = View.GONE
                                        it.playCount.visibility = View.VISIBLE
                                        if (list[position].type == "video") {
                                            it.ivImageIcon.visibility = View.GONE

                                        } else if (list[position].type == "image") {
                                            holder.view.ivImageIcon.visibility = View.VISIBLE

                                            if ((list[position].images?.size ?: 0) > 1) {
                                                holder.view.ivImageIcon.setImageDrawable(
                                                    ContextCompat.getDrawable(
                                                        MainApplication.get().applicationContext,
                                                        R.drawable.ic_multiimage
                                                    )
                                                )
                                            } else {
                                                holder.view.ivImageIcon.setImageDrawable(
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
            }

            true
        }

        if (list[position].isSelect) {
            //  selectedPosition = position
            holder.view.clLongClick.visibility = View.VISIBLE

            if (holder.view.ivPPin.visibility == View.VISIBLE) {
                holder.view.ivRoundUnPin.visibility = View.VISIBLE
                holder.view.ivPPin.visibility = View.GONE
            } else {
                holder.view.ivRoundUnPin.visibility = View.GONE
                holder.view.ivPPin.visibility = View.VISIBLE
            }

            holder.view.playCount.visibility = View.GONE
            holder.view.ivImageIcon.visibility = View.GONE

            holder.view.ivPlay.visibility = View.INVISIBLE
            holder.view.ivPPin.visibility = View.GONE
        } else {
            holder.view.clLongClick.visibility = View.GONE

            holder.view.ivPPin.visibility = View.VISIBLE
            holder.view.playCount.visibility = View.VISIBLE
            if (list[position].type == "video") {
                holder.view.ivImageIcon.visibility = View.GONE

            } else if (list[position].type == "image") {
                holder.view.ivImageIcon.visibility = View.VISIBLE


                if (list[position].images?.size ?: 0 > 1) {
                    holder.view.ivImageIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            MainApplication.get().applicationContext,
                            R.drawable.ic_multiimage
                        )
                    )
                } else {
                    holder.view.ivImageIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            MainApplication.get().applicationContext,
                            R.drawable.ic_uploading_image
                        )
                    )
                }
            }
            if (list[position].pinTime != null) {
                holder.view.ivPPin.visibility = View.VISIBLE
            } else {
                holder.view.ivPPin.visibility = View.GONE
            }
        }


        holder.view.clProfileCardView.setOnClickListener {
            holder.view.clProfileCardView.disableMultiTap()
            holder.view.clLongClick.visibility = View.GONE

            if (!list.isNullOrEmpty()) {
                if (longPress) {
                    list[selectedPosition].isSelect = false
                    selectedPosition = -1
                    longPress = false
                } else {
                    onClickListener.onItemClick(position, list[position])
                }

                holder.view.playCount.visibility = View.VISIBLE
                if (list[position].type == "video") {
                    holder.view.ivImageIcon.visibility = View.GONE

                } else if (list[position].type == "image") {
                    holder.view.ivImageIcon.visibility = View.VISIBLE


                    if (list[position].images?.size ?: 0 > 1) {
                        holder.view.ivImageIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                MainApplication.get().applicationContext,
                                R.drawable.ic_multiimage
                            )
                        )
                    } else {
                        holder.view.ivImageIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                MainApplication.get().applicationContext,
                                R.drawable.ic_uploading_image
                            )
                        )
                    }
                }
                holder.view.ivPlay.visibility = View.VISIBLE
                /** Handle Pin post for the single and double tap on post*/
                if (list[position].pinTime != null) {
                    holder.view.ivPPin.visibility = View.VISIBLE
                } else {
                    holder.view.ivPPin.visibility = View.GONE
                }
            }
        }


        holder.view.ivRoundPin.setOnClickListener {
            holder.view.ivRoundPin.disableMultiTap()
            holder.view.clLongClick.visibility = View.GONE
            onClickListener!!.onClickPin(it, position, list[position], true)

        }

        holder.view.ivRoundEdit.setOnClickListener {
            holder.view.ivRoundEdit.disableMultiTap()
            onClickListener!!.onClickEdit(it, position, list[position])
        }

        holder.view.ivRoundDelete.setOnClickListener {
            holder.view.ivRoundDelete.disableMultiTap()
            onClickListener.onDeletePost(it, position, list[position])
            holder.view.clLongClick.visibility = View.GONE
        }
        holder.view.ivRoundArchive.setOnClickListener {
            holder.view.ivRoundArchive.disableMultiTap()
            onClickListener?.onArchive(it, position, list[position])
        }
        holder.view.ivRoundUnArchive.setOnClickListener {
            holder.view.ivRoundUnArchive.disableMultiTap()
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

    suspend fun getBitmap(timeInSeconds: Double, imageView: ImageView, videoUrl: String) {
        val timeInMicroseconds = (timeInSeconds * 1000000).toLong()

        val bitmap = withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoUrl, HashMap<String, String>())

                retriever.getFrameAtTime(
                    timeInMicroseconds,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
            } catch (e: Exception) {
                Log.d("thumbnailEmpty", "Ur database thumbnail exception is  ${e.toString()}")
                null
            }
        }
        Log.d("thumbnailEmpty", "Ur database thumbnail bitmap  ${bitmap.toString()}")

        bitmap?.let {
            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(it)
            }
        }
    }


    /*    fun removeItem(position: Int) {
            if (position >= 0 && position < list.size) {
                list.removeAt(position)
                notifyItemRemoved(position)

                // Notify items from the removed position to the end of the list that their positions changed
                notifyItemRangeChanged(position, list.size - position)
            }
        }*/
    fun setNewItems(newItems: ArrayList<Data>) {
        list.clear()
        list.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addNewItems(newItems: ArrayList<Data>) {
        val oldPos = list.size + 1
        list.addAll(newItems)
        notifyItemRangeInserted(oldPos, newItems.size)
    }

    fun removeItem(index: Int) {
        Log.e("sasasa", index.toString())
        if (index != -1) {
            list.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, list.size)
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

}