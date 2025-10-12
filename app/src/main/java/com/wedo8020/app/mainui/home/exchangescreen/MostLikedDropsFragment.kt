package com.wedo8020.app.mainui.home.exchangescreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentMostLikedDropsBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.exchangescreen.adapter.DropsAdapter
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.DropsDataRequest
import com.wedo8020.app.response.MostTreesPlantedRequest
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val ARG_PARAM1 = "userID"
class MostLikedDropsFragment : BaseFragment(), MostLikedFragment.SearchListener {

    private lateinit var mBinding: FragmentMostLikedDropsBinding
    private lateinit var viewModel : PurchasesViewModel
    lateinit var adapter: DropsAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    private var currentMonthOffset = 0
    private var currentMonthField : String ?=null
    private var lastTwelveMonths : Int = 12
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding=
            DataBindingUtil.inflate(layoutInflater,R.layout.fragment_most_liked_drops, container, false)
        hideNavBottom()

        return  mBinding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PurchasesViewModel::class.java]
        viewModel.getDropsDetails(DropsDataRequest(search = "", filterDate = currentMonthField))

        setRecyclerAdapter()
        viewModel.prizeDetails()
        viewModel.dropsResponse.observe(viewLifecycleOwner, Observer {
                response ->
            if(response.success == true){
                Log.d("mostPlantedTreesResponse", "${response.data?.get("Jan 2024")}")
                mBinding.animationView.visibility = View.GONE
                response.data?.keys?.firstOrNull()?.let { key ->
                    Log.d("mostPlantedTreesResponse", "key is $key")
                    mBinding.monthText.text = key
                    var currentMonth = isCurrentMonthYear(key)
                    val data = response.data[key]


                    if (data.isNullOrEmpty()) {
                        mBinding.recyclerView.visibility = View.GONE
                        mBinding.recyclerViewNoData.visibility = View.VISIBLE
                        //mBinding.monthText.visibility = View.GONE
                        mBinding.recyclerViewNoData.text = "No results found"
                    } else {
                        mBinding.recyclerView.visibility = View.VISIBLE
                        mBinding.recyclerViewNoData.visibility = View.GONE
                        //mBinding.monthText.visibility = View.VISIBLE
                        adapter.setNewItems(data)
                    }
                    if(currentMonth){
                        mBinding.nextButtonSlider.visibility = View.INVISIBLE

                    }
                    else {
                        mBinding.nextButtonSlider.visibility = View.VISIBLE
                    }
                    if(lastTwelveMonths <=0){
                        mBinding.previousButtonSlider.visibility = View.INVISIBLE
                    }
                    else {
                        mBinding.previousButtonSlider.visibility = View.VISIBLE
                    }

                }

            }
            else {
                mBinding.animationView.visibility = View.GONE
            }
        })
        mBinding.refresh.setOnRefreshListener {

            mBinding.refresh.isRefreshing= false
            mBinding.recyclerView.visibility = View.GONE
            mBinding.animationView.visibility = View.GONE

            viewModel.getDropsDetails(DropsDataRequest(search = "", filterDate = currentMonthField))
        }
        viewModel.prizeDetailsResponse.observe(viewLifecycleOwner, Observer {
                response ->
            if(response.success){
                mBinding.winnerText.text = "${response.data?.drop?.leftText}: ${response.data?.drop?.rightText}"
            }
        })
        mBinding.previousButtonSlider.setOnClickListener {
            if(isInternetAvailable(requireContext())){
                mBinding.previousButtonSlider.disableMultiTap()
                lastTwelveMonths -=1
                currentMonthOffset--
                currentMonthField = getStartOfMonth(currentMonthOffset)
                viewModel.getDropsDetails(DropsDataRequest(search = "", filterDate = currentMonthField))
            }
            else {
                Utils.showToast("Please check your internet connection")
            }

        }
        mBinding.nextButtonSlider.setOnClickListener {
            if(isInternetAvailable(requireContext())){
                mBinding.nextButtonSlider.disableMultiTap()
                lastTwelveMonths +=1
                currentMonthOffset++
                currentMonthField = getStartOfMonth(currentMonthOffset)
                viewModel.getDropsDetails(DropsDataRequest(search = "", filterDate = currentMonthField))
            }
            else {
                Utils.showToast("Please check your internet connection")
            }

        }

    }

    private fun setRecyclerAdapter() {
        mBinding.apply {
            manager = LinearLayoutManager(requireContext())
            this.recyclerView.layoutManager = manager
            adapter = DropsAdapter(R.layout.drops_recycler, object  : DropsAdapter.DropsClick{
                override fun onClick(dropData: Data) {
                    val bundle = Bundle()
                    bundle.putInt("position",0)
                    bundle.putString("dataList",dropData._id)
                    findNavController().navigate(
                        R.id.action_mostLikedFragment_to_show_profile_post_fragment,
                        bundle
                    )
                }
            })
            mBinding.recyclerView.adapter = adapter

        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?) =
            MostLikedDropsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    override fun onSearch(query: String) {
        mBinding.recyclerViewNoData.visibility = View.GONE
        viewModel.getDropsDetails(DropsDataRequest(search = query, filterDate = currentMonthField))
    }
    private fun getStartOfMonth(monthOffset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthOffset)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return dateFormat.format(calendar.time)
    }
    private fun isCurrentMonthYear(key: String): Boolean {
        val calendar = Calendar.getInstance()
        val currentMonthYear = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)
        return key == currentMonthYear
    }




}