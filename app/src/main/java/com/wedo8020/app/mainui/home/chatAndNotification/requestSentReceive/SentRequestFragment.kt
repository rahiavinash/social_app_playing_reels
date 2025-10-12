package com.wedo8020.app.mainui.home.chatAndNotification.requestSentReceive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentSentRequestBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.ChatViewModel
import com.wedo8020.app.response.AcceptRejectRequest
import com.wedo8020.app.response.MessageRequest
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils


class SentRequestFragment : BaseFragment() {
    private lateinit var mBinding:FragmentSentRequestBinding
    private val viewModel by viewModels<ChatViewModel>()
    lateinit var adapter: RequestAdapter
    private lateinit var manager: RecyclerView.LayoutManager
    var userPosition : Int?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding=DataBindingUtil.inflate(layoutInflater,R.layout.fragment_sent_request, container, false )
        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isInternetAvailable(requireContext())){
            viewModel.sentMessageRequest(MessageRequest(offset = 0, limit = 200, order = -1, search = ""))
        }
        else {
            Utils.showToast("Please check your internet connection.")
        }


        setRecyclerAdapter()


        viewModel.sentMessagesResponse.observe(viewLifecycleOwner){
                response ->
            mBinding.rvNotificationList.visibility=View.VISIBLE
            if(response.success==true){
                val data = response.data
                if(data.isNullOrEmpty()){
                    mBinding.TvNoData.visibility = View.VISIBLE
                }
                else {
                    mBinding.TvNoData.visibility = View.GONE
                    response?.data?.let { adapter.setNewItems(it) }
                }


            }
        }
        viewModel.acceptRequestResponse.observe(viewLifecycleOwner){
            response ->
            if(response.success==true){
                userPosition?.let {
                    if (it >= 0 && it < adapter.itemCount) {
                        adapter.removeItem(it)
                    }
                }
                if(isInternetAvailable(requireContext())) {


                    if (adapter.list.size == 0) {
                        mBinding.rvNotificationList.visibility = View.GONE
                        viewModel.sentMessageRequest(
                            MessageRequest(
                                offset = 0,
                                limit = 200,
                                order = -1,
                                search = ""
                            )
                        )

                        mBinding.refresh.isRefreshing = false
                        mBinding.animationView.visibility = View.GONE
                    }
                }
                else {
                    Utils.showToast("Please check your internet connection.")
                }

            }

        }


        viewModel.showLoading.observe(viewLifecycleOwner){
            if (it) {
                mBinding.animationView.visibility = View.VISIBLE
            } else {

                mBinding.animationView.visibility = View.GONE
            }
        }
        mBinding.refresh.setOnRefreshListener {

            if(isInternetAvailable(requireContext())){
                mBinding.rvNotificationList.visibility=View.GONE
                viewModel.sentMessageRequest(
                    MessageRequest(
                        offset = 0,
                        limit = 200,
                        order = -1,
                        search = ""
                    )
                )

                mBinding.refresh.isRefreshing = false
                mBinding.animationView.visibility = View.GONE
            }
            else {
                Utils.showToast("Please check your internet connection.")
            }



        }
    }


    private fun setRecyclerAdapter() {
        if(isInternetAvailable(requireContext())){
            mBinding.apply {
                manager = LinearLayoutManager(requireContext())
                this.rvNotificationList.layoutManager = manager
                adapter = RequestAdapter(R.layout.request_recycler_data, object : RequestAdapter.RequestItemClick{
                    override fun cancelOnClick(accountId: String, position: Int) {

                        userPosition = position
                        viewModel.acceptReject(AcceptRejectRequest(status = "cancel", id= accountId)) }

                }, object : RequestAdapter.ItemClick{
                    override fun onClick(accountId: String) {
                        val bundle = Bundle()
                        bundle.putString("mentionUser", accountId)
                        if (accountId == Prefs.init().userId){
                            findNavController().navigate(R.id.profileFragment)
                        }else{
                            findNavController().navigate(
                                R.id.otherProfileFragment,
                                bundle
                            )
                        }

                    }

                })
                mBinding.rvNotificationList.adapter = adapter

            }
        }
        else {
            Utils.showToast("Please check your internet connection.")
        }

    }


}