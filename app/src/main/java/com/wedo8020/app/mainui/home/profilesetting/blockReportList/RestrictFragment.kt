package com.wedo8020.app.mainui.home.profilesetting.blockReportList

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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentRestrictBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profilesetting.settingAdapter.RestrictReportAdapter
import com.wedo8020.app.request.PostRestrict
import com.wedo8020.app.request.RestrictReportRequest
import com.wedo8020.app.request.UserRestrict
import com.wedo8020.app.response.UserData
import com.wedo8020.app.utils.EventObserver
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import java.util.Objects

/**
 * A simple [Fragment] subclass.
 * Use the [RestrictFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RestrictFragment : BaseFragment(),RestrictReportAdapter.OnClickListener {
    private lateinit var mBinding: FragmentRestrictBinding
    private val authViewModel by viewModels<PostViewModel>()
    private lateinit var adapterRestrict: RestrictReportAdapter
    var dialog: Dialog? = null
    private var offset=0
    private var onClickBtn=false
    private var restrictList: ArrayList<UserData> = ArrayList()
    private val uniqueList = arrayListOf<UserData>()
    private val idSet = HashSet<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_restrict, container, false)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        restrictListApi()
        restrictObserver()
        return mBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.rvReportBlocked.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    offset += 20
                    restrictListApi()
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
    private fun restrictObserver() {
        authViewModel.restrictResponseData.observe(viewLifecycleOwner, EventObserver { response ->
            if (response.success) {
                dialog?.dismiss()
                if (!response.data.isNullOrEmpty()){
                    response.data?.let {
                        /*   if (onClickBtn){
                               restrictList.clear()
                               onClickBtn=false
                           }*/
                        restrictList.addAll(response.data!!)
                        for (i in restrictList) {
                            if (idSet.add(i._id)) { // Add IDs to the HashSet, duplicates won't be added
                                uniqueList.add(i) // Add unique items to the new list
                            }
                        }
                        mBinding.rvReportBlocked.reStoreState()
                        adapterRestrict = RestrictReportAdapter(uniqueList,this)
                        adapterRestrict.type="restrict"
                        mBinding.rvReportBlocked.adapter = adapterRestrict
                        adapterRestrict.notifyDataSetChanged()

                    }
                }else{
                    if (restrictList.size == 0) {
                        mBinding.TvNoData.text = "Restricted users will \nappear here."
                    } else {
                        mBinding.TvNoData.visibility = View.GONE
                    }
                }

            }
        })
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

    override fun onClick(position: Int, data: UserData) {
        onClickBtn=true
        if (!data._id.isNullOrEmpty()){
            if (data.reportedType=="post"){
                authViewModel.postRestrict(PostRestrict(post = data._id))
            }else{
              //  showProgressDialog()
                authViewModel.userRestrict(UserRestrict( id = data._id))
            }
        }
        unRestrictObserver()
        removeItem(position)
    }

    override fun onClickUserProfile(position: Int, data: UserData) {
        val bundle = Bundle()
        if (data.reportedType=="user"){
            bundle.putString("mentionUser", data._id)
        }else{
            bundle.putString("mentionUser", data.creator?._id)
        }

        if (data.creator?._id == Prefs.init().userId){
            findNavController().navigate(R.id.profileFragment)
        }else{
            findNavController().navigate(
                R.id.otherProfileFragment,
                bundle
            )
        }
    }

    private fun restrictListApi(){
        if (isInternetAvailable(requireContext())) {
           // showProgressDialog()
            authViewModel.restrictedReportUser(
                RestrictReportRequest(
                    offset = offset,
                    limit = 20,
                    search = "",
                    type = "restrict"
                )
            )
        } else {
            dialog!!.dismiss()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun unRestrictObserver() {
        authViewModel.userRestrictData.observe(viewLifecycleOwner, EventObserver { response ->
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
        })
        authViewModel.showLoading.observe(requireActivity()) {
        }
        authViewModel.showMessage.observe(requireActivity()) {
            dialog!!.dismiss()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeItem(position: Int) {
        uniqueList.removeAt(position) // Modify your list here
        adapterRestrict.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()
        mBinding.rvReportBlocked.reStoreState()
        adapterRestrict = RestrictReportAdapter(uniqueList,this)
        adapterRestrict.type="restrict"
        mBinding.rvReportBlocked.adapter = adapterRestrict
    }
}