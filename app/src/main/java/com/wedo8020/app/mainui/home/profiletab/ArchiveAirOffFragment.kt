package com.wedo8020.app.mainui.home.profiletab

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.wedo8020.app.databinding.FragmentArcheiveAirOffBinding
import com.wedo8020.app.eventBus.ProfilePostItemClick
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.adapter.ActivePostAdapter
import com.wedo8020.app.mainui.home.adapter.ActivePostsAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.DeletePost
import com.wedo8020.app.request.PostPinRequest
import com.wedo8020.app.request.ProfilePostRequest
import com.wedo8020.app.response.Data
import com.wedo8020.app.utils.ArchivePostUpdate
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.OnClickEditPost
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.SaveDropUpdate
import com.wedo8020.app.utils.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Objects

private const val ARG_PARAM1 = "mentionUser"
private const val ARG_PARAM2 = "canSee"
private const val ARG_PARAM3 = "user_blocked"

class ArchiveAirOffFragment : BaseFragment(),ActivePostAdapter.OnClickListener {
 private lateinit var mBinding:FragmentArcheiveAirOffBinding
    private var offset = 0
    private var limit = 20
    var dialog: Dialog? = null
    var bundle = Bundle()
    var dialogs: Dialog? = null
    private var isPin = false
    private lateinit var manager: RecyclerView.LayoutManager
    private val authViewModel by viewModels<PostViewModel>()
    lateinit var adapter: ActivePostsAdapter
    private var list: ArrayList<Data> = ArrayList()
    private var animation = false
    private var pinPost = false
    private var isPagination = false
    private var deletePostId=""
    private var bunble=Bundle()
 /*   private var pinList: ArrayList<String> = ArrayList()
    private var PinPost = false
    private var isPagination = false
    private var currentPage = 1
    var DeletePostId=""*/
    private var mentionUserId = ""
    private var canSee = true
    private var updatePosition = 0
    private var isDeletePost = false
    private var userBlocked = false
    private var updatedIndex = 0
    private var isCallApi = false
    private var dataRequestMap: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mentionUserId = it.getString(ARG_PARAM1) ?: ""
            canSee = it.getBoolean(ARG_PARAM2)
            userBlocked = it.getBoolean(ARG_PARAM3)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding=DataBindingUtil.inflate(inflater, R.layout.fragment_archeive_air_off, container, false)
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            if (!isCallApi) {
                mBinding.animationView.visibility = View.VISIBLE
             //   adapter.list.clear()
                list.clear()
                callApi()
                offset = 0
                isCallApi = true
            }
        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        setPinObserver()
        profilePostRecyclerViewAdapter()
        manager = GridLayoutManager(requireContext(), 3)
        mBinding.rvArchivePost.layoutManager = manager
        mBinding.rvArchivePost.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    if (pinPost){

                        offset=0
                        pinPost=false
                    }
                    offset += 20
                    isPagination=true

                    loadMoreData()

                } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {

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

    override fun onDestroy() {
        super.onDestroy()
        offset=0
        adapter.longPressTypes=""
        Prefs.init().removeActivePostId=""
        Prefs.init().removePostType=""
        Prefs.init().removeSavePostId=""
        Prefs.init().removeArchivePostId=""
    }


    private fun loadMoreData() {
        if (isInternetAvailable(requireActivity())) {
            authViewModel.profilePostRequest =
                ProfilePostRequest(type = "archive", offset = offset, limit = limit)
            setRequestData(offset, limit)
            authViewModel.profilePosts(dataRequestMap)
        } else {
            dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }
    private fun setRequestData(offset: Int, limit: Int) {
        dataRequestMap.clear()
        dataRequestMap["type"] = "archive"
        dataRequestMap["offset"] = offset.toString()
        dataRequestMap["limit"] = limit.toString()
        if (mentionUserId.isNotEmpty()) {
            dataRequestMap["user"] = mentionUserId
        }
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }
    override fun onPause() {
        super.onPause()
        adapter.list.let { list = it }
    }

    @SuppressLint("SetTextI18n")
    private fun setObserver() {
        authViewModel.userProfilePost.observe(viewLifecycleOwner, EventObserver { it1 ->
            mBinding.animationView.visibility = View.GONE
            mBinding.rvArchivePost.visibility = View.VISIBLE
            if (it1.success) {
                adapter.longPressTypes="archive"
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


                } else {
                    if (::adapter.isInitialized) {
                        if (adapter.list.size == 0) {
                            mBinding.TvNoData.visibility = View.VISIBLE
                            mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()) {
                                "No post found"
                            } else {
                                "Your \"off the air\" posts will \nappear here"
                            }
                        } else {
                            mBinding.TvNoData.visibility = View.GONE
                        }
                    } else {
                        mBinding.TvNoData.visibility = View.VISIBLE
                        mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()) {
                            "No post found"
                        } else {
                            "Your \"off the air\" posts will \nappear here"
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
        authViewModel.userProfilePost.observe(viewLifecycleOwner, EventObserver { it1 ->
            mBinding.animationView.visibility=View.GONE
            dialogs?.dismiss()
            if (it1.success) {
                animation = true
                it1.data?.let {
                    if (!it1.data.isNullOrEmpty()) {
                        mBinding.TvNoData.visibility = View.GONE
                        when {
                            isPagination -> {
                            }
                            else -> {
                                list.clear()
                            }
                        }
                        it1.data!!.forEach {
                            if (!list.contains(it!!)){
                                list.add(it)
                            }
                        }
                      *//*  list.sortedBy {
                            it.pinTime
                        }*//*
                        mBinding.rvArchivePost.reStoreState()
                        adapter = ActivePostAdapter(list , this,mentionUserId.isNotEmpty(),context?: MainApplication.get().applicationContext)
                        mBinding.rvArchivePost.adapter = adapter
                        adapter.longPressTypes="archive"
                    }else{
                        if (::adapter.isInitialized){
                            if (adapter.list.size==0){
                                mBinding.TvNoData.visibility = View.VISIBLE
                                mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()){
                                    "No post found"
                                }else{
                                    "Your \"off the air\" posts will \nappear here"
                                }
                            } else {
                                mBinding.TvNoData.visibility = View.GONE
                            }
                        }else {
                            if (adapter.list.size == 0) {
                                mBinding.TvNoData.visibility = View.VISIBLE
                                mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()) {
                                    "No post found"
                                } else {
                                    "Your \"off the air\" posts will \nappear here"
                                }
                            }
                        }
                    }


                }
                if (::adapter.isInitialized){
                    adapter.notifyDataSetChanged()
                }
            }
        })

        authViewModel.showLoading.observe(requireActivity()) {
            if (it){
                mBinding.animationView.visibility=View.VISIBLE
            }else{
                mBinding.animationView.visibility=View.GONE
            }
        }
    }*/

    override fun onResume() {
        super.onResume()
        adapter.longPressTypes="archive"
        if (!Prefs.init().removeArchivePostId.isNullOrEmpty()) {
            if (updatedIndex < list.size && list.isNotEmpty()) {
                getPositionById(Prefs.init().removeArchivePostId, adapter.list)
                adapter.list.removeAt(updatedIndex)
                adapter.notifyItemRemoved(updatedIndex)
                adapter.notifyItemRangeChanged(updatedIndex, adapter.list.size)
            }
        }
    }
    private fun getPositionById(id: String, list: List<Data>): Int {
        for ((index, item) in list.withIndex()) {
            if (item._id == id) {
                updatedIndex=index
                return index
            }
        }
        return -1 // ID not found in the list
    }

/*    override fun onResume() {
        super.onResume()
        mBinding.rvArchivePost.reStoreState()
        adapter = ActivePostAdapter(list , this,mentionUserId.isNotEmpty(),context?:MainApplication.get().applicationContext)
        mBinding.rvArchivePost.adapter = adapter
        //    adapter.notifyDataSetChanged()
    }*/
  /*  private fun setPinObserver() {
        authViewModel.postPinLikeData.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()
                    list.clear()
                    authViewModel.profilePostRequest =
                        ProfilePostRequest(type = "archive", offset = 0, limit = limit)
                    setRequestData(0, limit)
                    authViewModel.profilePosts(dataRequestMap)
                  //  adapter.notifyDataSetChanged()
                }
            }
        }

        authViewModel.showLoading.observe(requireActivity()) {
        }
       authViewModel.showMessage.observe(requireActivity()){
           mBinding.animationView.visibility=View.GONE
           when {
               it.trim().isNotEmpty() -> {

               }
           }
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

    override fun onItemClick(position: Int, data: Data) {
        EventBus.getDefault().post(
            ProfilePostItemClick(
                position,
                list,
                "archive"
            )
        )
    }

    override fun onLongPressClick(view: View, position: Int, data: Data) {
        //  Toast.makeText(requireContext(),"long Press",Toast.LENGTH_SHORT).show()
    }

    override fun onClickEdit(view: View, position: Int, data: Data) {
        val postId=data._id
        bunble.putString("EditPostId",postId)
        findNavController().navigate(R.id.editPostFragment,bunble)
    }

    override fun onArchive(view: View, position: Int, data: Data) {
        offset = 0
        val postId = data._id
        if (isInternetAvailable(requireActivity())) {
           // showProgressDialogs()
            authViewModel.deletePostRequest =
                DeletePost( post = postId)
            authViewModel.userUnArchivePost()

            mBinding.rvArchivePost.smoothScrollToPosition(0)
        }else {
            dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }

    override fun onDeletePost(view: View, position: Int, data: Data) {
        offset = 0
        deletePostId = data._id
        deletePosts(position)
    }

    override fun onClickPin(view: View, position: Int, data: Data, click: Boolean) {
        offset = 0
        isPagination=false
        pinPost=click
        val id = data._id
        if (isInternetAvailable(requireActivity())) {
           // showProgressDialogs()
            authViewModel.postPinRequest =
                PostPinRequest(type = "archive", post = id)
            authViewModel.userPostPin()
            mBinding.rvArchivePost.smoothScrollToPosition(0)
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

    fun callApi() {

        if (userBlocked){
            mBinding.TvNoData.text = requireContext().getText(R.string.block_user_archive_post_msg)
            mBinding.animationView.visibility=View.GONE
            dialogs?.dismiss()
        } else if (!canSee){
            mBinding.TvNoData.text = requireContext().getText(R.string.userDisableArchive)
            mBinding.animationView.visibility=View.GONE
            dialogs?.dismiss()
        } else{
            if (isInternetAvailable(requireActivity())) {
//            mBinding.animationView.visibility=View.VISIBLE
                authViewModel.profilePostRequest =
                    ProfilePostRequest(type = "archive", offset = offset, limit = limit)
                if (mentionUserId.isNotEmpty()){


                    setRequestData(offset, limit)
                    authViewModel.profilePosts(dataRequestMap)


                }else{
                    setRequestData(offset, limit)
                    authViewModel.profilePosts(dataRequestMap)
                }

            } else {
                dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
        }


    }
    @SuppressLint("SetTextI18n")
    fun deletePosts(position: Int) {
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
        logout.text="Delete"
        sureLogout.text="Delete permanently?"
        ivClose.setOnClickListener {
            dialog?.dismiss()
        }
        logout.setOnClickListener {
            if (isInternetAvailable(requireActivity())) {
              //  showProgressDialogs()
                authViewModel.deletePostRequest = DeletePost(post = deletePostId)
                authViewModel.userDeletedPost()
//                mBinding.rvArchivePost.smoothScrollToPosition(0)
                adapter.list.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, adapter.list.size)

            } else {
                dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    companion object{
        fun newInstance(): ArchiveAirOffFragment {
            return ArchiveAirOffFragment()
        }
        @JvmStatic
        fun newInstance(param1: String, canSee: Boolean, userBlock: Boolean) =
            ArchiveAirOffFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putBoolean(ARG_PARAM2, canSee)
                    putBoolean(ARG_PARAM3, userBlock)
                }
            }
    }

    private fun profilePostRecyclerViewAdapter() {
        mBinding.apply {
            manager = GridLayoutManager(requireContext(), 3)
            mBinding.rvArchivePost.layoutManager = manager
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
                            offset = 0
                            isDeletePost = true
                            updatePosition = position
                            if (isInternetAvailable(requireActivity())) {
                                //   showProgressDialogs()
                                authViewModel.deletePostRequest =
                                    DeletePost( post = data._id)
                                authViewModel.userUnArchivePost()
                                //   mBinding.rvProfilePost.smoothScrollToPosition(0)
                                adapter.list.removeAt(updatePosition)
                                adapter.notifyItemRemoved(updatePosition)
                                adapter.notifyItemRangeChanged(updatePosition, adapter.list.size)

                            } else {
                                dialogs?.dismiss()
                                Utils.showToast("Please check your internet connection.")
                            }
                            clearPosition()
                            //  setPinObserver()
                        }

                        override fun onDeletePost(view: View, position: Int, data: Data) {
                            deletePostId = data._id
                            offset = 0
                            isDeletePost = true
                            updatePosition = position
                            deletePosts(position)
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
                            if (isInternetAvailable(requireActivity())) {
                                // showProgressDialogs()
                                authViewModel.postPinRequest =
                                    PostPinRequest(type = "archive", post = id)
                                authViewModel.userPostPin()
                            } else {
                                dialogs?.dismiss()
                                Utils.showToast("Please check your internet connection.")
                            }
                        }
                    },
                    mentionUserId.isNotEmpty(),
                    context ?: MainApplication.get().applicationContext
                )
            rvArchivePost.adapter = adapter
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
                        "Your \"active posts\" will \nappear here"
                    }
                } else {
                    mBinding.TvNoData.visibility = View.GONE
                }
            } else {
                mBinding.TvNoData.visibility = View.VISIBLE
                mBinding.TvNoData.text = if (mentionUserId.isNotEmpty()) {
                    "No post found"
                } else {
                    "Your \"active posts\" will \nappear here"
                }
            }
        }
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
    fun onClickArchivePostEvent(event: ArchivePostUpdate?) {
   /*     adapter.list.clear()
        list.clear()*/
        adapter.longPressTypes="archive"
        Prefs.init().removeActivePostId=""
        Prefs.init().removePostType=""
        Prefs.init().removeSavePostId=""
        Prefs.init().removeArchivePostId=""
        adapter.longPress = false
        adapter.selectedPosition = -1
    //    mBinding.animationView.visibility = View.VISIBLE
      //  mBinding.rvArchivePost.visibility=View.INVISIBLE
        offset = 0
      //  callApi()
    }

}