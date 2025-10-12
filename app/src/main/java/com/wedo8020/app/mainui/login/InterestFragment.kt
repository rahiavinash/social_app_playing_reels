package com.wedo8020.app.mainui.login

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
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
import com.wedo8020.app.databinding.FragmentInterestBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.activities.OnBoardingVideoActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.request.InterestRequest
import com.wedo8020.app.request.UpdateInterestRequest
import com.wedo8020.app.response.Category
import com.wedo8020.app.response.UserInterestData
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.isInternetAvailables
import java.util.Objects


class InterestFragment : BaseFragment(),ChooseInterestAdapter.OnclickListener {
    private lateinit var binding: FragmentInterestBinding
    private lateinit var adapter: ChooseInterestAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    private var interestList: ArrayList<Category> = ArrayList()
    private var selectInterest: ArrayList<String> = ArrayList()
    var dialog: Dialog? = null
    private var videoUrl : String?=null
    private val authViewModel by viewModels<PostViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_interest, container, false)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        callApiForGetInterest()
        getInterestObserver()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        manager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = manager
        binding.tvDone.setOnClickListener {
            binding.tvDone.disableBtn()
            if (Utils.isInternetAvailables(requireContext())) {
                Log.d("successApi", "selectedList : $selectInterest")

                binding.animationLoader.visibility = View.VISIBLE
                binding.tvDone.text=""

                authViewModel.updateInterest(
                    UpdateInterestRequest(
                        categories = selectInterest,
                        fromSetting = false
                    )
                )
                updateInterestObserver()
            } else {
                Utils.showToast("Please check your internet connection.")
            }

        }
        binding.IvBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun callApiForGetInterest() {
        if (isInternetAvailables(requireContext())) {
            authViewModel.getUserInterest(InterestRequest(country = Utils.getCountryNameFromCode()))
        } else {
            Utils.showToast("Please check your internet connection.")
        }
    }

    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged")
    private fun updateInterestObserver() {
        authViewModel.baseResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                   val intent = Intent(requireContext(), OnBoardingVideoActivity::class.java)
                    Log.e("PreviewFragmentCat", " video URI: $videoUrl")
                    intent.putExtra("videoUrl", videoUrl)
                    startActivity(intent)
                    binding.animationLoader.visibility = View.GONE
                    binding.tvDone.text="Done"
                    requireActivity().finish()

                }
                else {
                    binding.animationLoader.visibility = View.GONE
                    binding.tvDone.text="Done"

                }
            }
        }
        authViewModel.showLoading.observe(viewLifecycleOwner) {
            if (it){
                binding.animationLoader.visibility = View.VISIBLE
                binding.tvDone.text=""
            }else{
                binding.animationLoader.visibility = View.GONE
                binding.tvDone.text="Done"
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
                    videoUrl = response.data?.onBoardVideo?.video
                    response.data?.categories?.let { interestList.addAll(it) }
                    binding.recyclerView.reStoreState()
                    adapter = ChooseInterestAdapter(interestList, this)
                    binding.recyclerView.adapter = adapter
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
                    authViewModel.getUserInterest(InterestRequest(country = Utils.getCountryNameFromCode()))
                }
                // Perform actions when the internet is connected
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }
        return false
    }


}



