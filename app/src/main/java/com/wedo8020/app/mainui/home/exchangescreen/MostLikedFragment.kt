package com.wedo8020.app.mainui.home.exchangescreen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentMostLikedBinding
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.utils.Utils


class MostLikedFragment : BaseFragment() {

    private val searchDelay = 500L // milliseconds
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mBinding: FragmentMostLikedBinding
    private lateinit var viewModel : PurchasesViewModel
    private val fragments = ArrayList<Fragment>()
    private var adapter: ViewPagerAdapter? =null
    private var type=""
    private var userID: String? = null
    private var currentFragment: SearchListener? = null
    var handlers = Handler(Looper.getMainLooper())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding=
            DataBindingUtil.inflate(layoutInflater,R.layout.fragment_most_liked, container, false)
        hideNavBottom()
        currentFragment = adapter?.instantiateItem(mBinding.viewPager, mBinding.viewPager.currentItem) as? SearchListener

        return  mBinding.root
    }


    @SuppressLint("ClickableViewAccessibility", "Recycle")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PurchasesViewModel::class.java]



        hideKeyboardOnOutSideClick(mBinding.root)
        setUpUi()
        viewModel.leaderBoardResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response.success == true) {
                Log.d("leaderBoardSuccess", "${response.data?.get("Jan 2024")}")
            } else {
                Log.d("leaderBoardSuccess", " No Success ${response.message}")
            }
        })


        mBinding.root.setOnTouchListener { _, event ->
            hideSoftKeyboard()
            false
        }
        mBinding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                mBinding.searchEt.setText("")
                mBinding.searchEt.isEnabled = false
                currentFragment = adapter?.instantiateItem(mBinding.viewPager, mBinding.viewPager.currentItem) as? SearchListener
                mBinding.searchEt.isEnabled = true
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        mBinding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {

                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    val query = p0.toString().trim()
                    if (query.isNotEmpty()) {
                        Log.d("textCleared", "Text is not cleared")
                        currentFragment?.onSearch(query)
                    }
                    else {
                        Log.d("textCleared", "Text is cleared")
                        currentFragment?.onSearch(query)
                    }
                }, searchDelay)
            }
        })
    }






    private fun hideSoftKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mBinding.searchEt.windowToken, 0)
    }


    private fun setUpUi(){

        mBinding.apply {
            checkIfFragmentAttached {


                fragments.add(MostLikedAccountFragment.newInstance(userID))
                fragments.add(MostLikedDropsFragment.newInstance(userID))
                val titles = listOf(
                    "Accounts", "Drops"
                )
                adapter = ViewPagerAdapter(childFragmentManager, 2, fragments, titles)
                viewPager.adapter = adapter
                viewPager.offscreenPageLimit = 2
                tabLayout.setupWithViewPager(viewPager)
                currentFragment = adapter?.instantiateItem(
                    mBinding.viewPager,
                    mBinding.viewPager.currentItem
                ) as? SearchListener

                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        when (tab.position) {
                            0 -> {
                                mBinding.ivReceivedBar.visibility = View.GONE
                                mBinding.ivGivenBar.visibility = View.VISIBLE
                                mBinding.tvGivenTab.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            }

                            1 -> {
                                mBinding.tvReceiveTab.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                mBinding.ivReceivedBar.visibility = View.VISIBLE
                                mBinding.ivGivenBar.visibility = View.GONE
                            }
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {
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

                    override fun onTabReselected(tab: TabLayout.Tab) {
                        when (tab.position) {
                            0 -> {
                                mBinding.ivReceivedBar.visibility = View.GONE
                                mBinding.ivGivenBar.visibility = View.VISIBLE
                                mBinding.tvGivenTab.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                            }

                            1 -> {
                                mBinding.tvReceiveTab.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                mBinding.ivReceivedBar.visibility = View.VISIBLE
                                mBinding.ivGivenBar.visibility = View.GONE
                            }
                        }
                    }
                })

                backButton.setOnClickListener {
                    findNavController().navigateUp()
                }

                if (arguments != null && requireArguments().containsKey("hashtag")) {
                    Utils.runWithDelay(1000) {
                        viewPager.setCurrentItem(3, false)
                        //   searchEt.setText(requireArguments().getString("hashtag"))
                    }
                }
            }
        }

    }
    interface SearchListener {
        fun onSearch(query: String)




    }


}