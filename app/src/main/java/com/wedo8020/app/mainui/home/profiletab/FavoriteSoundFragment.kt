package com.wedo8020.app.mainui.home.profiletab

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentFavoriteSoundBinding
import com.wedo8020.app.mainui.home.adapter.SoundAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.SoundPinRequest
import com.wedo8020.app.request.ProfilePostRequest
import com.wedo8020.app.response.Data
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.Utils
import java.util.Objects


class FavoriteSoundFragment : Fragment(), SoundAdapter.OnClickListeners {
    private lateinit var mBinding: FragmentFavoriteSoundBinding
    private lateinit var manager: RecyclerView.LayoutManager
    private var pinPost = false
    private var offset = 0
    private var limit = 20
    private var isPagination = false
    lateinit var adapter: SoundAdapter
    var dialogs: Dialog? = null
    var dialog: Dialog? = null
    private var deletePostId = ""
    private val authViewModel by viewModels<PostViewModel>()
    private var soundList: ArrayList<Data> = ArrayList()
    private var dataRequestMap: HashMap<String, String> = HashMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_favorite_sound, container, false)
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        setObserver()
        callApi()
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPinObserver()
        manager = GridLayoutManager(requireContext(), 3)
        mBinding.rvProfilePost.layoutManager = manager

        mBinding.rvProfilePost.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    if (pinPost) {

                        offset = 0
                        pinPost = false
                    }
                    offset += 10
                    isPagination = true

                    loadMoreData()

                }

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_FLING -> {

                    }

                    AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> {

                    }

                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {

                    }

                    else -> {

                    }
                }

            }
        }

        )


    }

    fun callApi() {
        if (Utils.isInternetAvailables(requireActivity())) {
            showProgressDialogs()
            authViewModel.profilePostRequest =
                ProfilePostRequest(type = "sound", offset = offset, limit = limit)
            setRequestData(offset, limit)
            authViewModel.profilePosts(dataRequestMap)
        } else {
            dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }

    }
    private fun setRequestData(offset: Int, limit: Int) {
        dataRequestMap.clear()
        dataRequestMap["type"] = "sound"
        dataRequestMap["offset"] = offset.toString()
        dataRequestMap["limit"] = limit.toString()

    }
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun setObserver() {
        authViewModel.userProfilePost.observe(viewLifecycleOwner, EventObserver { it1 ->
            if (it1 != null) {
                if (it1.success) {
                    dialogs?.dismiss()
                    //     animation = true
                    Log.e("UserDetails", it1.data!!.size.toString())
                    it1.data?.let {

                        if (!it1.data.isNullOrEmpty()) {
                            if (isPagination) {
                            } else {
                                soundList.clear()
                            }
                            it1.data!!.forEach {
                                if (!soundList.contains(it!!)) {
                                    soundList.add(it)
                                }
                            }
                            soundList.sortedBy {
                                it.pinTime
                            }
                            Log.e("UserListSize", soundList.size.toString())
                            mBinding.rvProfilePost.reStoreState()
                            adapter = SoundAdapter(soundList, this@FavoriteSoundFragment)
                            mBinding.rvProfilePost.adapter = adapter
                            adapter.longPressTypes = "save"
                            adapter.notifyDataSetChanged()

                        }
                        if (soundList.size == 0) {
                            mBinding.TvNoData.text =
                                "Your \"Favorite sound post\" will \nappear here"
                        } else {
                            mBinding.TvNoData.visibility = View.GONE
                        }
                    }
                }
            }
        })

        authViewModel.showLoading.observe(requireActivity()) {
//showProgressDialogs()
        }
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    private fun loadMoreData() {

        if (Utils.isInternetAvailables(requireActivity())) {

            authViewModel.profilePostRequest =
                ProfilePostRequest(type = "sound", offset = offset, limit = limit)
            setRequestData(offset, limit)
            authViewModel.profilePosts(dataRequestMap)
        } else {
            dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }

    private fun showProgressDialogs() {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.progressbar)
        dialogs?.show()
    }

    override fun onLongPressClicks(view: View, position: Int, data: Data) {

    }


    override fun onPause() {
        super.onPause()
        SoundAdapter.player?.playWhenReady = false
        SoundAdapter.onPause = true
        //  pauseCurrentPlayingVideo()
    }

    override fun onResume() {
        super.onResume()
        SoundAdapter.player?.playWhenReady = false
        SoundAdapter.onPause = false
    }

    override fun onDeletePosts(view: View, position: Int, data: Data) {
        deletePostId = data._id
        deletePosts()
    }

    override fun onClickPins(view: View, position: Int, data: Data, click: Boolean) {
        isPagination = false
        pinPost = click
        val id = data._id
        if (Utils.isInternetAvailables(requireActivity())) {
            //  showProgressDialogs()
            authViewModel.soundPinRequest =
                SoundPinRequest(sound = id)
            authViewModel.userPostSoundPin()
            mBinding.rvProfilePost.smoothScrollToPosition(0)
        } else {
            dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }

    private fun setPinObserver() {
        authViewModel.postPinLikeData.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()
                    soundList.clear()
                    adapter.longPressTypes = "save"
                    showProgressDialogs()
                    authViewModel.profilePostRequest =
                        ProfilePostRequest(type = "sound", offset = 0, limit = limit)
                    setRequestData(0,limit)
                    authViewModel.profilePosts(dataRequestMap)
                }
            }
        }

        authViewModel.showLoading.observe(requireActivity()) {
//showProgressDialogs()
        }
    }

    private fun setSoundDeleteObserver() {
        authViewModel.postSaveSoundData.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()
                    soundList.clear()
                    adapter.longPressTypes = "save"
                    showProgressDialogs()
                    authViewModel.profilePostRequest =
                        ProfilePostRequest(type = "sound", offset = 0, limit = limit)
                    setRequestData(0,limit)
                    authViewModel.profilePosts(dataRequestMap)
                }
            }
        }

        authViewModel.showLoading.observe(requireActivity()) {
//showProgressDialogs()
        }
    }

    @SuppressLint("SetTextI18n")
    fun deletePosts() {
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.logout_are_you_sure)

        val logout = dialog?.findViewById(R.id.Logout) as TextView
        val sureLogout = dialog?.findViewById(R.id.SureLoutout) as TextView
        val ivClose = dialog?.findViewById(R.id.ivClose) as ImageView
        logout.text = "Delete"
        sureLogout.text = "Delete permanently?"
        ivClose.setOnClickListener {
            dialog?.dismiss()
        }
        logout.setOnClickListener {

            if (Utils.isInternetAvailables(requireActivity())) {
                showProgressDialogs()
                authViewModel.postSaveSoundRequest =
                    PostRequest(sound = deletePostId)
                authViewModel.postSaveSound()
                setSoundDeleteObserver()
                mBinding.rvProfilePost.smoothScrollToPosition(0)
            } else {
                dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

}