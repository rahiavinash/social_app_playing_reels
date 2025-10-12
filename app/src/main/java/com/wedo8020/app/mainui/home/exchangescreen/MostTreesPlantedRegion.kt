package com.wedo8020.app.mainui.home.exchangescreen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentMostTreesPlantedIndividualBinding
import com.wedo8020.app.databinding.FragmentMostTreesPlantedRegionBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.exchangescreen.adapter.MostPlantedTreesAdapter
import com.wedo8020.app.mainui.home.exchangescreen.adapter.MostPlantedTreesByRegionAdapter
import com.wedo8020.app.request.RegionRequest
import com.wedo8020.app.response.MostTreesPlantedRequest
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val ARG_PARAM1 = "userID"
class MostTreesPlantedRegion : BaseFragment(), MostTreesPlantedFragment.MostLikedSearchListener{

    private lateinit var binding: FragmentMostTreesPlantedRegionBinding
    private lateinit var viewModel : PurchasesViewModel
    lateinit var adapter: MostPlantedTreesByRegionAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    private var currentMonthOffset = 0
    private var currentMonthField : String ?=null
    private var lastTwelveMonths : Int = 12
    private var monthInfo : String?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(layoutInflater,R.layout.fragment_most_trees_planted_region, container, false)
        hideNavBottom()



        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PurchasesViewModel::class.java]
        currentMonthField = getStartOfMonth(currentMonthOffset)
        viewModel.getMostPlantedTreesByRegion(RegionRequest(search = "", filterDate = currentMonthField))
        setRecyclerAdapter()


        viewModel.mostPlantedTreesResponseByRegion.observe(viewLifecycleOwner) { response ->
            if (response.success == true) {
                // Log.d("mostPlantedTreesResponse", "${response.data?.get("Jan 2024")}")
                binding.animationView.visibility = View.GONE
                val currentMonth = monthInfo?.let { isCurrentMonthYear(it) }
                Log.d("RegionCountryResponse", "Region is ${response.data}")
                    if (response.data.isNullOrEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        binding.recyclerViewNoData.visibility = View.VISIBLE
                        binding.recyclerViewNoData.text = "No results found"
                    } else {
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.recyclerViewNoData.visibility = View.GONE
                        adapter.setNewItems(response.data)
                    }
                if(currentMonth == true){
                    binding.nextButtonSlider.visibility = View.INVISIBLE

                }
                else {
                    binding.nextButtonSlider.visibility = View.VISIBLE
                }
                if(lastTwelveMonths <=0){
                    binding.previousButtonSlider.visibility = View.INVISIBLE
                }
                else {
                    binding.previousButtonSlider.visibility = View.VISIBLE
                }

            }

             else {
                binding.animationView.visibility = View.GONE
            }


        }
        monthInfo = currentMonthField
        monthInfo = monthInfo?.let { getMonthYearFromDate(it) }
        binding.monthText.text = monthInfo

        binding.previousButtonSlider.setOnClickListener {
            binding.previousButtonSlider.disableMultiTap()
            if(isInternetAvailable(requireContext())){
                lastTwelveMonths -=1
                currentMonthOffset--
                currentMonthField = getStartOfMonth(currentMonthOffset)
                monthInfo = currentMonthField
                monthInfo = monthInfo?.let { getMonthYearFromDate(it) }
                binding.monthText.text = monthInfo
                viewModel.getMostPlantedTreesByRegion(RegionRequest(search = "", filterDate = currentMonthField))
            }
            else {
                Utils.showToast("Please check your internet connection")
            }


        }
        binding.nextButtonSlider.setOnClickListener {
            binding.nextButtonSlider.disableMultiTap()
            if(isInternetAvailable(requireContext())){
                lastTwelveMonths +=1
                currentMonthOffset++
                currentMonthField = getStartOfMonth(currentMonthOffset)
                monthInfo = currentMonthField
                monthInfo = monthInfo?.let { getMonthYearFromDate(it) }
                binding.monthText.text = monthInfo
                viewModel.getMostPlantedTreesByRegion(RegionRequest(search = "", filterDate = currentMonthField))
            }
            else {
                Utils.showToast("Please check your internet connection")
            }

        }
        binding.refresh.setOnRefreshListener {

            binding.refresh.isRefreshing= false
            binding.recyclerView.visibility = View.GONE
            binding.animationView.visibility = View.GONE
            viewModel.getMostPlantedTreesByRegion(RegionRequest(search = "", filterDate = currentMonthField))
        }
    }



    private fun setRecyclerAdapter() {
        binding.apply {
            manager = LinearLayoutManager(requireContext())
            this.recyclerView.layoutManager = manager
            adapter = MostPlantedTreesByRegionAdapter(R.layout.regions_layout)
            binding.recyclerView.adapter = adapter

        }
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String?) =
            MostTreesPlantedRegion().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
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


    private fun getMonthYearFromDate(dateTime: String): String {
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputDateFormat = SimpleDateFormat("MMM yyyy", Locale.US)

        val calendar = Calendar.getInstance()
        calendar.time = inputDateFormat.parse(dateTime)

        return outputDateFormat.format(calendar.time)
    }




    override fun onSearch(query: String) {
            binding.recyclerViewNoData.visibility = View.GONE
            viewModel.getMostPlantedTreesByRegion(
                RegionRequest(
                    search = query,
                    filterDate = currentMonthField
                )
            )
        }



}