package com.wedo8020.app.mainui.home.exchangescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wedo8020.app.R
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.databinding.FragmentPlantTreesInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.mainui.activities.MainActivity


class PlantTreesInfo : BottomSheetDialogFragment() {


    private var _binding: FragmentPlantTreesInfoBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding =FragmentPlantTreesInfoBinding.inflate(inflater, container, false)



        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity


        binding.plantTreesInfoDetails.text = activity.impactData?.arBonus?.tree?.info
        binding.crossButton.setOnClickListener {
            findNavController().navigate(R.id.plantTreesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }



}