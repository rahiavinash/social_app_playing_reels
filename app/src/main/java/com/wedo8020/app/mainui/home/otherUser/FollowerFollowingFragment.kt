package com.wedo8020.app.mainui.home.otherUser

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.wedo8020.app.R
import com.wedo8020.app.databinding.CustomFollowingFollowerTabBinding
import com.wedo8020.app.databinding.FragmentFollowerFollowingBinding
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.home.profiletab.friendFollowerFollowing.FollowerListFragment
import com.wedo8020.app.mainui.home.profiletab.friendFollowerFollowing.FollowingListFragment


class FollowerFollowingFragment : Fragment() {
    private var binding: FragmentFollowerFollowingBinding? = null
    private val fragments = ArrayList<Fragment>()
    private lateinit var adapter: ViewPagerAdapter
    private val tabIcons = intArrayOf(
        R.drawable.ic_vedio_svg,
        R.drawable.ic_archive,
    )
    private var userID: String? = null

    private var type: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFollowerFollowingBinding.inflate(inflater,container,false)
        setUpUi()
        return binding?.root
    }

    private fun setUpUi(){
        setViewPager()
    }
    private fun setViewPager() {
        type= requireArguments().getString("type").toString()
        userID= requireArguments().getString("userId")
        binding?.apply {
            fragments.add(FollowerListFragment.newInstance(userID))
            fragments.add(FollowingListFragment.newInstance(userID))
            val titles = listOf("Followers", "Following")
            adapter = ViewPagerAdapter(childFragmentManager, 2, fragments, titles)
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 2
            tabLayout.setupWithViewPager(viewPager)
            for (i in 0 until tabLayout.tabCount) {
                val tab = tabLayout.getTabAt(i)
                if (tab != null) {
                    val customView: View =
                        LayoutInflater.from(requireContext()).inflate(R.layout.custom_following_follower_tab, null)
                    val customBinding = CustomFollowingFollowerTabBinding.bind(customView)
                    val tvDot = customBinding.tvDot

                    val tabText = customBinding.tvTabLabel
                    tabText.text = titles[i] // Set default text

                    if (i==0){
                        tabText.setTextColor(requireContext().getColor(R.color.pureBlack))
                    }
                    tvDot.visibility = if (i<fragments.size-1){
                        View.VISIBLE
                    }else View.INVISIBLE
                    tab.customView = customView
                }
            }

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val customView = tab.customView
                    if (customView != null) {
                        val customBinding = CustomFollowingFollowerTabBinding.bind(customView)
                        customBinding.tvTabLabel.setTextColor(requireContext().getColor(R.color.pureBlack))
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // Reset the view when a tab is unselected
                    val customView = tab.customView
                    if (customView != null) {
                        if (context!=null){
                            val customBinding = CustomFollowingFollowerTabBinding.bind(customView)
                            customBinding.tvTabLabel.setTextColor(requireContext().getColor(R.color.PureGrey))
                        }
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // Handle tab reselection if needed
                }
            })
            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
            if (type=="follower"){
                viewPager.currentItem=0
            }else{
                viewPager.currentItem=1
            }
        }
    }
}