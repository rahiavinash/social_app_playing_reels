package com.wedo8020.app.mainui.home.profiletab.likeGivenAndReceive

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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentReceiveListBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profiletab.likeGivenAndReceive.adapter.GivenLikeAdapter
import com.wedo8020.app.request.LikeListRequest
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.response.LikeListResponseItem
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import java.util.Objects

private const val ARG_PARAM1 = "userID"

class ReceiveListFragment : BaseFragment(), GivenLikeAdapter.OnClickListener {
    private lateinit var mBinding: FragmentReceiveListBinding
    private val authViewModel by viewModels<PostViewModel>()
    private lateinit var adapter: GivenLikeAdapter
    private var isPagination = false
    private var sortByLike = false
    private var list: ArrayList<LikeListResponseItem> = ArrayList()
    private val uniqueList = arrayListOf<LikeListResponseItem>()
    var handlers = Handler(Looper.getMainLooper())
    var dialog: Dialog? = null
    var type = ""
    var offset = 0
    var searchValue = ""
    var isSearch = false
    private var userID: String? = null
    private var refreshItems = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userID = it.getString(ARG_PARAM1)
        }
        callApiForLikeList()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_receive_list,
            container,
            false
        )
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        hideNavBottom()

        likeListObserver()
        sortByLike = false
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOnOutSideClick(mBinding.clLikeReceive)

        /** Refresh the Data */
        mBinding.refresh.setOnRefreshListener {
            offset = 0
            refreshItems = true
            mBinding.animationView.visibility = View.GONE
            mBinding.TvNoData.visibility = View.GONE
            callApiForLikeList()
        }


        mBinding.rvReportBlocked.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    offset += 20
                    callApiForLikeList()
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
                    isSearch = true
                    handlers.removeCallbacksAndMessages(null)
                    handlers.postDelayed({
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (Utils.isInternetAvailables(requireActivity())) {
                                authViewModel.userLikeList(
                                    LikeListRequest(
                                        offset = offset,
                                        limit = 20,
                                        order = -1,
                                        search = searchValue,
                                        type = "received",
                                        user = userID
                                    )
                                )
                            } else {
                                Utils.showToast("Please check your internet connection.")
                            }
                        }, 0)

                    }, 200)
                }


            })
        }

        mBinding.llSortUP.setOnClickListener {
            list.clear()
            offset = 0
            if (mBinding.llSortDown.visibility == View.VISIBLE) {
                mBinding.llSortDown.visibility = View.GONE
                sortByLike = false
            } else {
                mBinding.llSortDown.visibility = View.VISIBLE
                sortByLike = true
            }
            if (isInternetAvailable(requireContext())) {
                authViewModel.userLikeList(
                    LikeListRequest(
                        offset = offset,
                        limit = 20,
                        order = -1,
                        search = searchValue,
                        type = "received",
                        user = userID
                    )
                )
            } else {
                Utils.showToast("Please check your internet connection.")
            }
        }

    }

    private fun callApiForLikeList() {
        if (isInternetAvailable(requireContext())) {
          //  mBinding.rvReportBlocked.visibility = View.GONE
            authViewModel.userLikeList(
                LikeListRequest(
                    offset = offset,
                    limit = 20,
                    order = -1,
                    search = searchValue,
                    type = "received",
                    user = userID
                )
            )
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }

    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged", "SetTextI18n")
    private fun likeListObserver() {
        authViewModel.likeListListData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    if (!response.data.isNullOrEmpty()) {
                        mBinding.TvNoData.visibility = View.GONE
                        mBinding.rvReportBlocked.visibility = View.VISIBLE
                        if (refreshItems) {
                            list.clear()
                            uniqueList.clear()
                            refreshItems=false
                        }
                        list.addAll(response.data!!)
                        list.forEach { list ->
                            if (uniqueList.any { it._id == list._id }) {
                            } else {
                                uniqueList.add(list)
                            }
                        }

                        if (sortByLike) {
                            uniqueList.sortByDescending {
                                it.overallLikes
                            }
                        } else {
                            uniqueList.sortBy {
                                it.overallLikes
                            }
                        }
                        mBinding.rvReportBlocked.reStoreState()
                        adapter = GivenLikeAdapter(uniqueList, this)
                        mBinding.rvReportBlocked.adapter = adapter
                        adapter.notifyDataSetChanged()
                    } else {
                        if (list.size == 0) {
                            mBinding.rvReportBlocked.reStoreState()
                            adapter = GivenLikeAdapter(uniqueList, this)
                            mBinding.rvReportBlocked.adapter = adapter
                            adapter.notifyDataSetChanged()
                            mBinding.TvNoData.visibility = View.VISIBLE
                            if (isSearch) {
                                mBinding.TvNoData.text = "No data found"
                            } else {
                                mBinding.TvNoData.text = "Likes received will appear here"
                            }
                        } else {
                            mBinding.TvNoData.visibility = View.GONE
                        }
                    }
                }

                mBinding.rvReportBlocked.visibility = View.VISIBLE
                mBinding.refresh.isRefreshing = false
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (isPagination) {
            } else {
                if (it) {
                    if (refreshItems) {

                    } else {
                        mBinding.animationView.visibility = View.VISIBLE
                    }
                } else {
                    mBinding.animationView.visibility = View.GONE
                    mBinding.refresh.isRefreshing = false
                }
            }

        }

        authViewModel.showMessage.observe(viewLifecycleOwner) {

        }
    }

    private fun showProgressDialog() {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.progressbar)
        dialog?.show()
    }


    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onUserClick(position: Int, data: LikeListResponseItem) {
        val bundle = Bundle()
        bundle.putString("mentionUser", data._id)
        if (data._id == Prefs.init().userId) {
            findNavController().navigate(
                R.id.profileFragment
            )
        } else {
            findNavController().navigate(
                R.id.otherProfileFragment,
                bundle
            )
        }
    }

    override fun onFollow(position: Int, data: LikeListResponseItem) {
        authViewModel.followUnfollowUserRequest = UserRequest(data._id)
        authViewModel.followUnfollowUser()
    }

    override fun onHearts(position: Int, data: LikeListResponseItem) {

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?) =
            ReceiveListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}