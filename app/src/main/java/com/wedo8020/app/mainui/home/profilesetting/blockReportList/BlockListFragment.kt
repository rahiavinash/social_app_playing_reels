package com.wedo8020.app.mainui.home.profilesetting.blockReportList

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentBlockReportListBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profilesetting.settingAdapter.BlockedAdapter
import com.wedo8020.app.request.BlockedRequest
import com.wedo8020.app.request.UserBlockedRequest
import com.wedo8020.app.response.BlockedUserData
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.loadImageAsyncMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Objects


class BlockListFragment : BaseFragment(), BlockedAdapter.OnClickListener {
    private lateinit var mBinding: FragmentBlockReportListBinding
    private val authViewModel by viewModels<PostViewModel>()
    private lateinit var adapter: BlockedAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    var dialog: Dialog? = null
    var dialogs: Dialog? = null
    var type = ""
    var offset = 0
    private var list: ArrayList<BlockedUserData> = ArrayList()
    private val uniqueList = arrayListOf<BlockedUserData>()
    private val idSet = HashSet<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_block_report_list,
            container,
            false
        )
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        callApiForGetList()
        setBlockObserver()
        unBlockedObserver()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = LinearLayoutManager(requireContext())
        mBinding.rvReportBlocked.layoutManager = manager



        mBinding.rvReportBlocked.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    offset += 10
                    callApiForGetList()

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

    private fun callApiForGetList() {
        if (isInternetAvailable(requireContext())) {
            showProgressDialog()
            authViewModel.userBlockedList(
                BlockedRequest(
                    offset = offset,
                    limit = 10,
                )
            )
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }

    private fun showProgressDialog() {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.progressbar)
        dialog?.show()
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun setBlockObserver() {
        authViewModel.userBlockedListData.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    dialog?.dismiss()
                    if (!response.data.isNullOrEmpty()) {
                        response.data.let {
                            list.addAll(response.data!!)
                            for (i in list) {
                                if (idSet.add(i._id)) { // Add IDs to the HashSet, duplicates won't be added
                                    uniqueList.add(i) // Add unique items to the new list
                                }
                            }
                            mBinding.rvReportBlocked.reStoreState()
                            adapter = BlockedAdapter(uniqueList, this)
                            mBinding.rvReportBlocked.adapter = adapter
                            adapter.notifyDataSetChanged()


                        }
                    } else {
                        if (list.size == 0) {
                            mBinding.TvNoData.text = "You have not blocked \nany accounts"
                        } else {
                            mBinding.TvNoData.visibility = View.GONE
                        }
                    }
                }
            }
        }
        authViewModel.showLoading.observe(requireActivity()) {
        }
        authViewModel.showMessage.observe(requireActivity()) {
            dialog!!.dismiss()
        }

    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onUnblockClick(position: Int, data: BlockedUserData) {
        userBlocked(data, position)
    }

    override fun onClickUserProfile(position: Int, data: BlockedUserData) {
        val bundle = Bundle()
        bundle.putString("mentionUser", data._id)
        if (data._id == Prefs.init().userId) {
            findNavController().navigate(R.id.profileFragment)
        } else {
            findNavController().navigate(
                R.id.otherProfileFragment,
                bundle
            )
        }
    }

    private fun unBlockedObserver() {
        authViewModel.baseResponse.observe(requireActivity()) { response ->
            dialog?.dismiss()
            if (response != null) {
                if (response.success) {
                    dialog?.dismiss()
                    Utils.showToast(response.message!!)
                    if (adapter.list.size == 0) {
                        mBinding.TvNoData.visibility = View.VISIBLE
                        mBinding.TvNoData.text = "You have not blocked \nany accounts"
                    } else {
                        mBinding.TvNoData.visibility = View.GONE
                    }
                }
            }
        }
        authViewModel.showMessage.observe(requireActivity()) {
            dialog?.dismiss()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeItem(position: Int) {
        uniqueList.removeAt(position) // Modify your list here
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun userBlocked(data: BlockedUserData, position: Int) {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.discard_post)
        dialogs?.setCanceledOnTouchOutside(true)
        val logout = dialogs?.findViewById(R.id.Logout) as TextView
        val mainText = dialogs?.findViewById(R.id.SureLoutout) as TextView
        val ivAlert = dialogs?.findViewById(R.id.ivAlert) as ImageView
        val ivUserProfileImage = dialogs?.findViewById(R.id.ivUserProfileImage) as ImageView
        // val ivClose = dialogs?.findViewById(R.id.ivClose) as ImageView
        //  ivClose.visibility=View.GONE
        ivAlert.visibility = View.GONE
        ivUserProfileImage.visibility = View.VISIBLE
        mainText.text = "Are you sure you want to Unblock \n \"${data.name}\"?"
        logout.text = "Unblock"
        CoroutineScope(Dispatchers.Main).launch {
            ivUserProfileImage.loadImageAsyncMessage(data.profile)
        }
        /* ivClose.setOnClickListener {
             dialogs?.dismiss()
         }*/
        logout.setOnClickListener {
            if (isInternetAvailable(requireActivity())) {
                authViewModel.userBlocked(UserBlockedRequest(data._id))
                removeItem(position)
            } else {

                Utils.showToast("Please check your internet connection.")
            }
            dialogs?.dismiss()
        }
        dialogs?.show()
    }
}