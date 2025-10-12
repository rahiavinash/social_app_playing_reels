package com.wedo8020.app.mainui.home.profiletab.linkedAccount

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentLinkedAccountBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.LinkedAccountRequest
import com.wedo8020.app.response.Users
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Utils


class LinkedAccountFragment : BaseFragment() {
    private lateinit var mBinding: FragmentLinkedAccountBinding
    private val viewModel by lazy { viewModels<PostViewModel>().value }
    private var limit = 30
    private var offset = 0
    private var order = -1
    private var search = ""
    private var linkedAccountList: ArrayList<Users> = ArrayList()
    private lateinit var adapter: LinkedAdapter
    private lateinit var manager: LinearLayoutManager
    var handlers = Handler(Looper.getMainLooper())
    private var pagination = false
    private var isSearch = false
    private var sortByLike = false
    private var isCallApi = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isCallApi) {
            getLinkedAccountDetails("")
            isCallApi = true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_linked_account,
            container,
            false
        )
        hideKeyboardOnOutSideClick(mBinding.clLinkedAccount)
        hideNavBottom()
        return mBinding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()

        setMentionRecyclerViewAdapter()
        mBinding.let {
            it.IvBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
        mBinding.rvLinkedAccount.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    if (dy > 0) { // Check if scrolling down
                        offset += 30
                        pagination = true
                        if (isInternetAvailable(requireContext())) {
                            getLinkedAccountDetails("")
                            Log.e("getChatHistory", "onScroll")
                        } else {
                            Utils.showToast("Please check your internet connection.")
                        }
                    }
                }
            }

        })

        mBinding.llSortUP.setOnClickListener {
            adapter.list.clear()
            linkedAccountList.clear()
            adapter.notifyDataSetChanged()
            offset = 0
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
            getLinkedAccountDetails("")
        }

        mBinding.searchEt.filters = Utils.ignoreFirstWhiteSpace(mBinding.searchEt, 60)
        mBinding.searchEt.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    offset = 0
                    if (search != p0.toString()) {
                        adapter.list.clear()
                        mBinding.rvLinkedAccount.visibility = View.GONE
                        search = p0.toString()
                        Log.e("isSearch" , search.length.toString())
                        isSearch = true
                        if (Utils.isInternetAvailables(requireActivity())) {
                            handlers.removeCallbacksAndMessages(null)
                            handlers.postDelayed({
                                Handler(Looper.getMainLooper()).postDelayed({
                                    getLinkedAccountDetails(search)
                                }, 0)
                            }, 400)
                        } else {
                            Utils.showToast("Please check your internet connection.")
                        }
                    }
                }

            })
        }


    }

    private fun getLinkedAccountDetails(search: String) {
        checkIfFragmentAttached {
            if (isInternetAvailable(requireContext())) {
                viewModel.getLinkedAccountDetails(
                    LinkedAccountRequest(
                        limit = limit,
                        offset = offset,
                        search = search,
                        order = order
                    )
                )
            } else {
                showToast(getString(R.string.no_internet_message))
            }
        }
    }

    private fun setMentionRecyclerViewAdapter() {
        mBinding.apply {

            manager = LinearLayoutManager(requireContext())
            rvLinkedAccount.layoutManager = manager
            adapter =
                LinkedAdapter(R.layout.linked_account_rv_list,
                    object :
                        LinkedAdapter.OnClickLinkedAccount {
                        override fun onClick(id: String, username: String) {
                            /*    mentionUserHash["@${username}"] = id
                                commentEt.text =
                                    commentEt.text.replace(startIndex, endIndex, "@${username} ")
                                commentEt.setSelection(commentEt.text.toString().length)
                                binding.hashtagMentionListContainer.visibility = View.GONE
                                binding.container.visibility = View.VISIBLE*/
                        }


                        override fun onClickProfile(position: Int, data: Users) {
                            val bundle = Bundle()
                            bundle.also {
                                it.putString("mentionUser", data._id)
                                findNavController().navigate(
                                    R.id.otherProfileFragment,
                                    it
                                )
                            }
                        }
                    })
            rvLinkedAccount.adapter = adapter
            adapter.setNewItems(linkedAccountList)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setObserver() {
        viewModel.linkedAccountDetailsData.observe(viewLifecycleOwner) { response ->
            mBinding.animationView.visibility = View.GONE
            if (response.success) {
                if (response.data != null) {
                    mBinding.tvSharedLink.visibility = View.GONE
                    mBinding.tvInviteSomeone.visibility = View.GONE
                    mBinding.rvLinkedAccount.visibility = View.VISIBLE
                    mBinding.TvNoData.visibility = View.GONE
                    linkedAccountList.addAll(response.data!!.users)
                    /*if (sortByLike){
                            linkedAccountList.sortByDescending { it.totalDailyAR }
                    }*/
                    if (adapter.list.size > 0) {
                        adapter.addNewItems(response.data!!.users)
                    } else {
                        adapter.setNewItems(response.data!!.users)
                    }
                    mBinding.tvSrcCount.text =
                        response.data!!.totalBonus.toString() + " " + AppConstant.SRC
                } else {
                    if (adapter.list.size == 0) {
                        mBinding.TvNoData.visibility = View.VISIBLE
                        if (isSearch) {
                            mBinding.TvNoData.text = "No data found"
                            mBinding.tvSharedLink.visibility = View.GONE
                            mBinding.tvInviteSomeone.visibility = View.GONE
                        } else {
                            mBinding.tvSharedLink.visibility = View.VISIBLE
                            mBinding.tvInviteSomeone.visibility = View.VISIBLE
                        }
                    } else {
                        mBinding.tvSharedLink.visibility = View.GONE
                        mBinding.tvInviteSomeone.visibility = View.GONE
                        mBinding.TvNoData.visibility = View.GONE
                    }
                    // adapter.setNewItems(linkedAccountList)

                }

            }
        }

        viewModel.showLoading.observe(viewLifecycleOwner) {
            when {
                pagination -> {
                }

                else -> {
                    if (it) {
                        mBinding.animationView.visibility = View.VISIBLE

                    } else {
                        mBinding.animationView.visibility = View.GONE

                    }

                }
            }

        }
        viewModel.showMessage.observe(viewLifecycleOwner) {

        }
    }
}
