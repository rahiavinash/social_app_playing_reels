package com.wedo8020.app.mainui.adapter

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.HashtagListItemBinding
import com.wedo8020.app.databinding.SearchAccountListItemBinding
import com.wedo8020.app.databinding.SearchDropsListItemBinding
import com.wedo8020.app.databinding.SearchSoundListItemBinding
import com.wedo8020.app.response.Data
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Hashtag
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchListAdapter(val type: String, val clickListeners: PostClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    Hashtag.onHashtagClick {
    var searchList = ArrayList<Data>()
    private var searchDropsListItemBinding: SearchDropsListItemBinding? = null
    private var soundListItemBinding: SearchSoundListItemBinding? = null
    private var hashtagListItemBinding: HashtagListItemBinding? = null
    private var searchAccountListItemBinding: SearchAccountListItemBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder: RecyclerView.ViewHolder?
        when (type) {
            "post" -> {
                searchDropsListItemBinding =
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.search_drops_list_item,
                        parent,
                        false
                    )
                viewHolder = DropViewHolder(searchDropsListItemBinding!!)
            }

            "sound" -> {
                soundListItemBinding =
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.search_sound_list_item,
                        parent,
                        false
                    )
                viewHolder = SoundViewHolder(soundListItemBinding!!)
            }

            "user" -> {
                searchAccountListItemBinding =
                    DataBindingUtil.inflate(
                        inflater,
                        R.layout.search_account_list_item,
                        parent,
                        false
                    )
                viewHolder = AccountViewHolder(searchAccountListItemBinding!!)
            }

            else -> {
                hashtagListItemBinding =
                    DataBindingUtil.inflate(inflater, R.layout.hashtag_list_item, parent, false)
                viewHolder = HashtagViewHolder(hashtagListItemBinding!!)
            }
        }
        return viewHolder
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = searchList[position]
        val context = holder.itemView.context
        when (type) {
            "post" -> {
                val viewHolder = holder as DropViewHolder
                viewHolder.binding.apply {
//                    item = data

                    username.text = data.creator.username
                    likeCount.text = (data.noOfViews ?: 0).toString()
                    CoroutineScope(Dispatchers.IO).launch {
                        if (data.type != null) {
                            (if (data.type == "video") {
                                data.videoThumbnail
                            } else {
                                if (!data.images.isNullOrEmpty()) {
                                    data.images!![0]
                                } else {
                                    ""
                                }
                            })?.let {
                                dropImage.loadImageAsync(
                                    it
                                )
                            }
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        userProfile.loadImageAsync(data.creator.profile)
                    }
                    if (!data.description.isNullOrEmpty()) {
                        val usernames = Utils.getUsernames(data.description)
                        val description = Utils.convertDesc(data.description)
                        Log.e("usernamesList", usernames.toString())
                        val ss = SpannableString(description)
                        val hashtagSpans1: ArrayList<IntArray> = Utils.getSpans(description, '#')
                        val callOutSpans1: ArrayList<IntArray> = Utils.getSpans(description, '@')
                        Utils.setSpanComment(
                            ss,
                            hashtagSpans1,
                            this@SearchListAdapter,
                            data.hashtags?.let { ArrayList(it) }?:ArrayList()
                        )
                        Utils.setSpanUname(
                            ss,
                            callOutSpans1,
                            this@SearchListAdapter,
                            data.mentionUsers
                        )
                        desc.visibility = View.VISIBLE
                        desc.movementMethod = LinkMovementMethod.getInstance()
                        desc.text = ss
                    } else {
                        desc.visibility = View.GONE
                    }

                    userProfile.setOnClickListener {
                        clickListeners.accountClick(data.creator._id)
                    }

                    dropImage.setOnClickListener {
                        it.disableMultiTap()
                        clickListeners.postClick(data)
                    }
                }
            }

            "sound" -> {
                val viewHolder = holder as SoundViewHolder

                viewHolder.binding.apply {
                    item = data
                    CoroutineScope(Dispatchers.IO).launch {
                        thumbnail.loadImageAsync(data.cover)
                    }
                }
            }

            "hashtag" -> {
                val viewHolder = holder as HashtagViewHolder
                viewHolder.binding.apply {
                    hashtagName.text = data.tag
                    totalPlay.text = "${(data.noOfPosts ?: 0)} Drops"
                    likeCount.text = (data.noOfLikes ?: 0).toString()
                    viewHolder.itemView.setOnClickListener {
                        clickListeners.hashtagClick(data._id)
                    }
                }
            }

            "user" -> {
                val viewHolder = holder as AccountViewHolder
                viewHolder.binding.apply {
//                    item = data
                    if (data._id == Prefs.init().userId){
                        overallContainer.visibility = View.VISIBLE
                    }else{
                        overallContainer.visibility = View.GONE
                    }
                    username.text = data.username
                    name.text = data.name
                    CoroutineScope(Dispatchers.IO).launch {
                        profileImage.loadImageAsync(data.profile)
                    }
                    accountType.text = when(data.accountType){
                        AppConstant.PRIVATE -> context.getString(R.string.private_account)
                        AppConstant.BUSINESS -> context.getString(R.string.business_account)
                        AppConstant.PUBLIC -> context.getString(R.string.public_account)
                        else -> ""
                    }
                    followCount.text = when(data.noOfFollowers){
                        null -> "0 Followers"
                        0,1 -> data.noOfFollowers.toString()+" Follower"
                        else -> data.noOfFollowers.toString()+" Followers"
                    }
                    likeCount.text = if (data.noOfLikes == null){
                        "0"
                    }else if (data.noOfLikes!! <2){
                        data.noOfLikes.toString()
                    }else {
                        data.noOfLikes.toString()
                    }
                    viewHolder.itemView.setOnClickListener {
                        clickListeners.accountClick(searchList[position]._id)
                    }
                    when (data.followStatus) {
                        "0" -> {
                            followBtn.text = "You"
                            followBtn.setTextColor(followBtn.context.getColor(R.color.RedColor))
                            followBtn.isEnabled = false
                        }

                        "1" -> {
                            followBtn.visibility = View.VISIBLE
                            followBtn.text =
                                viewHolder.itemView.context.getText(R.string.following)
                        }

                        "2" -> {
                            followBtn.visibility = View.VISIBLE
                            followBtn.text = viewHolder.itemView.context.getText(R.string.follow)
                        }

                        "3" -> {
                            followBtn.visibility = View.VISIBLE
                            followBtn.text =
                                viewHolder.itemView.context.getText(R.string.requested)
                        }
                    }
                    followBtn.setOnClickListener {
                        when (data.followStatus) {
                            "1" -> {
                                followBtn.text =
                                    viewHolder.itemView.context.getText(R.string.follow)
                            }

                            "2" -> {
                                if (data.accountType.equals(
                                        AppConstant.PRIVATE,
                                        ignoreCase = true
                                    )
                                ) {
                                    followBtn.text =
                                        viewHolder.itemView.context.getText(R.string.requested)
                                } else {
                                    followBtn.text =
                                        viewHolder.itemView.context.getText(R.string.following)
                                }
                            }
                            "3" -> {
                                followBtn.text = viewHolder.itemView.context.getText(R.string.follow)
                            }
                        }
//                        if (item.creator.followStatus != 3) {
                            clickListeners.followUnfollowClick(position)
//                        }
                    }
                }
            }
        }
    }

    class DropViewHolder(
        val binding: SearchDropsListItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class AccountViewHolder(
        val binding: SearchAccountListItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class SoundViewHolder(
        val binding: SearchSoundListItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class HashtagViewHolder(
        val binding: HashtagListItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    fun setNewItem(newItems: List<Data>) {
        searchList.clear()
        searchList.addAll(newItems)
        notifyDataSetChanged()
    }
    fun addNewItem(newItems: List<Data>) {
        val oldPos = searchList.size + 1
        searchList.addAll(newItems)
        notifyItemRangeInserted(oldPos, newItems.size)
    }

    override fun hashTagClick(text: String) {
        clickListeners.hashtagClick(text)
    }

    override fun mentionClick(user: String) {
        clickListeners.accountClick(user)
    }
    interface PostClickListener{
        fun accountClick(user: String)
        fun followUnfollowClick(position: Int)
        fun hashtagClick(text: String)
        fun postClick(data: Data)
    }
}