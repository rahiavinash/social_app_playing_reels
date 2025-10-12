package com.wedo8020.app.mainui.login.switchaccounts


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.BuildConfig
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentSwitchAccountBinding
import com.wedo8020.app.mainui.activities.LoginActivity
import com.wedo8020.app.mainui.activities.MainActivity

import com.wedo8020.app.mainui.base.BaseBottomSheetFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.login.LoginViewModel
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsDbCreation
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsEntity
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsRepo
import com.wedo8020.app.request.LoginRequest
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.disableMultiTap
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import java.util.Objects


class SwitchAccountFragment(override val layoutId: Int) : BaseBottomSheetFragment<FragmentSwitchAccountBinding>() {
    private lateinit var viewModel : SwitchAccountsViewModel
    private lateinit var database : SwitchAccountsDbCreation
    private lateinit var repository : SwitchAccountsRepo
    private lateinit var factory : SwitchAccountsViewModelFactory
    private val mViewModel by viewModels<LoginViewModel>()
    private val authViewModel by viewModels<PostViewModel>()
    lateinit var adapter: SwitchAdapter
    private var userName : String ?=null
    private var dialog: Dialog?=null
    private lateinit var socket: SocketIOImpl
    private lateinit var manager: RecyclerView.LayoutManager


    override fun onSetUpUi(binding: FragmentSwitchAccountBinding) {

        socket = MainApplication.get().socket
        database= SwitchAccountsDbCreation(requireContext())
        repository = SwitchAccountsRepo(database)
        factory = SwitchAccountsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SwitchAccountsViewModel::class.java]
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        viewModel.getAllAccounts()

       // viewModel.deleteAll()
        setRecyclerAdapter(binding)
        viewModel.allAccounts.observe(viewLifecycleOwner, Observer {
            list->
            Log.d("listSize", "$list")
            if(list.isNotEmpty()){

                adapter.setNewItems(list)
            }
        })
        mViewModel.loginData.observe(viewLifecycleOwner, Observer {
            response->
            if(response.success){

                dialog?.dismiss()
                Prefs.init().clear()
                Prefs.init().also { pref ->
                    pref.accessToken = response.data?.accessToken.toString()
                    pref.refreshToken = response.data?.refreshToken.toString()
                    pref.isLogIn = true
                    pref.username = userName.toString()
                    Prefs.init().userProfile = response.data?.profile.toString()
                }
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            else {
                dialog?.dismiss()
                Utils.showToast(response.message.toString())
            }})
            authViewModel.userLogoutData.observe(viewLifecycleOwner) { response ->
                if (response != null) {
                    if (response.success) {
                        binding.animationLoader.visibility = View.GONE
                        Prefs.init().clear()
                        val intent = Intent(requireActivity(), LoginActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    else {
                        binding.animationLoader.visibility = View.GONE
                    }
                }
            }

        Log.d("userLoggedIn",Prefs.init().username.toString())

        binding.tvAnotherAccount.setOnClickListener {
            binding.tvAnotherAccount.disableMultiTap(1500)
            binding.tvNewAccount.disableMultiTap(1500)
            binding.animationLoader.visibility = View.VISIBLE
            authViewModel.userLogout()


        }
        binding.tvNewAccount.setOnClickListener {
            binding.tvAnotherAccount.disableMultiTap(1500)
            binding.tvNewAccount.disableMultiTap(1500)
            binding.animationLoader.visibility = View.VISIBLE
            authViewModel.userLogout()

        }


    }

    private fun setRecyclerAdapter(mBinding: FragmentSwitchAccountBinding) {
        mBinding.apply {
            manager = LinearLayoutManager(requireContext())
            this.recyclerView.layoutManager = manager
            adapter = SwitchAdapter(R.layout.switch_account_recyclerview, object : SwitchAdapter.CheckBoxClickListener{
                override fun onCheckBoxClick(item: SwitchAccountsEntity, isChecked: Boolean) {
                        if(isChecked && item.username != Prefs.init().username){
                            showProgressDialog()
                            Prefs.init().username =item.username.toString()
                            adapter.notifyDataSetChanged()
                            socket.disconnect()
                            userName = item.username
                           Log.d("userLoginDetails", " $item")
                            mViewModel.login(
                                LoginRequest(username = item.username.toString(), password = item.password.toString(),
                                deviceToken = item.deviceId.toString(), appVersion = BuildConfig.VERSION_NAME)
                            )
                        }
                    else {
                        Toast.makeText(requireContext(), "Please check account", Toast.LENGTH_SHORT).show()
                        }
                }

            })

            mBinding.recyclerView.adapter = adapter

        }
    }
    private fun showProgressDialog() {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.progressbar)
        dialog?.show()
    }

}