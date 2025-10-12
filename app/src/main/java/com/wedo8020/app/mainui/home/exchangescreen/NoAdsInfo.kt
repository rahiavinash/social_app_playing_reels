package com.wedo8020.app.mainui.home.exchangescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.databinding.FragmentNoAdsInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.R
import com.wedo8020.app.mainui.activities.MainActivity


class NoAdsInfo : BottomSheetDialogFragment() {
    private var _binding: FragmentNoAdsInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding =FragmentNoAdsInfoBinding.inflate(inflater, container, false)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.crossButton.setOnClickListener {
            findNavController().navigate(R.id.premiumPurchaseFragment)
        }
        val activity = requireActivity() as MainActivity
        binding.noAdsInfoDetails.text = activity.impactData?.arBonus?.premiumPurchase?.noAds?.info
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}