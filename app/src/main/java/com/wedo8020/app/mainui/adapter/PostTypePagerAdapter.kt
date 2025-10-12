package com.wedo8020.app.mainui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.wedo8020.app.mainui.home.postFragment.SearchTabFragment
import com.wedo8020.app.utils.LogTypeEnum
import com.wedo8020.app.utils.Utils


class PostTypePagerAdapter  (fm: FragmentManager,
                             private var totalTabCount:Int,) : FragmentPagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var list = ArrayList<Fragment>()
   /* override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val frag = SearchTabFragment.newInstance(position)
        list.add(frag)
        Utils.printLog(LogTypeEnum.ERROR,"PostTypePagerAdapter",list.size.toString())
        return frag
    }*/
    override fun getItem(pos: Int): Fragment {

    val frag = SearchTabFragment.newInstance(pos)
    list.add(frag)
    Utils.printLog(LogTypeEnum.ERROR,"PostTypePagerAdapter",list.size.toString())
    return frag
        /*return when (pos) {
            0 ->{
                FriendsFragment.newInstance()
            }
            1 ->{
                DropsFragment.newInstance()
            }
            else -> {
                FollowingFragment.newInstance()
            }
        }*/
    }
    fun getTabPosition(position: Int): Int {
        return position
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Return null to display only custom view or use the title as needed
        return null
    }

    override fun getCount(): Int {
        return totalTabCount
    }

}