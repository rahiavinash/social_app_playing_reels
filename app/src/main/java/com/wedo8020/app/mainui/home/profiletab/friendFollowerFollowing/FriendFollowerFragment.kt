package com.wedo8020.app.mainui.home.profiletab.friendFollowerFollowing

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentFriendFollowerBinding
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment


/**
 * A simple [Fragment] subclass.
 * Use the [FriendFollowerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendFollowerFragment : BaseFragment() {
   private lateinit var mBinding:FragmentFriendFollowerBinding
    private lateinit var adapter: ViewPagerAdapter
    private val fullText ="Friends  •  Followers  •  Following"
    private val titles = listOf(
        "Friends ",
        "Followers",
        "Following"
    ) // Replace with your actual tab titles
    private val fragments = ArrayList<Fragment>()
    private var type=""
    private var wordToColors=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding =DataBindingUtil.inflate(layoutInflater,R.layout.fragment_friend_follower, container, false)

        mBinding.IvSetting.setOnClickListener {
            findNavController().navigateUp()
        }
        setViewPager(true)
        return mBinding.root
    }
    private fun setViewPager(canSeeArchive: Boolean) {
        type= requireArguments().getString("type").toString()

        mBinding?.apply {
            fragments.add(FriendListFragment())
            fragments.add(FollowerListFragment())
            fragments.add(FollowingListFragment())


            adapter = ViewPagerAdapter(childFragmentManager, 3, fragments, titles)
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 3

            if (type=="friend"){
                viewPager.currentItem=0
             wordToColors  = "Friends"
            }else if (type=="follower"){
                viewPager.currentItem=1
                wordToColors  = "Followers"
            }else{
                viewPager.currentItem=2
                wordToColors  = "Following"
            }

            /** Change the center text in red color */
            val spannable = SpannableString(fullText)

            val startIndexRestricted = fullText.indexOf(wordToColors)
            val endIndexRestricted = startIndexRestricted + wordToColors.length
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                startIndexRestricted,
                endIndexRestricted,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            mBinding.tvTabText.text = spannable
            tabLayout.setupWithViewPager(viewPager)

            /** SetIcon For TabLayout and TabColor*/
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val spannable = SpannableString(fullText)
                    titles.forEachIndexed { index, wordToColor ->
                        val startIndex = fullText.indexOf(wordToColor)
                        val endIndex = startIndex + wordToColor.length
                        val color = when (index) {
                            //      1 -> Color.RED  // Always red for "Blocked"
                            tab.position -> Color.BLACK // Selected tab color
                            else -> Color.GRAY // Other tabs are gray when unselected
                        }
                        /* val color = if (index == 1 && index == selectedIndex) Color.RED else Color.GRAY*/
                        spannable.setSpan(
                            ForegroundColorSpan(color),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    mBinding.tvTabText.text = spannable
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    val spannable = SpannableString(fullText)
                    titles.forEachIndexed { index, wordToColor ->
                        val startIndex = fullText.indexOf(wordToColor)
                        val endIndex = startIndex + wordToColor.length
                        val color = when (index) {
                       //     1 -> Color.RED  // Always red for "Blocked"
                            tab.position -> Color.BLACK // Selected tab color
                            else -> Color.GRAY // Other tabs are gray when unselected
                        }
                        spannable.setSpan(
                            ForegroundColorSpan(color),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    mBinding.tvTabText.text = spannable
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    val spannable = SpannableString(fullText)
                    titles.forEachIndexed { index, wordToColor ->
                        val startIndex = fullText.indexOf(wordToColor)
                        val endIndex = startIndex + wordToColor.length
                        val color = when (index) {
                      //      1 -> Color.RED  // Always red for "Blocked"
                            tab.position -> Color.BLACK // Selected tab color
                            else -> Color.GRAY // Other tabs are gray when unselected
                        }

                        spannable.setSpan(
                            ForegroundColorSpan(color),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    mBinding.tvTabText.text = spannable
                }
            })



        }
    }

}