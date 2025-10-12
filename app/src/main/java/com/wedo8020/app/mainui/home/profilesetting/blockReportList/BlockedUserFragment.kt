package com.wedo8020.app.mainui.home.profilesetting.blockReportList


import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentBlockedUserBinding
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.google.android.material.tabs.TabLayout

class BlockedUserFragment : BaseFragment() {

    private lateinit var mBinding: FragmentBlockedUserBinding
    private lateinit var adapter: ViewPagerAdapter
    private val fullText = "Restricted  •  Blocked  •  Reported"
    private val fragments = ArrayList<Fragment>()
    val titles = listOf(
        "Restricted",
        "Blocked",
        "Reported"
    )
    var type = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_blocked_user, container, false)

        arguments?.let {
            type  = it.getString("clBlockedList").toString()
        }

        /** Change the center text in red color */
        if (type == "clBlockedList") {
            val spannable = SpannableString(fullText)
            val wordToColors = "Blocked"
            val startIndexRestricted = fullText.indexOf(wordToColors)
            val endIndexRestricted = startIndexRestricted + wordToColors.length
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                startIndexRestricted,
                endIndexRestricted,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            mBinding.tvTabText.text = spannable
        } else {
            //    val fullText = getString(R.string.restricted_blocked_reported)
            val spannable = SpannableString(fullText)
            val wordToColors = "Restricted"
            val startIndexRestricted = fullText.indexOf(wordToColors)
            val endIndexRestricted = startIndexRestricted + wordToColors.length
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                startIndexRestricted,
                endIndexRestricted,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            mBinding.tvTabText.text = spannable
        }




        mBinding.IvSetting.setOnClickListener {
            findNavController().navigateUp()
        }
        setViewPager(true)
        return mBinding.root
    }

    private fun setViewPager(canSeeArchive: Boolean) {
        mBinding?.apply {
            fragments.add(RestrictFragment())
            fragments.add(BlockListFragment())
            fragments.add(ReportListFragment())
            // Replace with your actual tab titles
            adapter = ViewPagerAdapter(childFragmentManager, 3, fragments, titles)
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 3
            if (type == "clBlockedList") {
                viewPager.currentItem = 1
            }
            tabLayout.setupWithViewPager(viewPager)

            /** SetIcon For TabLayout and TabColor*/
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val spannable = SpannableString(fullText)
                    val selectedIndex = tab.position
                    titles.forEachIndexed { index, wordToColor ->
                        val startIndex = fullText.indexOf(wordToColor)
                        val endIndex = startIndex + wordToColor.length
                        val color = if (index == 1 && index == selectedIndex) {
                            Color.RED
                        } // Always red for "Blocked"
                        else if (index == 2 && tab.position == 2) {
                            Color.BLACK
                        } // Selected tab color
                        else if (index == 0 && tab.position == 0) {
                            Color.BLACK
                        } // Selected tab color
                        else {
                            Color.GRAY // Other tabs are gray when unselected
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
                        val color = if (index != 1) Color.GRAY else Color.RED
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
                            1 -> Color.RED  // Always red for "Blocked"
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