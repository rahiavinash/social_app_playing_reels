package com.wedo8020.app.mainui.home.exchangescreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentMostTreesPlantedBinding
import com.wedo8020.app.mainui.adapter.ViewPagerAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.utils.Utils


class MostTreesPlantedFragment : BaseFragment() {

    private lateinit var mBinding: FragmentMostTreesPlantedBinding
    private val searchDelay = 500L // milliseconds
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var viewModel : PurchasesViewModel
    private val fragments = ArrayList<Fragment>()
    private var adapter: ViewPagerAdapter? =null
    private var type=""
    private var userID: String? = null
    private var currentFragment: MostLikedSearchListener? = null
    var handlers = Handler(Looper.getMainLooper())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding=
            DataBindingUtil.inflate(layoutInflater,R.layout.fragment_most_trees_planted, container, false)
        hideNavBottom()

        return  mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()

        hideKeyboardOnOutSideClick(mBinding.root)
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
                currentFragment = adapter?.instantiateItem(mBinding.viewPager, mBinding.viewPager.currentItem) as? MostLikedSearchListener
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
                        currentFragment?.onSearch(query)
                    }
                    else {
                        currentFragment?.onSearch(query)
                    }
                }, searchDelay)
            }
        })



    }

    private fun setUpUi(){
        mBinding.apply {
            checkIfFragmentAttached {


                fragments.add(MostTreesPlantedIndividual.newInstance(userID))
                fragments.add(MostTreesPlantedBusiness.newInstance(userID))
                fragments.add(MostTreesPlantedRegion.newInstance(userID))
                val titles = listOf(
                    "Individual", "Business", "Region"
                )
                adapter = ViewPagerAdapter(childFragmentManager, 3, fragments, titles)
                viewPager.adapter = adapter
                viewPager.offscreenPageLimit = 2
                if (viewPager.currentItem ==0){
                    mBinding.ivReceivedBar.visibility = View.GONE
                    mBinding.ivRegionBar.visibility = View.GONE
                    mBinding.ivGivenBar.visibility = View.VISIBLE
                    mBinding.tvGivenTab.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                }
                tabLayout.setupWithViewPager(viewPager)
                currentFragment = adapter?.instantiateItem(
                    mBinding.viewPager,
                    mBinding.viewPager.currentItem
                ) as? MostLikedSearchListener


                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        when (tab.position) {
                            0 -> {
                                mBinding.ivReceivedBar.visibility = View.GONE
                                mBinding.ivRegionBar.visibility = View.GONE
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
                                mBinding.ivRegionBar.visibility = View.GONE
                                mBinding.ivReceivedBar.visibility = View.VISIBLE
                                mBinding.ivGivenBar.visibility = View.GONE
                            }
                            2 -> {
                                mBinding.ivReceivedBar.visibility = View.GONE
                                mBinding.ivRegionBar.visibility = View.VISIBLE
                                mBinding.ivGivenBar.visibility = View.GONE
                                mBinding.tvRegionTab.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
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
                        mBinding.tvRegionTab.setTextColor(
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
                                mBinding.ivRegionBar.visibility = View.GONE
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
                                mBinding.ivRegionBar.visibility = View.GONE
                                mBinding.ivReceivedBar.visibility = View.VISIBLE
                                mBinding.ivGivenBar.visibility = View.GONE
                            }
                            2 -> {
                                mBinding.tvRegionTab.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.white
                                    )
                                )
                                mBinding.ivReceivedBar.visibility = View.GONE
                                mBinding.ivGivenBar.visibility = View.GONE
                                mBinding.ivRegionBar.visibility = View.VISIBLE
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
    interface MostLikedSearchListener {
        fun onSearch(query: String)


    }


}