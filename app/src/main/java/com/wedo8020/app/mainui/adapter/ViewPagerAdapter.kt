package com.wedo8020.app.mainui.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

@SuppressLint("WrongConstant")
class ViewPagerAdapter (fm: FragmentManager,
                        private var totalTabCount: Int,
                        var list: ArrayList<Fragment>,
                        private var tabTitles: List<String> ) : FragmentPagerAdapter(fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {


    override fun getItem(pos: Int): Fragment {
        return list[pos]

    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles.getOrNull(position)
    }


    override fun getCount(): Int {
        return totalTabCount
    }

}