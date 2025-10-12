package com.wedo8020.app.mainui.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.PostRecycleviewBinding
import com.wedo8020.app.response.ProfilePostData
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SavePostAdapter(
    var list: ArrayList<ProfilePostData>,
    onClickListener: OnClickListener,
    val otherProfile: Boolean = false
) :
    RecyclerView.Adapter<SavePostAdapter.ViewHolder>() {
    private val onClickListener: OnClickListener = onClickListener
    private var selectedPosition = -1
    var longPressTypes = ""
    private var longPress = false

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
            CoroutineScope(Dispatchers.Main).launch {
                holder.view.ivPost.loadImageAsync(list[position].videoThumbnail)
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
                    holder.view.ivPost.loadImageAsync(list[position].images?.get(0) ?: "")
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
            if (!otherProfile) {
                longPress = true
                if (selectedPosition != position) {
                    /** Deselect the previously selected item (if any)*/
                    if (selectedPosition != -1) {
                        list[selectedPosition].isSelect = false
                        notifyItemChanged(selectedPosition)
                    }
                    /** Select the clicked item*/
                    selectedPosition = position
                    list[selectedPosition].isSelect = true
                    notifyItemChanged(position)
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
            holder.view.clLongClick.visibility = View.GONE


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


        holder.view.ivRoundPin.setOnClickListener {
            holder.view.clLongClick.visibility = View.GONE
            onClickListener!!.onClickPin(it, position, list[position], true)

        }

        holder.view.ivRoundEdit.setOnClickListener {
            onClickListener!!.onClickEdit(it, position, list[position])
        }

        holder.view.ivRoundDelete.setOnClickListener {
            onClickListener.onDeletePost(it, position, list[position])
            holder.view.clLongClick.visibility = View.GONE
        }
        holder.view.ivRoundArchive.setOnClickListener {
            onClickListener.onArchive(it, position, list[position])
        }
        holder.view.ivRoundUnArchive.setOnClickListener {
            onClickListener.onArchive(it, position, list[position])
        }
    }

   /* fun addNewItems(newItems: ArrayList<Data>) {
        val oldPos = list.size + 1
        list.addAll(newItems)
        notifyItemRangeInserted(oldPos, newItems.size)
    }*/

    interface OnClickListener {
        fun onItemClick(position: Int, data: ProfilePostData)
        fun onLongPressClick(view: View, position: Int, data: ProfilePostData)
        fun onClickEdit(view: View, position: Int, data: ProfilePostData)
        fun onArchive(view: View, position: Int, data: ProfilePostData)
        fun onDeletePost(view: View, position: Int, data: ProfilePostData)
        fun onClickPin(view: View, position: Int, data: ProfilePostData, click: Boolean)


    }


}