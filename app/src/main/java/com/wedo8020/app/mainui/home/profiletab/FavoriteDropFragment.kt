package com.wedo8020.app.mainui.home.profiletab

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentFavoriteDropBinding
import com.wedo8020.app.eventBus.ProfilePostItemClick
import com.wedo8020.app.mainui.home.adapter.ActivePostsAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.PostPinRequest
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.ProfilePostRequest
import com.wedo8020.app.response.Data
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.OnClickEditPost
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.SaveDropUpdate
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.isInternetAvailables
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Objects


class FavoriteDropFragment : Fragment() {
    private lateinit var mBinding: FragmentFavoriteDropBinding
    private lateinit var manager: RecyclerView.LayoutManager
    private var pinPost = false
    private var isPin = false
    private var offset = 0
    var bundle = Bundle()
    private var limit = 20
    private var isPagination = false
    lateinit var adapter: ActivePostsAdapter
    var dialogs: Dialog? = null
    var dialog: Dialog? = null
    private var deletePostId = ""
    private var removePosition = -1
    private val authViewModel by viewModels<PostViewModel>()
    private var list: ArrayList<Data> = ArrayList()
    private var dataRequestMap: HashMap<String, String> = HashMap()
    private var mentionUserId = ""
    private var updatedIndex = 0
    private var isCallApi = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_favorite_drop, container, false)
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        setObserver()
        /*if (!isCallApi) {
        //    mBinding.animationView.visibility = View.VISIBLE
            callApi()
            isCallApi = true
        }*/
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPinObserver()
        unSavePostObserver()
        profilePostRecyclerViewAdapter()
        manager = GridLayoutManager(requireContext(), 3)
        mBinding.rvProfilePost.layoutManager = manager

        mBinding.rvProfilePost.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    if (pinPost) {
                        offset = 0
                        pinPost = false
                    }
                    offset += 20
                    isPagination = true
                    callApi()
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
        if (isInternetAvailables(requireActivity())) {
            authViewModel.savePost(
                ProfilePostRequest(
                    type = "save",
                    offset = offset,
                    limit = limit
                )
            )
        } else {
            dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }

    private fun setRequestData(offset: Int, limit: Int) {
        dataRequestMap.clear()
        dataRequestMap["type"] = "save"
        dataRequestMap["offset"] = offset.toString()
        dataRequestMap["limit"] = limit.toString()
    }

    @SuppressLint("SetTextI18n")
    private fun setObserver() {
        authViewModel.userSavedPost.observe(viewLifecycleOwner, EventObserver { it1 ->
            mBinding.animationView.visibility = View.GONE
            mBinding.rvProfilePost.visibility = View.VISIBLE
            if (it1.success) {
                if (!it1.data.isNullOrEmpty()) {

                    mBinding.TvNoData.visibility = View.GONE
                    /*  if (isPin) {
                       *//*mBinding.rvProfilePost.smoothScrollToPosition(0)
                        isPin = false*//*
                    }*/
                    if (adapter.list.size > 0) {
                        adapter.addNewItems(it1.data!!)
                    } else {
                        adapter.setNewItems(it1.data!!)
                        // mBinding.rvProfilePost.smoothScrollToPosition(0)
                    }
                    adapter.longPressTypes = "save"
                } else {
                    if (::adapter.isInitialized) {
                        if (adapter.list.size == 0) {
                            mBinding.TvNoData.visibility = View.VISIBLE
                            mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()) {
                                "No post found"
                            } else {
                                "Your \"favorite drops \" will \nappear here"
                            }
                        } else {
                            mBinding.TvNoData.visibility = View.GONE
                        }
                    } else {
                        mBinding.TvNoData.visibility = View.VISIBLE
                        mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()) {
                            "No post found"
                        } else {
                            "Your \"favorite drops \" will \nappear here"
                        }
                    }

                }

            }
        })
        authViewModel.showLoading.observe(viewLifecycleOwner) {

        }

    }
    /*  @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
      private fun setObserver() {
          authViewModel.userSavedPost.observe(requireActivity()) { response ->
              mBinding.animationView.visibility = View.GONE
              mBinding.rvProfilePost.visibility = View.VISIBLE
              if (response != null) {
                  if (response.success) {
                      dialogs?.dismiss()
                      //     animation = true
                      response.data?.let {
                          if (!response.data.isNullOrEmpty()) {
                              mBinding.TvNoData.visibility = View.GONE
                              when {
                                  isPagination -> {
                                  }

                                  else -> {
                                      list.clear()
                                      newlist.clear()
                                  }
                              }
                              list.addAll(response.data!!)
                              list.forEach {
                                  if (!newlist.contains(it)) {
                                      newlist.add(it)
                                  }
                              }
                              list.sortedBy {
                                  it.pinTime
                              }
                              mBinding.rvProfilePost.reStoreState()
                              adapter = ActivePostAdapter(newlist, this@FavoriteDropFragment,context=context?:MainApplication.get().applicationContext)
                              mBinding.rvProfilePost.adapter = adapter
                              adapter.longPressTypes = "save"
                              adapter.notifyDataSetChanged()
                          } else {
                              if (adapter.list.size == 0) {
                                  adapter.notifyDataSetChanged()
                                  mBinding.TvNoData.visibility = View.VISIBLE
                                  mBinding.TvNoData.text =
                                      "Your \"favorite drops \" will \nappear here"
                              } else {
                                  mBinding.TvNoData.visibility = View.GONE
                              }
                          }

                      }
                  }
              }
          }

          authViewModel.showLoading.observe(requireActivity()) {
          }
      }*/


    @SuppressLint("NotifyDataSetChanged")
    private fun setPinObserver() {
        authViewModel.postPinLikeData.observe(viewLifecycleOwner) { response ->
            dialogs?.dismiss()
            mBinding.animationView.visibility = View.GONE
            if (response != null) {
                if (response.success) {
                    if (isPin){
                        adapter.list.clear()
                        list.clear()
                        callApi()
                    }
                    adapter.longPress = false
                    adapter.selectedPosition = -1

                    // adapter.notifyDataSetChanged()
                }
            }
        }


        authViewModel.showLoading.observe(viewLifecycleOwner) {

        }
    }







    /*    private fun setPinObserver() {
            authViewModel.postPinLikeData.observe(viewLifecycleOwner) { response ->
                mBinding.animationView.visibility = View.GONE
                mBinding.rvProfilePost.visibility = View.VISIBLE
                dialogs?.dismiss()
                if (response != null) {
                    if (response.success) {
                        list.clear()
                        newlist.clear()
                        adapter.longPressTypes = "save"
                        if (isInternetAvailables(requireActivity())) {
                            mBinding.animationView.visibility = View.VISIBLE
                            authViewModel.savePost(
                                ProfilePostRequest(
                                    type = "save",
                                    offset = offset,
                                    limit = limit
                                )
                            )
                        } else {
                            dialogs?.dismiss()
                            Utils.showToast("Please check your internet connection.")
                        }
                    }
                }
            }

            authViewModel.showLoading.observe(requireActivity()) {
                if (!it) {
                    mBinding.animationView.visibility = View.VISIBLE
                } else {
                    mBinding.animationView.visibility = View.GONE
                }
            }
        }*/

    @SuppressLint("SetTextI18n")
    fun deletePost(position: Int) {
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

            if (isInternetAvailables(requireActivity())) {
                // showProgressDialogs()
                authViewModel.postSaveRequest =
                    PostRequest(post = deletePostId)
                authViewModel.postSave()
                adapter.removeItem(removePosition)
                adapter.list.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, adapter.list.size)
                if (adapter.list.size == 0) {
                    mBinding.TvNoData.visibility = View.VISIBLE
                    mBinding.TvNoData.text = "Your \"favorite drops \" will \nappear here"
                } else {
                    mBinding.TvNoData.visibility = View.GONE
                }
            } else {
                dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun unSavePostObserver() {
        authViewModel.postSaveData.observe(requireActivity()) { response ->
            if (response.success) {
                dialogs?.dismiss()
                /*
                    adapter.list.removeAt(removePosition)
                    adapter.notifyItemRemoved(removePosition)
                    // Notify items from removePosition to the end of the list that their positions changed
                    adapter.notifyItemRangeChanged(removePosition, adapter.list.size - removePosition)*/

            }
        }

        authViewModel.showLoading.observe(requireActivity()) {
        }
    }

    override fun onPause() {
        super.onPause()
        adapter.list.let { list = it }
    }

    override fun onResume() {
        super.onResume()
        adapter.longPressTypes="save"
        if (!Prefs.init().removeSavePostId.isNullOrEmpty()) {
            if (updatedIndex<list.size && list.isNotEmpty()){
                getPositionById(Prefs.init().removeSavePostId, adapter.list)
                adapter.list.removeAt(updatedIndex)
                adapter.notifyItemRemoved(updatedIndex)
                adapter.notifyItemRangeChanged(updatedIndex, adapter.list.size)
            }

        }
    }

    private fun getPositionById(id: String, list: List<Data>): Int {
        for ((index, item) in list.withIndex()) {
            if (item._id == id) {
                updatedIndex = index
                return index
            }
        }
        return -1 // ID not found in the list
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onStart() {
        super.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
        //initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClickSavePostEvent(event: SaveDropUpdate?) {
        Prefs.init().removeActivePostId=""
        Prefs.init().removePostType=""
        Prefs.init().removeSavePostId=""
        adapter.longPress = false
        offset = 0
        adapter.selectedPosition = -1
        adapter.longPressTypes = "save"
        adapter.list.clear()
        mBinding.animationView.visibility = View.VISIBLE
        mBinding.rvProfilePost.visibility=View.INVISIBLE
        callApi()
    }

    private fun profilePostRecyclerViewAdapter() {
        mBinding.apply {
            manager = GridLayoutManager(requireContext(), 3)
            mBinding.rvProfilePost.layoutManager = manager
            adapter =
                ActivePostsAdapter(
                    R.layout.post_recycleview,
                    object :
                        ActivePostsAdapter.OnClickListener {
                        override fun onItemClick(position: Int, data: Data) {
                            EventBus.getDefault().post(
                                ProfilePostItemClick(
                                    position,
                                    adapter.list,
                                    "post"
                                )
                            )
                        }

                        override fun onLongPressClick(view: View, position: Int, data: Data) {
                        }

                        override fun onClickEdit(view: View, position: Int, data: Data) {
                            val postId = data._id
                            bundle.putString("EditPostId", postId)
                            findNavController().navigate(R.id.editPostFragment, bundle)
                            adapter.list[position].isSelect = false
                            EventBus.getDefault()
                                .post(OnClickEditPost("edit"))
                            clearPosition()
                        }

                        override fun onArchive(view: View, position: Int, data: Data) {
                            /*  offset = 0

                              if (isInternetAvailable(requireActivity())) {
                                  //   showProgressDialogs()
                                  authViewModel.deletePostRequest =
                                      DeletePost(post = data._id)
                                  authViewModel.userArchivePost()
                                  //   mBinding.rvProfilePost.smoothScrollToPosition(0)
                                  adapter.list.removeAt(position)
                                  adapter.notifyItemRemoved(position)
                                  adapter.notifyItemRangeChanged(position, adapter.list.size)

                              } else {
                                  dialogs?.dismiss()
                                  Utils.showToast("Please check your internet connection.")
                              }
                              clearPosition()*/
                            //  setPinObserver()
                        }

                        override fun onDeletePost(view: View, position: Int, data: Data) {
                            deletePostId = data._id
                            offset = 0

                            deletePost(position)
                            clearPosition()

                        }

                        override fun onClickPin(
                            view: View,
                            position: Int,
                            data: Data,
                            click: Boolean
                        ) {
                            offset = 0
                            isPagination = false
                            pinPost = click
                            isPin = true
                            val id = data._id
                            if (isInternetAvailables(requireActivity())) {
                                authViewModel.postPinRequest =
                                    PostPinRequest(type = "save", post = id)
                                authViewModel.userPostPin()
                              //  mBinding.rvProfilePost.smoothScrollToPosition(0)
                            } else {
                                dialogs?.dismiss()
                                Utils.showToast("Please check your internet connection.")
                            }

                        }
                    },
                    mentionUserId.isNotEmpty(),
                    context ?: MainApplication.get().applicationContext
                )
            rvProfilePost.adapter = adapter
            adapter.setNewItems(list)
        }
    }

    private fun clearPosition() {
        adapter.longPress = false
        adapter.selectedPosition = -1
        if (adapter.list.size == 0) {
            if (::adapter.isInitialized) {
                if (adapter.list.size == 0) {
                    mBinding.TvNoData.visibility = View.VISIBLE
                    mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()) {
                        "No post found"
                    } else {
                        "Your \"favorite drops \" will \nappear here"
                    }
                } else {
                    mBinding.TvNoData.visibility = View.GONE
                }
            } else {
                mBinding.TvNoData.visibility = View.VISIBLE
                mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()) {
                    "No post found"
                } else {
                    "Your \"favorite drops \" will \nappear here"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        offset = 0
        adapter.longPressTypes=""
        Prefs.init().removeActivePostId=""
        Prefs.init().removePostType=""
        Prefs.init().removeSavePostId=""
        Prefs.init().removeArchivePostId=""
    }
}