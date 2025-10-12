package com.wedo8020.app.mainui.home.chatAndNotification.requestSentReceive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentRequestSentAndReceiveBinding
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.utils.Utils

/**
 * A simple [Fragment] subclass.
 * Use the [RequestSentAndReceiveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RequestSentAndReceiveFragment : BaseFragment() {
    private lateinit var mBinding:FragmentRequestSentAndReceiveBinding
    private val fragments = ArrayList<Fragment>()
    private var adapter: ViewPagerAdapter? =null
    private val titles = listOf(
        "sent","Received"
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding=DataBindingUtil.inflate(layoutInflater, R.layout.fragment_request_sent_and_receive, container, false)
  setUpUi()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            val navController = findNavController()
            val previousDestinationId = navController.previousBackStackEntry?.destination?.id
            if (previousDestinationId == R.id.messageAndNotificationsFragment) {
                findNavController().navigateUp()
            } else {
                findNavController().navigate(R.id.messageAndNotificationsFragment)
            }
        }

        mBinding.ivBack.setOnClickListener {
            val navController = findNavController()
            val previousDestinationId = navController.previousBackStackEntry?.destination?.id
            if (previousDestinationId == R.id.messageAndNotificationsFragment) {
                findNavController().navigateUp()
            } else {
                findNavController().navigate(R.id.messageAndNotificationsFragment)
            }
        }
        return mBinding.root
    }



    private fun setUpUi(){

        mBinding.apply {
           checkIfFragmentAttached {
               fragments.add(SentRequestFragment())
               fragments.add(ReceiveRequestFragment())
               // Replace with your actual tab titles
               adapter = ViewPagerAdapter(childFragmentManager, 2, fragments, titles)
               viewPager.adapter = adapter
               viewPager.offscreenPageLimit = 2
               if (arguments?.getString("requestReceive") == "requestReceive") {
                   viewPager.currentItem = 1
                   mBinding.tvReceiveTab.setTextColor(
                       ContextCompat.getColor(
                           requireContext(),
                           R.color.LightBlack
                       )
                   )
                   mBinding.ivReceivedBar.visibility = View.VISIBLE
                   mBinding.ivGivenBar.visibility = View.GONE
               }
               tabLayout.setupWithViewPager(viewPager)
               tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                   override fun onTabSelected(tab: TabLayout.Tab) {
                       when (tab.position) {
                           0 -> {
                               mBinding.ivReceivedBar.visibility = View.GONE
                               mBinding.ivGivenBar.visibility = View.VISIBLE
                               mBinding.tvGivenTab.setTextColor(
                                   ContextCompat.getColor(
                                       requireContext(),
                                       R.color.LightBlack
                                   )
                               )
                           }

                           1 -> {
                               mBinding.tvReceiveTab.setTextColor(
                                   ContextCompat.getColor(
                                       requireContext(),
                                       R.color.LightBlack
                                   )
                               )
                               mBinding.ivReceivedBar.visibility = View.VISIBLE
                               mBinding.ivGivenBar.visibility = View.GONE
                           }
                       }
                   }

                   override fun onTabUnselected(tab: TabLayout.Tab) {
                       if (context != null) {
                           mBinding.tvGivenTab.setTextColor(
                               ContextCompat.getColor(
                                   requireContext(),
                                   R.color.Grey
                               )
                           )
                           mBinding.tvReceiveTab.setTextColor(
                               ContextCompat.getColor(
                                   requireContext(),
                                   R.color.Grey
                               )
                           )
                       }
                   }

                   override fun onTabReselected(tab: TabLayout.Tab) {
                       when (tab.position) {
                           0 -> {
                               mBinding.ivReceivedBar.visibility = View.GONE
                               mBinding.ivGivenBar.visibility = View.VISIBLE
                               mBinding.tvGivenTab.setTextColor(
                                   ContextCompat.getColor(
                                       requireContext(),
                                       R.color.LightBlack
                                   )
                               )
                           }

                           1 -> {
                               mBinding.tvReceiveTab.setTextColor(
                                   ContextCompat.getColor(
                                       requireContext(),
                                       R.color.LightBlack
                                   )
                               )
                               mBinding.ivReceivedBar.visibility = View.VISIBLE
                               mBinding.ivGivenBar.visibility = View.GONE
                           }
                       }
                   }
               })
               if (arguments != null && requireArguments().containsKey("hashtag")) {
                   Utils.runWithDelay(1000) {
                       viewPager.setCurrentItem(3, false)
                       //   searchEt.setText(requireArguments().getString("hashtag"))
                   }
               } else if (arguments != null && requireArguments().containsKey("type")) {
                   Utils.runWithDelay(1000) {
                       viewPager.setCurrentItem(2, false)
                       //   searchEt.setText(requireArguments().getString("hashtag"))
                   }
               }
           }






        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //  mBinding = null
        adapter = null

    }
}