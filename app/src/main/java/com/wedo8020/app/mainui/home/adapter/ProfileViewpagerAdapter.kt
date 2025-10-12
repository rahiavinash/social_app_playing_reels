package com.wedo8020.app.mainui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.wedo8020.app.mainui.home.profiletab.ActivePostFragment
import com.wedo8020.app.mainui.home.profiletab.ArchiveAirOffFragment
import com.wedo8020.app.mainui.home.profiletab.FavoriteDropFragment

@Suppress("DEPRECATION")
class ProfileViewpagerAdapter(fm:FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            0 -> {
                return ActivePostFragment()
            }
            1 -> {

                return FavoriteDropFragment()
            }
            2 -> {
                return ArchiveAirOffFragment()
            }
            /*3 -> {
                return ArchiveAirOffFragment()
            }*/
            else -> {
                return ActivePostFragment()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position) {
            0 -> {
                return ""
            }
            1 -> {
                return ""
            }
            2 -> {
                return ""
            }
           /* 3 -> {
                return ""
            }*/
        }
        return super.getPageTitle(position)
    }


}



