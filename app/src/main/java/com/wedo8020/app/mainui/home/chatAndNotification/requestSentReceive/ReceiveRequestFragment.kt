package com.wedo8020.app.mainui.home.chatAndNotification.requestSentReceive

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentReceiveRequestBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.chatAndNotification.chatViewModel.ChatViewModel
import com.wedo8020.app.response.AcceptRejectRequest
import com.wedo8020.app.response.MessageRequest
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import java.util.Objects


class ReceiveRequestFragment : BaseFragment() {
    private lateinit var mBinding: FragmentReceiveRequestBinding
    private val viewModel by viewModels<ChatViewModel>()
    lateinit var adapter: RequestReceiveAdapter
    var selectedUser: Int? = null
    var dialog: Dialog? = null
    //private lateinit var connectivityManager: ConnectivityManager
    //private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var manager: RecyclerView.LayoutManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_receive_request, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        dialog = Dialog(requireContext())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

//        connectivityManager =
//            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        networkCallback = object : ConnectivityManager.NetworkCallback() {
//            override fun onAvailable(network: Network) {
//                viewModel.receiveMessageRequest(
//                    MessageRequest(
//                        offset = 0,
//                        limit = 200,
//                        order = -1,
//                        search = ""
//                    ))
//                mBinding.rejectAll.visibility = View.VISIBLE
//            }
//
//            override fun onLost(network: Network) {
//                mBinding.rejectAll.visibility = View.GONE
//                Utils.showToast("Please check your internet connection.")
//            }
//        }


                if (isInternetAvailable(requireContext())) {
            viewModel.receiveMessageRequest(
                MessageRequest(
                    offset = 0,
                    limit = 200,
                    order = -1,
                    search = ""
                )
            )
        } else {
                    mBinding.rejectAll.visibility = View.GONE
            Utils.showToast("Please check your internet connection.")
        }

        setRecyclerAdapter()
        viewModel.receivedMessagesResponse.observe(viewLifecycleOwner) { response ->
            mBinding.rvNotificationList.visibility = View.VISIBLE

            if (response.success == true) {
                val data = response.data
                if (data.isNullOrEmpty()) {

                    mBinding.TvNoData.visibility = View.VISIBLE
                    mBinding.rejectAll.visibility = View.GONE
                } else {
                    mBinding.rejectAll.visibility = View.VISIBLE
                    mBinding.TvNoData.visibility = View.GONE
                    adapter.setNewItems(data)

                }
            }
        }
        mBinding.rejectAll.setOnClickListener {
            deleteAllRequests()
        }

        viewModel.showLoading.observe(viewLifecycleOwner) {
            if (it) {
                //showProgressDialog()
                mBinding.animationView.visibility = View.VISIBLE
            } else {

                mBinding.animationView.visibility = View.GONE
            }
        }
        viewModel.acceptRequestResponse.observe(viewLifecycleOwner) { response ->
            if (response.success == true) {
                Log.d("ApiAcceptReject", "Api is hit ${response.message}")
                //selectedUser?.let { adapter.removeItem(it) }
                if (isInternetAvailable(requireContext())) {
                    if (adapter.list.size == 0) {
                        Log.d("receiverequestFragment", "adapter size is zero $selectedUser")
                        mBinding.rvNotificationList.visibility = View.GONE
                        viewModel.receiveMessageRequest(MessageRequest())



                        mBinding.animationView.visibility = View.GONE
                        mBinding.refresh.isRefreshing = false
                    }
                    else {
                        Log.d("receiverequestFragment", "adapter size is not zero $selectedUser")
                        selectedUser?.let { adapter.removeItem(it)}
                        showToast(response.message.toString())
                        mBinding.rvNotificationList.visibility = View.GONE
                        viewModel.receiveMessageRequest(
                            MessageRequest(
                                offset = 0,
                                limit = 200,
                                order = -1,
                                search = ""
                            )
                        )
                        mBinding.animationView.visibility = View.GONE
                        mBinding.refresh.isRefreshing = false

                    }
                } else {
                    Utils.showToast("Please check your internet connection.")
                }


            }
            else {
                mBinding.animationView.visibility = View.GONE
                mBinding.refresh.isRefreshing = false
            }

        }

        mBinding.refresh.setOnRefreshListener {

            if (isInternetAvailable(requireContext())) {
                mBinding.rvNotificationList.visibility = View.GONE
                viewModel.receiveMessageRequest(
                    MessageRequest(
                        offset = 0,
                        limit = 200,
                        order = -1,
                        search = ""
                    )
                )


                mBinding.animationView.visibility = View.GONE
                mBinding.refresh.isRefreshing = false
            } else {
                Utils.showToast("Please check your internet connection.")
            }


        }
    }

    private fun setRecyclerAdapter() {

        if (isInternetAvailable(requireContext())) {
            mBinding.apply {
                manager = LinearLayoutManager(requireContext())
                this.rvNotificationList.layoutManager = manager
                adapter = RequestReceiveAdapter(
                    R.layout.request_recycler_data,
                    object : RequestReceiveAdapter.RequestItemClick {
                        override fun onClick(
                            accountId: String,
                            requestType: String,
                            position: Int
                        ) {
                            selectedUser = position
                            when (requestType) {
                                "Accept" -> {
                                    viewModel.acceptReject(
                                        AcceptRejectRequest(
                                            status = "accept",
                                            id = accountId
                                        )
                                    )
                                    adapter.notifyItemChanged(position)
                                }

                                "Reject" -> {
                                    viewModel.acceptReject(
                                        AcceptRejectRequest(
                                            status = "reject",
                                            id = accountId
                                        )
                                    )
                                    adapter.notifyItemChanged(position)
                                }

                                else -> {
                                    val bundle = Bundle()
                                    bundle.putString("mentionUser", accountId)

                                    if (accountId == Prefs.init().userId){
                                        findNavController().navigate(R.id.profileFragment)
                                    } else{
                                        findNavController().navigate(
                                            R.id.otherProfileFragment,
                                            bundle
                                        )
                                    }

                                }
                            }
                        }

                    })
                mBinding.rvNotificationList.adapter = adapter

            }
        } else {
            Utils.showToast("Please check your internet connection.")
        }

    }

    fun deleteAllRequests() {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.delete_all_requests)

        val logout = dialog?.findViewById(R.id.reject) as TextView
        val ivClose = dialog?.findViewById(R.id.ivClose) as ImageView
        ivClose.setOnClickListener {
            dialog?.dismiss()
        }
        logout.setOnClickListener {
            if (isInternetAvailable(requireActivity())) {
                viewModel.acceptReject(AcceptRejectRequest(status = "clear"))
                adapter.clearData()
            } else {

                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }


}
object NetworkUtils {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}