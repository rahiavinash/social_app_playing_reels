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
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentGivenLikeListBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profiletab.likeGivenAndReceive.adapter.GivenLikeAdapter

import com.wedo8020.app.request.LikeListRequest
import com.wedo8020.app.request.RemoveLikes
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.response.LikeListResponseItem
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects

private const val ARG_PARAM1 = "userID"

class GivenLikeListFragment : BaseFragment(), GivenLikeAdapter.OnClickListener {

private lateinit var mBinding:FragmentGivenLikeListBinding
private val authViewModel by viewModels<PostViewModel>()
    var dialog: Dialog? = null
    var dialogs: Dialog? = null
    private lateinit var adapter: GivenLikeAdapter
    var type = ""
    var offset = 0
    private var removePosition = 0
    var searchValue=""
    var isSearch=false
    var  isPagination = false
    private var sortByLike = false
    private var refreshItems = false
    private var list: ArrayList<LikeListResponseItem> = ArrayList()
    private var sortList: ArrayList<LikeListResponseItem> = ArrayList()
    private val uniqueList = arrayListOf<LikeListResponseItem>()
    private val idSet = HashSet<String>()
    var handlers = Handler(Looper.getMainLooper())
    private var userID: String?=null
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
        mBinding=DataBindingUtil.inflate(layoutInflater,R.layout.fragment_given_like_list, container, false )
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        hideNavBottom()
        likeListObserver()
        hideKeyboardOnOutSideClick(mBinding.clLikeGiven)
        return mBinding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** Refresh the Data */
        mBinding.refresh.setOnRefreshListener {
            offset=0
            refreshItems=true
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

                @SuppressLint("SuspiciousIndentation")
                override fun afterTextChanged(p0: Editable?) {
                    list.clear()
                    uniqueList.clear()
                    searchValue = p0.toString()
                        handlers.removeCallbacksAndMessages(null)
                        handlers.postDelayed({
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (Utils.isInternetAvailables(requireActivity())) {
                                    isSearch=true
                                authViewModel.userLikeList(LikeListRequest(
                                    offset=  offset,
                                    limit=20,
                                    order=-1,
                                    search=searchValue,
                                    type="given",
                                    user = userID
                                ))
                                } else {
                                    Utils.showToast("Please check your internet connection.")
                                }
                            }, 0)

                        }, 50)

                }


            })
        }

        mBinding.llSortUP.setOnClickListener {
            list.clear()
            uniqueList.clear()
            offset=0
            searchValue
            if (mBinding.llSortDown.visibility == View.VISIBLE) {
                mBinding.llSortDown.visibility = View.GONE
                sortByLike = false
            } else {
                mBinding.llSortDown.visibility = View.VISIBLE
                sortByLike = true
            }
            if (isInternetAvailable(requireContext())){
                mBinding.rvReportBlocked.visibility = View.GONE
                authViewModel.userLikeList(LikeListRequest(
                    offset=  offset,
                    limit=20,
                    order=-1,
                    search=searchValue,
                    type="given",
                    user = userID
                ))
            }else{
                Utils.showToast("Please check your internet connection.")
            }

        }

    }
private fun callApiForLikeList(){
    if (isInternetAvailable(requireContext())){
     //   mBinding.rvReportBlocked.visibility = View.GONE
        authViewModel.userLikeList(LikeListRequest(
            offset = offset,
            limit = 20,
            order = -1,
            search = "",
            type = "given",
            user = userID
        ))
    }else{
        Utils.showToast("Please check your internet connection.")
    }
}

    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged", "SetTextI18n")
    private fun likeListObserver() {
        authViewModel.likeListListData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    if (!response.data.isNullOrEmpty()){
                        mBinding.TvNoData.visibility = View.GONE
                        mBinding.rvReportBlocked.visibility = View.VISIBLE
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
                        }*/

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
                    }else{
                            if (list.size == 0) {
                                uniqueList.clear()
                                adapter = GivenLikeAdapter(uniqueList, this)
                                mBinding.rvReportBlocked.adapter = adapter
                                adapter.notifyDataSetChanged()
                                if (!isSearch){
                                    mBinding.TvNoData.visibility = View.VISIBLE
                                    mBinding.TvNoData.text = "Likes given will appear here"
                                }else{
                                    mBinding.TvNoData.visibility = View.VISIBLE
                                    mBinding.TvNoData.text = "No data found"
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
            when {
                isPagination -> {
                }
                else -> {
                    if (it) {
                        if (refreshItems){

                        }else{
                            mBinding.animationView.visibility = View.VISIBLE
                        }
                    } else {
                        mBinding.animationView.visibility = View.GONE
                        mBinding.refresh.isRefreshing = false
                    }
                }
            }

        }

        authViewModel.showMessage.observe(viewLifecycleOwner) {

        }
    }
  /*  private fun showProgressDialog() {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.progressbar)
        dialog?.show()
    }*/


    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onUserClick(position: Int, data: LikeListResponseItem) {
        val bundle = Bundle()
        bundle.putString("mentionUser", data._id)
        if (data._id == Prefs.init().userId){
            findNavController().navigate(R.id.profileFragment)
        }else{
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
        removePosition=position
        likeRemove(data)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?) =
            GivenLikeListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }


    private fun likeRemove(data: LikeListResponseItem) {
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.remove_likes_from_user)
        val tvUserName = dialogs?.findViewById(R.id.tvUserName) as TextView
        val yesIdo = dialogs?.findViewById(R.id.yesIdo) as TextView
        val tvNa = dialogs?.findViewById(R.id.tvNa) as TextView
   /*     val ivClose = dialogs?.findViewById(R.id.ivClose) as ImageView*/
        val ivUserImage = dialogs?.findViewById(R.id.ivUserImage) as ImageView
        if(data.profile!=null){
            CoroutineScope(Dispatchers.Main).launch {
                ivUserImage.loadImageAsyncMessage(data.profile)
            }
        }

        tvUserName.text=data.username
        yesIdo.setOnClickListener {
            yesIdo.disableBtn()
            tvNa.disableBtn()
            if(isInternetAvailable(requireActivity())) {
                authViewModel.removeLikesFromUser(RemoveLikes(user =data._id ))
                removeLikeObserver()
                adapter.removeItem(removePosition)
            } else {
                Utils.showToast("Please check your internet connection.")
            }
        }

        tvNa.setOnClickListener {
            yesIdo.disableBtn()
            tvNa.disableBtn()
            dialogs?.dismiss()
        }

        dialogs?.show()
    }
    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged", "SetTextI18n")
    private fun removeLikeObserver() {
        authViewModel.baseResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    if (adapter.list.size ==0){
                        mBinding.TvNoData.visibility = View.VISIBLE
                        mBinding.TvNoData.text = "Likes given will appear here"
                    }
                    dialogs?.dismiss()
                    response.message?.let { Utils.showToast(it) }
                }
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
           /* if (it) {
                mBinding.animationView.visibility = View.VISIBLE
            }else{
                mBinding.animationView.visibility = View.GONE
            }*/

        }

        authViewModel.showMessage.observe(viewLifecycleOwner) {

        }
    }
}