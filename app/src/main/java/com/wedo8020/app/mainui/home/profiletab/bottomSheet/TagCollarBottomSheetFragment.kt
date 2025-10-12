package com.wedo8020.app.mainui.home.profiletab.bottomSheet


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentTagCollabBottomSheetBinding
import com.wedo8020.app.mainui.home.profiletab.mentionAdapter.TagSelectedAdapter
import com.wedo8020.app.mainui.home.profiletab.mentionAdapter.TagCollaborationAdapter
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.UserListRequest
import com.wedo8020.app.response.TagUsers
import com.wedo8020.app.response.UserList
import com.wedo8020.app.utils.MessageEvent
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.isInternetAvailables
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.wedo8020.app.utils.Utils.ignoreFirstWhiteSpace


class TagCollarBottomSheetFragment : BottomSheetDialogFragment(),
    TagCollaborationAdapter.OnClickListener, TagSelectedAdapter.OnClickListener {
    private lateinit var mBinding: FragmentTagCollabBottomSheetBinding
    private val authViewModel by viewModels<PostViewModel>()
    lateinit var adapter: TagCollaborationAdapter
    private lateinit var adapterSecond: TagSelectedAdapter
    private var userList: ArrayList<UserList> = ArrayList()
    private var tagList: ArrayList<UserList> = ArrayList()
    private val gSons = Gson()
    private var offset = 0
    private var limit = 20
    private var selectedTagList: ArrayList<TagUsers> = ArrayList()
    private var sFTagListData: ArrayList<UserList>? = null
    var handler = Handler(Looper.getMainLooper())
    var searchValue=""
    var isSearch=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_tag__collab__bottom_sheet_,
            container,
            false
        )

        val type = object : TypeToken<ArrayList<TagUsers>>() {}.type
        if (!arguments?.getString("tagList").isNullOrEmpty()) {
            selectedTagList = Gson().fromJson(arguments?.getString("tagList"), type)
        }
        val types = object : TypeToken<ArrayList<UserList>>() {}.type
        sFTagListData = Gson().fromJson(Prefs.init().TagList, types)
        callApi()
        setTagObserver()
        disableBottomSheetDraggableBehavior()
        return mBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        // Set behavior to stay open when touched inside and close when touched outside
        dialog.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    //  dismiss() // Dismiss the bottom sheet when it is hidden
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Handle sliding behavior if needed
            }
        })

        return dialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.searchEt.filters= ignoreFirstWhiteSpace(mBinding.searchEt,60)
        mBinding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userList.clear()
                offset=0
                searchValue = s.toString()
                if (isInternetAvailables(requireContext())) {
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        authViewModel.userList =
                            UserListRequest(limit = limit, offset = 0, search = searchValue)
                        authViewModel.userList()
                        isSearch=true
                    }, 100)
                } else {
                    Utils.showToast("Please check your internet connection.")
                }
            }

            override fun afterTextChanged(s: Editable?) {


                // setPinObserver()
            }
        })
        mBinding.tvDone.setOnClickListener {
            val json = gSons.toJson(tagList)
            Prefs.init().TagList = json
            org.greenrobot.eventbus.EventBus.getDefault()
                .post(MessageEvent(""))
            dismiss()
        }
     /*   mBinding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // on scroll change we are checking when users scroll as bottom.
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                offset += 10
                loadMoreData()
            }
        })*/
        mBinding.rvFriend.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    offset += 20
                    loadMoreData()

                } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {

                }

            }})
        /*   mBinding.rvFriend.addOnScrollListener(object : RecyclerView.OnScrollListener() {
               override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                   if (!recyclerView.canScrollVertically(1) && dy > 0) {
                       offset += 10
                       loadMoreData()

                   } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {

                   }

               }


        *//*       override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {

                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                } else {

                }

            }*//*


        }

        )*/



    }



    fun callApi() {
        if (isInternetAvailables(requireContext())) {
            authViewModel.userList =
                UserListRequest(limit = 20, offset = 0, search = "")
            authViewModel.userList()
        } else {
            //     dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        mBinding.rvFriend.reStoreState()
        mBinding.rvFriend.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = TagCollaborationAdapter(userList, this, tagList)
        mBinding.rvFriend.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun loadMoreData() {
        if (isInternetAvailables(requireContext())) {
            authViewModel.userList =
                UserListRequest(limit = limit, offset = offset, search = searchValue)
            authViewModel.userList()
        } else {
            //  dialogs?.dismiss()
            Utils.showToast("Please check your internet connection.")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setTagObserver() {
        authViewModel.userListResponseData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    //        dialogs?.dismiss()
                    response.data?.let { userList.addAll(it) }
                    if (isSearch) {
                        isSearch = false
                    } else {
                        selectedTagList.forEach { it ->
                            val tagId = it._id
                            response.data!!.forEach {
                                if (tagId == it._id) {
                                    tagList.add(it)
                                }
                            }
                        }
                    }
                    if (Prefs.init().TagList.isNotEmpty()) {
                        tagList = sFTagListData!!
                    }
                    mBinding.rvTaggedPerson.layoutManager = LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    adapterSecond = TagSelectedAdapter(tagList, this)
                    mBinding.rvTaggedPerson.reStoreState()
                    mBinding.rvTaggedPerson.adapter = adapterSecond
                    adapterSecond.notifyDataSetChanged()



                    mBinding.rvFriend.reStoreState()
                    mBinding.tvNoData.visibility = View.GONE
                    adapter = TagCollaborationAdapter(userList , this, tagList)
                    mBinding.rvFriend.adapter = adapter
                    adapter.notifyDataSetChanged()

                    if(adapter.list.size <= 0){
                        mBinding.tvNoData.visibility = View.VISIBLE
                    }
                    else {
                        mBinding.tvNoData.visibility = View.GONE
                    }
                }
            }
        }

        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (it) {
                //showProgressDialog()
                mBinding.animationLoader.visibility = View.VISIBLE
            } else {
                mBinding.animationLoader.visibility = View.GONE
            }

        }
    }


    private fun disableBottomSheetDraggableBehavior() {
        this.isCancelable = false
        this.dialog?.setCanceledOnTouchOutside(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClickTag(position: Int, data: UserList) {
        val types = object : TypeToken<ArrayList<UserList>>() {}.type
        sFTagListData = Gson().fromJson(Prefs.init().TagList, types)


        requireActivity().runOnUiThread {
            if (tagList.any { it._id == data._id }) {
                tagList.removeAll { it._id == data._id }
            } else {
                tagList.add(data)
            }
            adapterSecond.notifyDataSetChanged()
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onRemoveTag(position: Int, data: UserList) {
        userList.forEach {
            if (it._id == data._id) {
                adapter.isTagActiveId = data._id
                data.isTagSelected = false
                it.isTagSelected = false

                if (!sFTagListData.isNullOrEmpty()) {
                    adapter.removeTag = true
                }
                adapter.notifyDataSetChanged()
                adapterSecond.notifyDataSetChanged()
            }
        }

    }


}