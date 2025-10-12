package com.wedo8020.app.mainui.home.exchangescreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.MainApplication
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentXchangeBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.response.ImpactData
import com.wedo8020.app.utils.NewMessage
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.zelpyy.SocketProvider.SocketIOImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Objects


class XchangeFragment : BaseFragment() {
    private var _binding: FragmentXchangeBinding? = null
    private val binding get() = _binding!!
    private var impactData : ImpactData? = null
    private var totalPlantsByWeDo : String?=null
    private var srcRemaining : String?=null
    private var srcValue : String ?= null
    private lateinit var viewModel : PurchasesViewModel
    private var dialogs: Dialog?=null

    private lateinit var socket: SocketIOImpl
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        showNavBottom()
        _binding = FragmentXchangeBinding.inflate(inflater, container, false)
        socket= MainApplication.get().socket

        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // getNotificationCount()
        val activity = requireActivity() as MainActivity

        viewModel = ViewModelProvider(this)[PurchasesViewModel::class.java]

        if(isInternetAvailable(requireContext())){
            showProgressDialog()
            viewModel.impactDetails()
        }
        else {
            showToast(getString(R.string.no_internet_message))
        }

        totalPlantsByWeDo = activity.impactData?.totalTreesByWeDo.toString()
        srcRemaining = activity.impactData?.remainingWD.toString()
        Log.d("callledSrc", "I am getting called src is $srcRemaining")
        //srcValue = activity.impactData?.WD.toString()
//        val wdValue = srcValue
//        val roundedValue = if (wdValue != null) {
//            try {
//                val bdValue = BigDecimal(wdValue.toString())
//                bdValue.setScale(6, BigDecimal.ROUND_HALF_UP).toDouble()
//            } catch (e: NumberFormatException) {
//                0.0f
//            }
//        } else {
//            0.0
//        }
//        srcValue = roundedValue.toString()
//        binding.bitcoinSrcText.text = "$srcValue SRC"
        viewModel.mutableBaseResponse.observe(viewLifecycleOwner) { impactDetails ->
            Log.d("callled", "I am getting called")
            if (impactDetails.success == true) {
                dialogs?.dismiss()
                impactData = impactDetails.data
                Log.d("ImpactData", impactDetails.data.toString())
                binding.treesPlantedValue.text = impactData?.totalTreesByWeDo.toString()
                srcRemaining = impactData?.remainingWD.toString()
                totalPlantsByWeDo = impactData?.totalTreesByWeDo.toString()
                srcValue = impactData?.WD.toString()
                val wdValue = srcValue
                val roundedValue = if (wdValue != null) {
                    try {
                        val bdValue = BigDecimal(wdValue.toString())
                        bdValue.setScale(6, BigDecimal.ROUND_HALF_UP).toDouble()
                    } catch (e: NumberFormatException) {
                        0.0f
                    }
                } else {
                    0.0
                }
                srcValue = roundedValue.toString()
                binding.bitcoinSrcText.text = "$srcValue SRC"


            } else {
                dialogs?.dismiss()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            dialogs?.dismiss()
            activity.let {

                    activity.homeTabClick()
                }
            }

        Log.d("impactDetails", " Impact details are ${impactData}")
        binding.treesPlantedValue.text = totalPlantsByWeDo
        binding.srcSupplyValue.text = srcRemaining
        binding.getPremiumButton.setOnClickListener {
            checkIfFragmentAttached {


                if (isInternetAvailable(requireContext())) {
                    findNavController().navigate(R.id.premiumPurchaseFragment)
                } else {
                    Utils.showToast("Please check your internet connect")
                }
            }

        }
        binding.plantTreesButton.setOnClickListener {
            checkIfFragmentAttached {


                if (isInternetAvailable(requireContext())) {
                    findNavController().navigate(R.id.plantTreesFragment)
                } else {
                    Utils.showToast("Please check your internet connect")
                }
            }

        }
        binding.plantImage.setOnClickListener {
            findNavController().navigate(R.id.mostTreesPlantedFragment)
        }
        binding.buySrcButton.setOnClickListener {

            checkIfFragmentAttached {


                if (isInternetAvailable(requireContext())) {
                    findNavController().navigate(R.id.buySrcFragment)
                } else {
                    Utils.showToast("Please check your internet connect")
                }
            }

        }
        binding.bitcoinImage.setOnClickListener {

            findNavController().navigate(R.id.walletFragment, bundle)
        }
        binding.heartImage.setOnClickListener {
            findNavController().navigate(R.id.mostLikedFragment)
        }

    }

    override fun onStop() {
        super.onStop()
        dialogs?.dismiss()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        dialogs?.dismiss()
        binding.bitcoinSrcText.text=""
    }

    companion object {
        val bundle = Bundle()
    }
    override fun onResume() {
        super.onResume()
        if (!socket.isConnected()) {
            socket.connect()

        } else {
            setSocketListenON()
            getNotificationCount()
        }
    }


    private fun getNotificationCount() {
        socket.emitMessage("totalNotification", "Bearer " + Prefs.init()
            .accessToken) { _, _ ->
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSocketListenON() {
        socket.listenOff("totalNotificationSuccess")
        socket.listenOff("error")
        socket.listenOn("totalNotificationSuccess") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                val obj = JSONObject(args[0].toString())
                val dataObject = obj.getJSONObject("data")
                Log.e("totalNotificationSuccess" ,dataObject.toString())
                val notificationsValue = dataObject.optInt("notifications", 0)

                val messageValue = dataObject.optInt("messages", 0)
                org.greenrobot.eventbus.EventBus.getDefault()
                    .post(NewMessage(messageValue.toString()))
            }
        }
        socket.listenOn("error"){args ->
            CoroutineScope(Dispatchers.Main).launch {

            }
        }
    }
    private fun showProgressDialog() {
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.progressbar)
        dialogs?.show()
    }

}