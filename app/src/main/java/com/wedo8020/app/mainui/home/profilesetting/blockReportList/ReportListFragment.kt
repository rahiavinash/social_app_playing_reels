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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentReportListBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profilesetting.settingAdapter.RestrictReportAdapter
import com.wedo8020.app.request.PostRequest
import com.wedo8020.app.request.RestrictReportRequest
import com.wedo8020.app.response.UserData
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import java.util.Objects


class ReportListFragment : BaseFragment(), RestrictReportAdapter.OnClickListener {
    private lateinit var mBinding: FragmentReportListBinding
    private val authViewModel by viewModels<PostViewModel>()
    private lateinit var adapterRestrict: RestrictReportAdapter
    var dialog: Dialog? = null
    private var reportList: ArrayList<UserData> = ArrayList()
    private val uniqueList = arrayListOf<UserData>()
    private val idSet = HashSet<String>()
    private var offset = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reportListApi()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_report_list, container, false)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        restrictObserver()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.rvReportBlocked.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    offset += 20
                    reportListApi()
                } else if(!recyclerView.canScrollVertically(-1) && dy < 0) {
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

    private fun showProgressDialog() {
       /* dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)!!.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.progressbar)
        dialog?.show()*/
    }


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun restrictObserver() {
        authViewModel.restrictResponseData.observe(viewLifecycleOwner, EventObserver { response ->
            if (response.success) {
                dialog?.dismiss()
                if (!response.data.isNullOrEmpty()){
                    response.data?.let {
                        reportList.addAll(response.data!!)
                        for (i in reportList) {
                            if (idSet.add(i._id)) { // Add IDs to the HashSet, duplicates won't be added
                                uniqueList.add(i) // Add unique items to the new list
                            }
                        }
                        mBinding.rvReportBlocked.reStoreState()
                        adapterRestrict = RestrictReportAdapter(uniqueList, this)
                        adapterRestrict.type = "report"
                        mBinding.rvReportBlocked.adapter = adapterRestrict
                        adapterRestrict.notifyDataSetChanged()



                    }
                }else{
                    if (reportList.size == 0) {
                        mBinding.TvNoData.text = "Reported users will \nappear here."
                    } else {
                        mBinding.TvNoData.visibility = View.GONE
                    }
                }




            }
        })
        authViewModel.showMessage.observe(viewLifecycleOwner) {
            dialog!!.dismiss()
        }
    }

    private fun reportListApi() {
        if (isInternetAvailable(requireContext())) {
            showProgressDialog()
            authViewModel.restrictedReportUser(
                RestrictReportRequest(
                    offset = offset,
                    limit = 20,
                    search = "",
                    type = "report"
                )
            )
        } else {
            dialog!!.dismiss()
        }
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onClick(position: Int, data: UserData) {
        if (data.reportedType == "user") {
            authViewModel.postReportRequest = PostRequest(
                user = data._id,
                reason = data.reason
            )
            authViewModel.userReport()
        } else {
            authViewModel.postReportRequest = PostRequest(
                post = data._id,
                reason = data.reason
            )
            authViewModel.postReport()

        }
        unReportObserver()
        removeItem(position)
    }

    override fun onClickUserProfile(position: Int, data: UserData) {
        val bundle = Bundle()
        bundle.putString("mentionUser", data.creator?._id
        )
        if (data.creator?._id == Prefs.init().userId){
            findNavController().navigate(R.id.profileFragment)
        }else{
            findNavController().navigate(
                R.id.otherProfileFragment,
                bundle
            )
        }

    }

    private fun unReportObserver() {
        authViewModel.postReportData.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    dialog?.dismiss()
                    Utils.showToast(response.message!!)
                    if (adapterRestrict.list.size == 0) {
                        mBinding.TvNoData.visibility = View.VISIBLE
                        mBinding.TvNoData.text = "Restricted users will \nappear here."
                    } else {
                        mBinding.TvNoData.visibility = View.GONE
                    }
                }
            }
        }
        authViewModel.showMessage.observe(viewLifecycleOwner) {
            dialog!!.dismiss()
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    private fun removeItem(position: Int) {
        uniqueList.removeAt(position) // Modify your list here
        adapterRestrict.notifyDataSetChanged()
    }

   /* override fun onPause() {
        super.onPause()
        adapterRestrict.list.let { reportList=it }
    }*/
    override fun onResume() {
        super.onResume()
        mBinding.rvReportBlocked.reStoreState()
        adapterRestrict = RestrictReportAdapter(uniqueList,this)
        adapterRestrict.type="report"
        mBinding.rvReportBlocked.adapter = adapterRestrict
    }
}