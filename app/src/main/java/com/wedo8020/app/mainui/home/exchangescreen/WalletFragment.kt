package com.wedo8020.app.mainui.home.exchangescreen

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentWalletBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.exchangescreen.adapter.PurchaseHistoryAdapter
import com.wedo8020.app.request.UserListRequest
import com.wedo8020.app.response.ImpactData
import java.math.BigDecimal
import java.util.Objects


class WalletFragment : BaseFragment() {
    private lateinit var mBinding: FragmentWalletBinding
    private var srcValue: String?= null
    private var impactData : ImpactData? = null
    private lateinit var viewModel : PurchasesViewModel
    lateinit var adapter: PurchaseHistoryAdapter
    private var dialogs: Dialog?=null
    private lateinit var manager: RecyclerView.LayoutManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding=
            DataBindingUtil.inflate(layoutInflater,R.layout.fragment_wallet, container, false)
       hideNavBottom()
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        dialogs = Dialog(requireContext())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

       // srcValue = activity.impactData?.WD.toString()
//        val wdValue = srcValue
//        val roundedValue = if (wdValue != null) {
//            try {
//                val bdValue = BigDecimal(wdValue.toString())
//                bdValue.setScale(6, BigDecimal.ROUND_HALF_UP).toDouble()
//            } catch (e: NumberFormatException) {
//                0.0f
//            }
//        } else {
//            0.0
//        }
//        srcValue = roundedValue.toString()
//
//        val srcText = "$srcValue SRC"
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = findNavController()
            val previousDestinationId = navController.previousBackStackEntry?.destination?.id

            if (previousDestinationId == R.id.messageAndNotificationsFragment) {

                navController.navigateUp()
            } else {

               findNavController().navigate(R.id.xchangeFragment)
            }
        }
//        val spannableString = SpannableString(srcText)
//        val startIndex = srcText.indexOf("SRC")
//        spannableString.setSpan(
//            ForegroundColorSpan(Color.YELLOW),
//            startIndex,
//            startIndex + "SRC".length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )



        viewModel = ViewModelProvider(this)[PurchasesViewModel::class.java]
        if(isInternetAvailable(requireContext())){

            viewModel.getPurchasesHistory(UserListRequest(limit = 0, offset = 0, search = ""))
            viewModel.prizeDetails()
            viewModel.impactDetails()
        }
        else {
            showToast(getString(R.string.no_internet_message))
        }

        setRecyclerAdapter()
        mBinding.transferSrcButton.setOnClickListener {

            findNavController().navigate(R.id.transferSrcFragment, bundle)
        }
        mBinding.backButton.setOnClickListener {

            val previousDestinationId = findNavController().previousBackStackEntry?.destination?.id
            Log.d("navGraph", "$previousDestinationId and ${R.id.messageAndNotificationsFragment}")
            if (previousDestinationId == R.id.messageAndNotificationsFragment) {
                Log.d("navGraph", "I am coming from message and Chats")
                findNavController().navigateUp()
            } else {
                Log.d("navGraph", "I am not coming from message and Chats")
                findNavController().navigate(R.id.xchangeFragment)
            }
        }
        mBinding.detailText.text = "${activity.impactData?.wallet}"
        viewModel.purchaseHistoryResponse.observe(viewLifecycleOwner, Observer {
                response ->
            if(isInternetAvailable(requireContext())){
                if(response?.success == true){

                    Log.d("purchaseHistory", "history : ${response.data?.wd}")
                    mBinding.noRecyclerData.visibility = View.GONE
                    if(response.data?.transactions?.isNullOrEmpty() == true){
                        mBinding.noRecyclerData.visibility = View.VISIBLE
                        mBinding.animationLoader.visibility = View.GONE
                    }
                    else {
                        mBinding.animationLoader.visibility = View.GONE
                        response.data?.transactions?.let { adapter.setNewItems(it) }
                    }
                }
                else {
                    mBinding.animationLoader.visibility = View.GONE
                    mBinding.noRecyclerData.visibility = View.VISIBLE
                }
            }
            else {
                mBinding.animationLoader.visibility = View.GONE
                showToast(getString(R.string.no_internet_message))
            }



        })

        viewModel.mutableBaseResponse.observe(viewLifecycleOwner) { impactDetails ->

            if (impactDetails.success == true) {

                impactData = impactDetails.data
                srcValue =impactData?.WD.toString()
                val wdValue = srcValue
                val roundedValue = if (wdValue != null) {
                    try {
                        val bdValue = BigDecimal(wdValue.toString())
                        bdValue.setScale(6, BigDecimal.ROUND_HALF_UP).toDouble()
                    } catch (e: NumberFormatException) {
                        0.0f
                    }
                } else {
                    0.0
                }
                srcValue = roundedValue.toString()
                Log.d("ImpactData", impactDetails.data.toString())
                val srcText = "$srcValue SRC"
                bundle.putString("src", impactData?.WD.toString())
                val spannableString = SpannableString(srcText)
                val startIndex = srcText.indexOf("SRC")
                spannableString.setSpan(
                    ForegroundColorSpan(Color.YELLOW),
                    startIndex,
                    startIndex + "SRC".length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                mBinding.srcText.text = spannableString
                Log.d("updatedSrcis", "${impactDetails.data?.WD}")

            } else {

            }
        }


        Log.d("srcValueis", "src is : $srcValue")
    }
    private fun setRecyclerAdapter() {
        mBinding.apply {
            manager = LinearLayoutManager(requireContext())
            this.recyclerData.layoutManager = manager
            adapter = PurchaseHistoryAdapter(R.layout.purchase_history_recycler)

            mBinding.recyclerData.adapter = adapter

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding.srcText.text = ""
    }
    companion object{
        val bundle = Bundle()
    }
    private fun showProgressDialog() {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.progressbar)
        dialogs?.show()
    }


}