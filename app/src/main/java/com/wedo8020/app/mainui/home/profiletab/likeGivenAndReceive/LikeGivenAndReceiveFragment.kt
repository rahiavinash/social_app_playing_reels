package com.wedo8020.app.mainui.home.profiletab.likeGivenAndReceive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentLikeGivenAndReceiveBinding
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment

import com.wedo8020.app.utils.Utils
import com.google.android.material.tabs.TabLayout

/**
 * A simple [Fragment] subclass.
 * Use the [LikeGivenAndReceiveFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LikeGivenAndReceiveFragment : BaseFragment() {
private lateinit var mBinding:FragmentLikeGivenAndReceiveBinding
    private val fragments = ArrayList<Fragment>()
    private var adapter: ViewPagerAdapter? =null
    private var type=""
    private var userID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding=DataBindingUtil.inflate(layoutInflater, R.layout.fragment_like_given_and_receive, container, false)
        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
    }

    private fun setUpUi(){
        type= requireArguments().getString("types").toString()
        userID= requireArguments().getString("userId")
        mBinding?.apply {
            mBinding?.apply {
                fragments.add(GivenLikeListFragment.newInstance(userID))
                fragments.add(ReceiveListFragment.newInstance(userID))
                val titles = listOf(
                    "Given","Received"
                ) // Replace with your actual tab titles
                adapter = ViewPagerAdapter(childFragmentManager, 2, fragments, titles)
                viewPager.adapter = adapter
                viewPager.offscreenPageLimit = 2
                if (type=="given"){
                    viewPager.currentItem=0
                    mBinding.ivReceivedBar.visibility=View.GONE
                    mBinding.ivGivenBar.visibility=View.VISIBLE
                    mBinding.tvGivenTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }else{
                    viewPager.currentItem=1
                    mBinding.tvReceiveTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    mBinding.ivReceivedBar.visibility=View.VISIBLE
                    mBinding.ivGivenBar.visibility=View.GONE
                }
                tabLayout.setupWithViewPager(viewPager)

                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    when (tab.position){
                        0 -> {
                            mBinding.ivReceivedBar.visibility=View.GONE
                            mBinding.ivGivenBar.visibility=View.VISIBLE
                            mBinding.tvGivenTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        }
                        1-> {
                            mBinding.tvReceiveTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            mBinding.ivReceivedBar.visibility=View.VISIBLE
                            mBinding.ivGivenBar.visibility=View.GONE
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    mBinding.tvGivenTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                    mBinding.tvReceiveTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    when (tab.position){
                        0 -> {
                            mBinding.ivReceivedBar.visibility=View.GONE
                            mBinding.ivGivenBar.visibility=View.VISIBLE
                            mBinding.tvGivenTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        }
                        1-> {
                            mBinding.tvReceiveTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                            mBinding.ivReceivedBar.visibility=View.VISIBLE
                            mBinding.ivGivenBar.visibility=View.GONE
                        }
                    }
                }
            })

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            if (arguments!=null&&requireArguments().containsKey("hashtag")){
                Utils.runWithDelay(1000){
                    viewPager.setCurrentItem(3,false)
                 //   searchEt.setText(requireArguments().getString("hashtag"))
                }
            }
        }
    }}


    override fun onDestroyView() {
        super.onDestroyView()

      //  mBinding = null
        adapter = null

    }

}