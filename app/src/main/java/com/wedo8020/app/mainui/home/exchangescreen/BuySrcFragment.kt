package com.wedo8020.app.mainui.home.exchangescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.wedo8020.app.databinding.FragmentBuySrcBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.R
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.FIVE_SRC_ID
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.ONE_SRC_ID
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.TEN_SRC_ID
import com.wedo8020.app.utils.Utils.disableMultiTap


class BuySrcFragment : BaseFragment() {

    private var _binding: FragmentBuySrcBinding? = null
    private val binding get() = _binding!!
    private var srcValue : String ? =null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuySrcBinding.inflate(inflater, container, false)
        hideNavBottom()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        val purchaseManager = mainActivity.getPurchaseManager()



        binding.nineDollar.text = mainActivity.inAppProductsPrice.get("com.wedo.src.type1") ?: "N/A"
        binding.fortyFourDollar.text = mainActivity.inAppProductsPrice.get("com.wedo.src.type2") ?: "N/A"
        binding.nineNineNineSevenNine.text = mainActivity.inAppProductsPrice.get("com.wedo.src.type3") ?: "N/A"



        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.xchangeFragment)
        }
        binding.moreInfoText.setOnClickListener {
            buySrcMoreInfo()
        }

        binding.constraintLayout.setOnClickListener {
//            srcValue = "1"
//            val purchaseTypeString = PurchaseType.SrcPurchases
//
//
//            val inputData = Data.Builder()
//                .putString("srcValue", srcValue)
//                .putString("id", "com.wedo.src.type1")
//                .putString("purchaseType", purchaseTypeString.toString())
//                .build()
//
//            val constraints = Constraints.Builder()
//               .setRequiredNetworkType(NetworkType.CONNECTED)
//
//                .build()
//
//            val workRequest = OneTimeWorkRequest.Builder(PurchaseWorker::class.java)
//                .setInputData(inputData)
//                .setConstraints(constraints)
//                .build()
//            WorkManager.getInstance(requireContext()).enqueue(workRequest)
            binding.constraintLayout.disableMultiTap()
            binding.constraintLayoutOne.disableMultiTap()
            binding.constraintLayoutTwo.disableMultiTap()
           purchaseManager.purchaseItem(ONE_SRC_ID, purchaseType = PurchaseType.SrcPurchases, wd = 1)
        }
        binding.constraintLayoutOne.setOnClickListener {
            binding.constraintLayout.disableMultiTap()
            binding.constraintLayoutOne.disableMultiTap()
            binding.constraintLayoutTwo.disableMultiTap()
            purchaseManager.purchaseItem(FIVE_SRC_ID, purchaseType = PurchaseType.SrcPurchases, wd = 5)
        }
        binding.constraintLayoutTwo.setOnClickListener {
            binding.constraintLayout.disableMultiTap()
            binding.constraintLayoutOne.disableMultiTap()
            binding.constraintLayoutTwo.disableMultiTap()
            purchaseManager.purchaseItem(TEN_SRC_ID, purchaseType = PurchaseType.SrcPurchases, wd = 10)
        }
    }

    private fun buySrcMoreInfo() {
        val bottomSheetFragment: BottomSheetDialogFragment = BuySrcInfo()
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }





    override fun onDestroyView() {
        super.onDestroyView()

        _binding=null
    }


}