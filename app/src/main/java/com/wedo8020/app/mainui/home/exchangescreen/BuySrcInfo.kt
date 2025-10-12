package com.wedo8020.app.mainui.home.exchangescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.wedo8020.app.R
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.databinding.FragmentBuySrcInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.mainui.activities.MainActivity


class BuySrcInfo : BottomSheetDialogFragment() {

    private var _binding: FragmentBuySrcInfoBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding =FragmentBuySrcInfoBinding.inflate(inflater, container, false)

        val activity = requireActivity() as MainActivity


        binding.buySrcInfoDetails.text = activity.impactData?.arBonus?.wdInfo
        binding.crossButton.setOnClickListener {
            findNavController().navigate(R.id.buySrcFragment)
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}