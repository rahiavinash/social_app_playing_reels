package com.wedo8020.app.mainui.home.postFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R

import com.wedo8020.app.databinding.FragmentSearchTabBinding
import com.wedo8020.app.mainui.adapter.SearchListAdapter
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.HomeFragment
import com.wedo8020.app.request.SearchRequest
import com.wedo8020.app.request.UserRequest
import com.wedo8020.app.response.Data
import com.wedo8020.app.response.SearchBaseResponse
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.RecyclerViewScrollListener
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.observeOnce
import java.lang.ref.WeakReference

private const val ARG_PARAM1 = "position"

class SearchTabFragment : BaseFragment(), SearchListAdapter.PostClickListener {
    private var position: Int? = null
    private var binding: FragmentSearchTabBinding? = null
    private var searchRequest: SearchRequest? = null
    var searchText = ""
    private val mViewModel by viewModels<PostViewModel>()
    private var adapter: SearchListAdapter? = null
    private var manager: GridLayoutManager? = null
    private var firstAPIHit = false
    private var searchLiveData: LiveData<SearchBaseResponse>? = null
    private var observer: Observer<SearchBaseResponse>? = null
    private var offset = 0
    private var isLoading = false
    private var dataList: ArrayList<Data>? = null


    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            SearchTabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_PARAM1)
        }


        searchRequest = SearchRequest(
            type = when (position) {
                0 -> {
                    "post"
                }

                1 -> {
                    "user"
                }

                2 -> {
                    "hashtag"
                }

                else -> {
                    "post"
                }
            }
        )


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchTabBinding.inflate(inflater, container, false)

        setUpUi()
        return binding!!.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpUi() {
        val weakContext = WeakReference(requireContext())
//        hitSearchApi()
        binding?.apply {
            searchRecycler.setOnTouchListener { view, motionEvent ->
                hideSearchKeyboard()
                false
            }
            manager = if (position == 0) {
                GridLayoutManager(weakContext.get(), 2, GridLayoutManager.VERTICAL, false)
            } else {
                GridLayoutManager(weakContext.get(), 1, GridLayoutManager.VERTICAL, false)
            }
            mainLayout.setOnClickListener {
                hideSearchKeyboard()
            }
            searchRecycler.layoutManager = manager
            adapter = SearchListAdapter(searchRequest!!.type, this@SearchTabFragment)
            searchRecycler.adapter = adapter
            dataList?.toList()?.let { adapter?.setNewItem(it) }
        }

        /* val marginInPixels = resources.getDimensionPixelSize(R.dimen.item_margin) // Get margin size in pixels
         recyclerView.addItemDecoration(CustomItemDecoration(marginInPixels))*/
        if (position == 0) {
            setOnGestureBack(binding!!.root)
        }
        if (position == 0) {
            hitSearchApi()
        }

        setRecyclerScroll()

    }

    private fun hideSearchKeyboard(){
        if (parentFragment!=null && parentFragment is SearchFragment){
            val parentFrag = requireParentFragment() as SearchFragment
            parentFrag.hideKeyboard()

        }
    }
    private fun setRecyclerScroll() {
        binding?.searchRecycler?.addOnScrollListener(object : RecyclerViewScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

//                hideSearchKeyboard()
                if (!recyclerView.canScrollVertically(1) && dy > 0 && !isLoading) {
                    // Load more items if needed
                    isLoading = true
                    offset += 10
                    loadMoreData()
                }
            }

            override fun onItemIsFirstVisibleItem(index: Int, lastItemIndex: Int) {

            }
        })
    }

    fun searchByText(search: String) {
        if (searchText == search){
            return
        }
        if (observer != null) {
            searchLiveData?.removeObserver(observer!!)
        }
        Log.e("sagsdf","searchByText")
        searchText = search
        offset = 0
        searchRequest?.offset = offset
        searchRequest?.search = search
        searchLiveData = mViewModel
            .searchList(searchRequest!!)

        observer = Observer { response ->
            if (response != null) {
                if (response.success) {
                    if (response.data != null) {
                        isLoading = false

                        // Additional adjustment to the BottomSheet height based on the keyboard height
                        adapter?.setNewItem(response.data!!.filterNotNull())

                        if (adapter?.searchList.isNullOrEmpty()){
                            binding?.noDataFound?.visibility = View.VISIBLE
                        }else{
                            binding?.noDataFound?.visibility = View.GONE
                        }
//                                        return true
                    }
                }
            }
        }
        searchLiveData?.observeOnce(observer!!)
//        searchLiveData?.observe(viewLifecycleOwner,observer!!)

    }

    private fun loadMoreData() {
        if (observer != null) {
            searchLiveData?.removeObserver(observer!!)
        }
        searchRequest?.apply {
            search = searchText
            offset = this@SearchTabFragment.offset
        }
        searchLiveData = mViewModel
            .searchList(searchRequest!!)

        observer = Observer { response ->
            if (response.success) {
                if (!response.data.isNullOrEmpty()) {
                    isLoading = false
                    // Additional adjustment to the BottomSheet height based on the keyboard height
                    adapter?.addNewItem(response.data!!.filterNotNull())

                    if (adapter?.searchList.isNullOrEmpty()){
                        binding?.noDataFound?.visibility = View.VISIBLE
                    }else{
                        binding?.noDataFound?.visibility = View.GONE
                    }
//                                        return true
                }
            }
        }
        searchLiveData?.observeOnce(observer!!)
    }

    fun hitSearchApi() {
        if (!firstAPIHit) {
            binding?.progressBar?.visibility = View.VISIBLE
            Log.e("sagsdf","firstAPIHit")
            offset = 0
            if (Utils.isInternetAvailables(requireContext())){
                searchRequest?.offset = offset
                val viewModel = mViewModel.searchList(searchRequest!!)
                viewModel.observeOnce { response ->
                    if (response != null) {
                        if (response.success) {
                            if (response.data != null) {
                                isLoading = false
                                if (adapter?.searchList.isNullOrEmpty()) {
                                    adapter?.setNewItem(response.data!!.filterNotNull())
                                }
                                if (adapter?.searchList.isNullOrEmpty()){
                                    binding?.noDataFound?.visibility = View.VISIBLE
                                }else{
                                    binding?.noDataFound?.visibility = View.GONE
                                }
                            }
                        }
                    }
                    binding?.progressBar?.visibility = View.GONE

                }
                firstAPIHit = true
            } else {
                binding?.progressBar?.visibility = View.GONE

                Utils.showToast(getString(R.string.no_internet_message))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        destroyAllResources()
//        firstAPIHit = false
    }


    private fun destroyAllResources() {
        searchLiveData?.removeObserver(observer!!)
        dataList = adapter?.searchList
        binding = null
        manager = null
        adapter = null

    }

    override fun accountClick(user: String) {
        hideSearchKeyboard()
        if (user == Prefs.init().userId) {
            findNavController().navigate(
                R.id.profileFragment
            )
        } else {
            val bundle = Bundle()
            bundle.putString("mentionUser", user)
            findNavController().navigate(
                R.id.action_searchFragment_to_otherprofileFragment,
                bundle
            )
        }

    }

    override fun followUnfollowClick(position: Int) {
        hideSearchKeyboard()
        adapter?.searchList?.get(position)?.let {
            when (it.followStatus) {
                "1" -> {
                    it.followStatus = "2"
                    mViewModel.followUnfollowUserRequest = UserRequest(it._id)
                    mViewModel.followUnfollowUser()
//                        .text = requireContext().getText(R.string.follow)
                }

                "2" -> {
                    mViewModel.followUnfollowUserRequest = UserRequest(it._id)
                    mViewModel.followUnfollowUser()
                    if (it.accountType.equals(
                            AppConstant.PRIVATE,
                            ignoreCase = true
                        )
                    ) {
                        it.followStatus = "3"
                        /*  tvFollowBtn.text =
                              requireContext().getText(R.string.requested)*/
                    } else {
                        /* it.followStatus = "1"
                         tvFollowBtn.text =
                             requireContext().getText(R.string.following)*/
                    }
                }

                "3" -> {
                    it.followStatus = "2"
                    mViewModel.followUnfollowUserRequest =
                        UserRequest(it._id, AppConstant.CANCEL)
                    mViewModel.postFollowRequestStatus()
//                        tvFollowBtn.text = requireContext().getText(R.string.follow)
                }

                else -> {
                    Utils.showToast(requireContext().getString(R.string.something_went_wrong))
                }
            }
        }
    }

    override fun hashtagClick(text: String) {
        hideSearchKeyboard()
        val bundle = Bundle()
        bundle.putString("hashtag", text)
        bundle.putString("type", "hashtag")
        findNavController().navigate(
            R.id.action_searchFragment_to_show_profile_post_fragment,
            bundle
        )
    }

    override fun postClick(data: Data) {
        hideSearchKeyboard()
        val bundle = Bundle()
        bundle.putInt("position", 0)
        bundle.putString("dataList", data._id)
        findNavController().navigate(
            R.id.action_searchFragment_to_show_profile_post_fragment,
            bundle
        )
    }


}