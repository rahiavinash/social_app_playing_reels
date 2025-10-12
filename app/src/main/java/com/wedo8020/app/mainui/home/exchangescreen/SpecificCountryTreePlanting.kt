package com.wedo8020.app.mainui.home.exchangescreen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentSpecificCountryTreePlantingBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.FIVE_PLANTS_ID
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.ONE_PLANT_ID
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.TEN_PLANTS_ID
import com.wedo8020.app.mainui.home.exchangescreen.adapter.CountryListAdapter
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.utils.Utils.disableMultiTap


class SpecificCountryTreePlanting : BaseFragment() {


    private var _binding: FragmentSpecificCountryTreePlantingBinding? = null
    private val binding get() = _binding!!
    var treesCount : String? = null
    var amount: String? = null
    lateinit var adapter: CountryListAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    private var countryList  : ArrayList<String> =  arrayListOf()
    val countryMap: HashMap<String, String> = HashMap()
    var countryIdOne : String?= null
    var countryName : String?=null
    private var isClicked : Boolean ? = null
    private val viewModel by viewModels<PurchasesViewModel>()
    private val repository = RemoteRepositaryImp()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpecificCountryTreePlantingBinding.inflate(inflater, container, false)
        hideNavBottom()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        val purchaseManager = mainActivity.getPurchaseManager()

        binding.countryListText.isFocusable = false
        binding.countryListText.isClickable = false
        binding.countryListText.isFocusableInTouchMode = false
        binding.countryListText.keyListener = null
        arguments?.let{
           treesCount =  it.getString("tree")
            amount= it.getString("amount")
        }
        binding.confirm.isEnabled = false

        binding.crossButton.setOnClickListener {
            findNavController().navigateUp()
        }
        repository.getCountryDetails { isSuccess, response ->
            if(isSuccess && response.success == true){
                Log.d("countryListDetails", response.data.toString())
                for(x in 0 until (response.data?.size ?: 0)){
                    countryList.add(response.data?.get(x)?.name.toString())
                    countryMap[response.data?.get(x)?.name.toString()] =response.data?.get(x)?._id.toString()
                }

            }
        }

        viewModel.getCountryDetails()
        setRecyclerAdapter()


        binding.dropDownClick.setOnClickListener {
            if (binding.countryList.visibility == View.VISIBLE) {

                binding.countryList.visibility = View.GONE
            } else {

                binding.countryList.visibility = View.VISIBLE
                viewModel.getCountryDetails()
            }
        }
//        binding.countryListText.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                Log.d("recyclerView", "It has focus")
//                binding.countryList.visibility = View.VISIBLE
//                viewModel.getCountryDetails()
//            } else {
//                Log.d("recyclerView", "It has focus gone")
//                binding.countryList.visibility = View.GONE
//            }
//        }

        viewModel.countryList.observe(viewLifecycleOwner){
            result->
            if(result.success==true){

                result.data?.let { adapter.setNewItems(it) }
                for(x in 0 until (result.data?.size ?: 0)){
                    countryList.add(result.data?.get(x)?.name.toString())
                    countryMap[result.data?.get(x)?.name.toString()] = result.data?.get(x)?._id.toString()
                }
            }
        }
//        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_items, countryList)
//        binding.autoCompleteTextView.setAdapter(arrayAdapter)
//        arrayAdapter.notifyDataSetChanged()
//        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
//            // Country selected from the dropdown
//            val selectedCountryName = countryList[position]
//            countryId = countryMap[selectedCountryName]
//            Log.d("autoCompleteText", countryId.toString())
//            binding.confirm.isEnabled = true
//            Log.d("TreesData", "Country is selected $treesCount $countryId")
//            binding.confirm.setBackgroundResource(R.drawable.blue_button_background)
//        }


        binding.treesPlantedNumber.text = treesCount
        binding.payAmount.text = amount


        binding.confirm.setOnClickListener {
            binding.confirm.disableMultiTap()
            Log.d("countrySelected", "${countryIdOne} $countryName")
            if (!countryIdOne.isNullOrEmpty()) {
                when (treesCount) {
                    "1 Tree" -> {
                        Log.d("TreesData", "One tree is selected")
                        purchaseManager.purchaseItem(
                            ONE_PLANT_ID,
                            purchaseType = PurchaseType.PlantPurchases,
                            country = countryIdOne,
                            trees = 1
                        )
                    }
                    "5 Trees" -> {
                        Log.d("TreesData", "5 tree is selected")
                        purchaseManager.purchaseItem(
                            FIVE_PLANTS_ID,
                            purchaseType = PurchaseType.PlantPurchases,
                            country = countryIdOne,
                            trees = 5
                        )
                    }
                    "10 Trees" -> {
                        Log.d("TreesData", "10 tree is selected")
                        purchaseManager.purchaseItem(
                            TEN_PLANTS_ID,
                            purchaseType = PurchaseType.PlantPurchases,
                            country = countryIdOne,
                            trees = 10
                        )
                    }
                }
            } else {
                // Handle when no country is selected
                // Show an error message or take necessary action
                Log.d("Confirmation", "Please select a country")
            }
        }



    }
    private fun setRecyclerAdapter() {
        binding.apply {
            manager = LinearLayoutManager(requireContext())
            this.countryList.layoutManager = manager
            adapter = CountryListAdapter(R.layout.drop_down_items, object : CountryListAdapter.CountryClick{



                override fun onClick(country: String, countryId: String) {
                    countryIdOne = countryId
                    countryName = country
                    binding.countryListText.setText("$country")
                    binding.countryList.visibility = View.GONE
                    binding.confirm.isEnabled = true
                    binding.confirm.setBackgroundResource(R.drawable.blue_button_background)
                }
            })

            binding.countryList.adapter = adapter

        }
    }


}