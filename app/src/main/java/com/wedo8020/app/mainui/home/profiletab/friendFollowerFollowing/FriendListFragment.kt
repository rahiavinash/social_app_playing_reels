package com.wedo8020.app.mainui.home.profiletab.friendFollowerFollowing

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentFriendListBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profiletab.friendFollowerFollowing.adapter.FriendAdapter
import com.wedo8020.app.request.FriendListRequest
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.response.FriendListData
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.loadImageAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects

class FriendListFragment : BaseFragment(),FriendAdapter.OnClickListener{
    private lateinit var mBinding :FragmentFriendListBinding
    private val authViewModel by viewModels<PostViewModel>()
    private lateinit var adapter: FriendAdapter
    private var requestStatusForIndex = ""
    private var updatePosition = 0
    var dialog: Dialog? = null
    var type = ""
    var isSearch = false
    private var userId = ""
    private var userProfile = ""
    var offset = 0
    var order = -1
    var  isPagination = false
    private var sortByLike = false
    var searchValue=""
    private var list: ArrayList<FriendListData> = ArrayList()
    private val uniqueList = arrayListOf<FriendListData>()
    var handlers = Handler(Looper.getMainLooper())
    private var refreshItems = false
    var profile = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding=DataBindingUtil.inflate(inflater,R.layout.fragment_friend_list, container, false )
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        hideNavBottom()
        hideKeyboardOnOutSideClick(mBinding.clView)
        callApiForGetList()
        likeListObserver()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Refresh the Data */
        mBinding.refresh.setOnRefreshListener {
            offset=0
            refreshItems=true
            mBinding.animationView.visibility = View.GONE
            mBinding.TvNoData.visibility = View.GONE
            Handler(Looper.myLooper()!!).postDelayed({
                callApiForGetList()
            },1000)
        }
        mBinding.rvReportBlocked.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    offset += 20
                    callApiForGetList()
                    mBinding.paginationLoader.visibility=View.VISIBLE
                    isPagination = true
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
        })
        mBinding.searchEt.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    list.clear()
                    uniqueList.clear()
                    searchValue = p0.toString()
                    if (Utils.isInternetAvailables(requireActivity())) {
                        handlers.removeCallbacksAndMessages(null)
                        handlers.postDelayed({
                            Handler(Looper.getMainLooper()).postDelayed({
                                isSearch=true
                                authViewModel.friendList(
                                    FriendListRequest(
                                        offset = offset,
                                        limit = 20,
                                        order=1,
                                        search = searchValue,
                                    )
                                )

                            }, 0)

                        }, 200)
                    } else {
                        Utils.showToast("Please check your internet connection.")
                    }
                }


            })
        }

        mBinding.llSortUP.setOnClickListener {
            list.clear()
            uniqueList.clear()
            offset=0
            if (mBinding.llSortDown.visibility == View.VISIBLE) {
                mBinding.llSortDown.visibility = View.GONE
                sortByLike = false
            } else {
                mBinding.llSortDown.visibility = View.VISIBLE
                sortByLike = true
            }
            order = if (order == -1) {
                1
            } else {
                -1
            }
            callApiForGetList()
        }



        /**Call the observer for Follow and Unfollow Request*/
        authViewModel.followUnfollowUserData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                val followStatus: Int = when (requestStatusForIndex) {
                    "Unfollow" -> {
                        2
                    }

                    "Requested" -> {
                        3
                    }

                    else -> {
                        1
                    }
                }
                adapter.updateItem(updatePosition, followStatus , profile)

                showToast(response.message.toString())
            }
        }
    }

    private fun callApiForGetList() {
        if (isInternetAvailable(requireContext())) {
            authViewModel.friendList(
                FriendListRequest(
                    offset = offset,
                    limit = 20,
                    order=order,
                    search = searchValue,
                )
            )
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }

    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged", "SetTextI18n")
    private fun likeListObserver() {
        authViewModel.friendListData.observe(viewLifecycleOwner) { response ->
            mBinding.paginationLoader.visibility=View.GONE
            if (response != null) {
                mBinding.TvNoData.visibility = View.GONE
                mBinding.rvReportBlocked.visibility = View.VISIBLE
                mBinding.refresh.isRefreshing = false
                if (response.success) {
                    if (!response.data.isNullOrEmpty()) {
                        mBinding.TvNoData.visibility = View.GONE
                        if (refreshItems) {
                            list.clear()
                            uniqueList.clear()
                            refreshItems=false
                        }
                        list.addAll(response.data!!)
                        list.forEach {list->
                            if(uniqueList.any { it._id == list._id }){
                            }   else{
                                uniqueList.add(list)
                            }
                        }

                       /* if (sortByLike) {
                            uniqueList.sortByDescending {
                                it.overallLikes
                            }
                        }*//*else{
                            uniqueList.sortBy {
                                it.overallLikes
                            }
                        }*/
                        mBinding.rvReportBlocked.reStoreState()
                        adapter = FriendAdapter(uniqueList, this)
                        mBinding.rvReportBlocked.adapter = adapter
                        adapter.notifyDataSetChanged()
                    }else{
                        mBinding.rvReportBlocked.reStoreState()
                        adapter = FriendAdapter(uniqueList, this)
                        mBinding.rvReportBlocked.adapter = adapter
                        adapter.notifyDataSetChanged()
                        if (uniqueList.size == 0) {
                            mBinding.TvNoData.visibility = View.VISIBLE
                            if (isSearch){
                                mBinding.TvNoData.text = "No data found"
                            }else{
                                mBinding.TvNoData.text = "Accounts that follow you \nwill appear here"
                            }
                        } else {
                            mBinding.TvNoData.visibility = View.GONE
                        }
                    }
                }
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (isPagination) {
            } else {
                if (it) {
                    when {
                        refreshItems -> {

                        }
                        else -> {
                            mBinding.animationView.visibility = View.VISIBLE
                        }
                    }
                } else {
                    mBinding.animationView.visibility = View.GONE
                    mBinding.refresh.isRefreshing = false
                }
            }
        }

    }




    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onUserClick(position: Int, data: FriendListData) {
        val bundle = Bundle()
        bundle.putString("mentionUser", data._id)
        if (data._id == Prefs.init().userId){
            findNavController().navigate(
                R.id.profileFragment)
        }else{
            findNavController().navigate(
                R.id.otherProfileFragment,
                bundle
            )
        }
    }

    override fun onFollowAndUnfollow(position: Int, data: FriendListData) {

    }

    override fun onRequestCancel(position: Int, data: FriendListData) {

    }

    override fun onFollow(position: Int, data: FriendListData ,  requestStatus:String , accountType:String) {
        userId=data._id
        userProfile=data.profile.toString()
        profile = data.profile.toString()
        requestStatusForIndex=requestStatus
        if (data.accountType=="private"){
            if (requestStatus=="Unfollow"){
                updatePosition=position
                unFollowConfirmation(data.name)
            }else{
                if (isInternetAvailable(requireActivity())) {
                    authViewModel.followUnfollowUserRequest = UserRequest(userId)
                    authViewModel.followUnfollowUser()
                    //   setLogoutObserver()
                } else {
                    Utils.showToast("Please check your internet connection.")
                }
            }
        }else{
            if (isInternetAvailable(requireActivity())) {
                authViewModel.followUnfollowUserRequest = UserRequest(userId)
                authViewModel.followUnfollowUser()
                //   setLogoutObserver()
            } else {
                Utils.showToast("Please check your internet connection.")
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun unFollowConfirmation(userName: String) {
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialog?.setCancelable(true)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.are_you_sure_unfollow)
        val logout = dialog?.findViewById(R.id.Logout) as TextView
        val accountType = dialog?.findViewById(R.id.AccountType) as TextView
        val ivImage = dialog?.findViewById(R.id.ivUserProfileImage) as ImageView
        CoroutineScope(Dispatchers.Main).launch {
            ivImage.loadImageAsync(userProfile)
        }
        accountType.text= "“$userName” is a private account"
        logout.setOnClickListener {
            if (isInternetAvailable(requireActivity())) {
                authViewModel.followUnfollowUserRequest = UserRequest(userId)
                authViewModel.followUnfollowUser()

                /* adapter.list[position].followStatus=2
                 adapter.notifyItemChanged(position)*/
            } else {
                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }


        dialog?.show()
    }
}