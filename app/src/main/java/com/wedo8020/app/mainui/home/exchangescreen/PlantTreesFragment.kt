package com.wedo8020.app.mainui.home.exchangescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentPlantTreesBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.utils.Utils.disableMultiTap

private const val PRODUCT_ID = "com.wedo.plant1"
class PlantTreesFragment : BaseFragment() {

    private lateinit var _binding: FragmentPlantTreesBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        _binding = FragmentPlantTreesBinding.inflate(inflater, container, false)

        hideNavBottom()


        binding.moreInfoButton.setOnClickListener {
            plantTreesMoreInfo()
        }
        val activity = requireActivity() as MainActivity





        /** Update Purchase Product */

        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        val activity = requireActivity() as? MainActivity

        binding.nineDollar.text = activity?.inAppProductsPrice?.get("com.wedo.plant1") ?: "N/A"
        binding.oneTwoDollar.text = activity?.inAppProductsPrice?.get("com.wedo.plant5") ?: "N/A"
        binding.nineNineNineSevenNine.text = activity?.inAppProductsPrice?.get("com.wedo.plant10") ?: "N/A"

        binding.thousandBonusText.text = activity?.impactData?.arBonus?.tree?.one?.label ?: "N/A"
        binding.tenThousandBonusText.text = activity?.impactData?.arBonus?.tree?.five?.label ?: "N/A"
        binding.fiftyThousandBonusText.text = activity?.impactData?.arBonus?.tree?.ten?.label ?: "N/A"


        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.xchangeFragment)
        }
        binding.constraintLayout.setOnClickListener {
            binding.constraintLayout.disableMultiTap()
            binding.constraintLayoutOne.disableMultiTap()
            binding.constraintLayoutTwo.disableMultiTap()
            bundle.putString("tree", "1 Tree")
            bundle.putString("amount", activity?.inAppProductsPrice?.get("com.wedo.plant1") ?: "N/A")
            findNavController().navigate(R.id.specificCountryTreePlanting, bundle)
        }

        binding.constraintLayoutOne.setOnClickListener {
            binding.constraintLayout.disableMultiTap()
            binding.constraintLayoutOne.disableMultiTap()
            binding.constraintLayoutTwo.disableMultiTap()
            bundle.putString("tree", "5 Trees")
            bundle.putString("amount", activity?.inAppProductsPrice?.get("com.wedo.plant5") ?: "N/A")
            findNavController().navigate(R.id.specificCountryTreePlanting, bundle)
        }
        binding.constraintLayoutTwo.setOnClickListener {
            binding.constraintLayout.disableMultiTap()
            binding.constraintLayoutOne.disableMultiTap()
            binding.constraintLayoutTwo.disableMultiTap()
            bundle.putString("tree", "10 Trees")
            bundle.putString("amount", activity?.inAppProductsPrice?.get("com.wedo.plant10") ?: "N/A")
            findNavController().navigate(R.id.specificCountryTreePlanting, bundle)
        }

    }








    private fun plantTreesMoreInfo() {
        val bottomSheetFragment: BottomSheetDialogFragment = PlantTreesInfo()
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    companion object {
        val bundle = Bundle()
    }

}



