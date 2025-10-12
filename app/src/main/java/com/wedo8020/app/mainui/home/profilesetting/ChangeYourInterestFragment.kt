package com.wedo8020.app.mainui.home.profilesetting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentChangeYourInterestBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.home.profilesetting.settingAdapter.InterestAdapter
import com.wedo8020.app.request.InterestRequest
import com.wedo8020.app.request.UpdateInterestRequest
import com.wedo8020.app.response.Category
import com.wedo8020.app.response.UserInterestData
import com.wedo8020.app.utils.Utils
import java.util.Objects

class ChangeYourInterestFragment : BaseFragment(), InterestAdapter.OnclickListener {
    private lateinit var mBinding: FragmentChangeYourInterestBinding
    private val authViewModel by viewModels<PostViewModel>()
    private lateinit var adapter: InterestAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    private var interestList: ArrayList<Category> = ArrayList()
    private var selectInterest: ArrayList<String> = ArrayList()
    var dialog: Dialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_change_your_interest,
            container,
            false
        )
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        callApiForGetInterest()
        getInterestObserver()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        mBinding.recyclerView.layoutManager = manager
        mBinding.tvDone.setOnClickListener {
            mBinding.tvDone.disableBtn()
            if (isInternetAvailable(requireContext())) {
                showProgressDialog()
                authViewModel.updateInterest(
                    UpdateInterestRequest(
                        categories = selectInterest,
                        fromSetting = true
                    )
                )
                updateInterestObserver()
            } else {
                Utils.showToast("Please check your internet connection.")
            }

        }
        mBinding.IvBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun callApiForGetInterest() {
        if (isInternetAvailable(requireContext())) {
            authViewModel.getUserInterest(InterestRequest(country = Utils.getCountryNameFromCode()) )
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }

    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged")
    private fun updateInterestObserver() {
        authViewModel.baseResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    findNavController().navigateUp()
                    //   Utils.showToast(response.message!!)
                }
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (it){
                showProgressDialog()
            }else{
                dialog!!.dismiss()
            }
        }

        authViewModel.showMessage.observe(viewLifecycleOwner) {

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getInterestObserver() {
        authViewModel.interestResponseData.observe(viewLifecycleOwner) { response ->

            if (response != null) {
                if (response.success) {
                    dialog?.dismiss()
                    interestList.clear()


                    response.data?.categories?.let { interestList.addAll(it) }
                    Log.e("ASAsASASAS" , interestList.size.toString())
               //     interestList.addAll(response.data!!)
                    mBinding.recyclerView.reStoreState()
                    adapter = InterestAdapter(interestList, this)
                    mBinding.recyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

                val selectedIds = interestList.filter { it.selected }.map { it._id }
                selectInterest.addAll(selectedIds)
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
        }

        authViewModel.showMessage.observe(viewLifecycleOwner) {
              dialog?.dismiss()
        }
    }

    private fun RecyclerView.reStoreState() {
        val recyclerViewState = this.layoutManager?.onSaveInstanceState()
        this.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onClickInterest(position: Int, data: Category) {
        if (selectInterest.contains(data._id)) {
            selectInterest.remove(data._id)
        } else {
            selectInterest.add(data._id)
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

    override fun onResume() {
        super.onResume()

        requireActivity().registerReceiver(
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isConnected(context)) {
                // Internet is connected
                //  Utils.showToast("Internet connected")
                // showProgressDialog()
                if (selectInterest.isNullOrEmpty()) {
                    authViewModel.getUserInterest(InterestRequest(country = Utils.getCountryNameFromCode()) )
                }

                // Perform actions when the internet is connected
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }
        return false
    }
}