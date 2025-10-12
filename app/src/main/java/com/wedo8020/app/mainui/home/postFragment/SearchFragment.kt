package com.wedo8020.app.mainui.home.postFragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager

import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentSearchBinding
import com.wedo8020.app.mainui.adapter.PostTypePagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.utils.Utils.autoCleared
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.wedo8020.app.eventBus.SearchHashtag
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SearchFragment : BaseFragment() {
    private var binding by autoCleared<FragmentSearchBinding>()
    private var adapter: PostTypePagerAdapter? = null
    private var isSearchAllow = true
    private val tabIcons = intArrayOf(
        R.drawable.ic_drop,
        R.drawable.ic_account,

        R.drawable.ic_hashtag
    )
    private val tabTitle = arrayOf("Drops","Accounts","Hashtags")

    private val textWatcherHandler = Handler(Looper.myLooper()!!)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater,container,false)
        hideNavBottom()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
    }


    private fun setUpUi(){
        val currentContext = requireContext()

        binding?.apply {
//            hideKeybordOnOutSideClick(mainLayout)
            if (adapter==null){
                adapter = PostTypePagerAdapter(childFragmentManager, 3)
            }else{
                adapter?.notifyDataSetChanged()
            }


            mainLayout.setOnClickListener {
                hideKeyboard()
            }
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 3
            tabLayout.setupWithViewPager(viewPager)
            for (i in 0 until tabLayout.tabCount) {
                val tab = tabLayout.getTabAt(i)
                if (tab != null) {
                    val customView: View =
                        LayoutInflater.from(currentContext).inflate(R.layout.custom_tab_item, null)
                    val tabIcon = customView.findViewById<ImageView>(R.id.tab_icon)
                    tabIcon.setImageResource(tabIcons[i]) // Set default icon
                    val tabText = customView.findViewById<TextView>(R.id.tab_text)
                    tabText.text = tabTitle[i] // Set default text

                    tabText.visibility = if (i==0){
                        View.VISIBLE
                    }else View.INVISIBLE
                    tab.customView = customView
                }
            }
            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    hideKeyboard()
                    isSearchAllow = false
//                    searchEt.setText("")
                    isSearchAllow = true
                    val customView = tab.customView

                    if (customView != null) {

                        val tabIcon = customView.findViewById<ImageView>(R.id.tab_icon)
                        tabIcon.imageTintList = ColorStateList.valueOf(currentContext.getColor(R.color.white))
                        val tabText = customView.findViewById<TextView>(R.id.tab_text)
                        tabText.visibility = View.VISIBLE
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // Reset the view when a tab is unselected
                    val customView = tab.customView
                    if (customView != null) {
                        if (context!=null){
                            val tabIcon = customView.findViewById<ImageView>(R.id.tab_icon)
                            tabIcon.imageTintList = ColorStateList.valueOf(currentContext.getColor(R.color.unselectedColor))
                            val tabText = customView.findViewById<TextView>(R.id.tab_text)
                            tabText.visibility = View.INVISIBLE
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
            viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    hideKeyboard()
                }

                override fun onPageSelected(position: Int) {
                    val frag = adapter!!.list[binding?.viewPager?.currentItem?:0] as  SearchTabFragment
                    frag.hitSearchApi()

                    if (frag.searchText!=searchEt.text.toString()){
                        Log.e("sagsdf","onPageSelected")

                        frag.searchByText(searchEt.text.toString())
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                }
            })
            searchEditText()

        }
    }
    private fun searchEditText(){
        binding?.searchEt?.apply {
            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (isSearchAllow){
                        textWatcherHandler.removeCallbacksAndMessages(null)
                        if (p0 == " ") {
                            setText(text.toString().replace(" ", ""))
                            setSelection(text.toString().length)
                        }
                        textWatcherHandler.postDelayed({
//                            try {
                            if (adapter!!.list.size>0){
                                val frag = adapter!!.list[binding?.viewPager?.currentItem?:0] as  SearchTabFragment
                                frag.searchByText(text.toString().trim())
                            }
//                            }catch (e: Exception){}
                        }, 1000)
                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }
    override fun onResume() {
        super.onResume()

    }

    override fun onDestroyView() {
        super.onDestroyView()

//        binding = null
//        adapter = null
//
    }

    @Subscribe
    fun searchHashtag(event: Any){
        if (event is SearchHashtag){
            binding?.apply {
                viewPager.currentItem = 2
                searchEt.setText(event.hashtag)
            }
        }
    }
    fun hideKeyboard() {
        try {
            if (context != null) {
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding?.searchEt?.windowToken, 0)
            }
        } catch (_: Exception) {
        }
    }

}