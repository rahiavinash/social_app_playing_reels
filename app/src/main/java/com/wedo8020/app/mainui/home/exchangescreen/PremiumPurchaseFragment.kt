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
import android.widget.TextView
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.databinding.FragmentPremiumPurchaseBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedo8020.app.R
import com.wedo8020.app.eventBus.DialogShown
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.NO_ADS_LIFE_TIME
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.NO_ADS_ONE_YEAR
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.VERIFIED_ACCOUNT_LIFE_TIME
import com.wedo8020.app.mainui.home.exchangescreen.InAppConstants.VERIFIED_ACCOUNT_ONE_YEAR
import com.wedo8020.app.repo.RemoteRepositaryImp
import com.wedo8020.app.request.PurchaseDetails
import com.wedo8020.app.utils.Utils.disableMultiTap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Objects


class PremiumPurchaseFragment : BaseFragment(), PurchaseManager.PurchaseViewCallback {

    private var _binding: FragmentPremiumPurchaseBinding? = null
    private var dialogs:Dialog?=null
    private val binding get() =
        _binding ?: error("Binding should not be accessed before onCreateView or after onDestroyView")

    private val repository = RemoteRepositaryImp()

    var dialog: Dialog? = null




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPremiumPurchaseBinding.inflate(inflater, container, false)
        hideNavBottom()
        return binding.root
    }
    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkIfFragmentAttached {
            dialog = Dialog(requireContext())
            dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

            dialogs = Dialog(requireContext())
            dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        }



        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.xchangeFragment)
        }
        val mainActivity = activity as MainActivity
        val purchaseManager = mainActivity.getPurchaseManager()
        purchaseManager.purchaseViewCallback = this
        val activity = requireActivity() as MainActivity

        binding.bonusVerifiedOneYear.text = activity.impactData?.arBonus?.premiumPurchase?.blueTick?.label
        binding.bonusVerifiedLifetime.text = activity.impactData?.arBonus?.premiumPurchase?.blueTick?.lifetimeLabel
        binding.bonusNoAdsYear.text = activity.impactData?.arBonus?.premiumPurchase?.noAds?.label
        binding.bonusNoAdsLifeTime.text = activity.impactData?.arBonus?.premiumPurchase?.noAds?.lifetimeLabel
        binding.bonusAiYear.text = activity.impactData?.arBonus?.premiumPurchase?.ai?.label
        binding.bonusAiLifeTime.text = activity.impactData?.arBonus?.premiumPurchase?.ai?.lifetimeLabel



        binding.fourNineDollarButton.text = "Buy 1 year\n${activity.inAppProductsPrice["com.wedo.verify.yearly"]}"
        binding.oneFourNineDollarButton.text = "Buy Lifetime\n${activity.inAppProductsPrice["com.wedo.verify.lifetime"]}"
        binding.textView6.text = "Buy 1 Year\n${activity.inAppProductsPrice["com.wedo.noads.yearly"]}"
        binding.textView7.text = "Buy Lifetime\n${activity.inAppProductsPrice["com.wedo.noads.lifetime"]}"
        binding.oneOneNineTextButton.text = "Buy 1 Year\n${activity.inAppProductsPrice["com.wedo.ai.yearly"]}"
        binding.threeFiveNineButton.text = "Buy LifeTime\n$${activity.inAppProductsPrice["com.wedo.ai.lifetime"]}"



        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.xchangeFragment)
        }
        binding.moreInfoButton.setOnClickListener {
            blueTickBottomSheet()
        }
        binding.moreInfoButtonTwo.setOnClickListener {
            noAdBottomSheet()
        }
        binding.moreInfoButtonThree.setOnClickListener {
            aiInfoBottomSheet()
        }


        CoroutineScope(Dispatchers.IO).launch {

            CoroutineScope(Dispatchers.Main).launch {
                showProgressDialog()
            }
            repository.getPurchaseDetails()
            { isSuccess: Boolean, response: PurchaseDetails ->
                if (isSuccess && response.success==true) {

                    dialogs?.dismiss()

                  Log.d("dataIs", response.data.toString())
                    if(response.data != null) {
                        Log.d("dataIs", response.data.toString())
                        CoroutineScope(Dispatchers.Main).launch {
                            if (response.data.noAds?.noAdsEnable == true && response.data.noAds.expireTime == null) {
                                Log.d("helloooo", "You have selected no ads for lifetime ")
                                binding.purchasedImage.visibility = View.VISIBLE
                                binding.textView6.isClickable = false
                                binding.textView7.isClickable = false
                                binding.purchasedImageOneYearNoAds.visibility = View.INVISIBLE
                            }
                            if (response.data.noAds?.noAdsEnable == true && response.data.noAds.expireTime != null) {
                                binding.purchasedImageOneYearNoAds.visibility = View.VISIBLE
                                binding.textView6.isClickable = false
                                Log.d("helloooo", "You have selected no ads for 1 year")
                            }
                            if (response.data.blueTick?.blueTickEnable == true && response.data.blueTick.expireTime != null) {
                                Log.d("helloooo", "You have selected blue tick for 1 year")
                                binding.purchasedImageOneYearVerifiedTick.visibility = View.VISIBLE
                                binding.fourNineDollarButton.isClickable = false
                            }
                            if (response.data.blueTick?.blueTickEnable == true && response.data.blueTick.expireTime == null) {
                                Log.d("helloooo", "You have selected blue tick for lifetime ")
                                binding.purchasedImageLifeTimeVerifiedTick.visibility = View.VISIBLE
                                binding.fourNineDollarButton.isClickable = false
                                binding.oneFourNineDollarButton.isClickable = false
                                binding.purchasedImageOneYearVerifiedTick.visibility = View.INVISIBLE
                            }
                        }
                    }


                    Log.d("getPurchaseDetail", response.toString())
                } else {
                    dialogs?.dismiss()
                    Log.d("getPurchaseDetail", response.toString())
                }
            }
        }


        binding.textView6.setOnClickListener {
            binding.textView6.disableMultiTap()
            binding.textView7.disableMultiTap()
            binding.fourNineDollarButton.disableMultiTap()
            binding.oneFourNineDollarButton.disableMultiTap()
            purchaseManager.purchaseItem(NO_ADS_ONE_YEAR, purchaseType = PurchaseType.PremiumPurchases, aiEnable = false,
                blueTickEnable = false,noAdsEnable = true)
        }

        binding.textView7.setOnClickListener {
            binding.textView6.disableMultiTap()
            binding.textView7.disableMultiTap()
            binding.fourNineDollarButton.disableMultiTap()
            binding.oneFourNineDollarButton.disableMultiTap()
            purchaseManager.purchaseItem(NO_ADS_LIFE_TIME, purchaseType = PurchaseType.PremiumPurchases,
                aiEnable = false, blueTickEnable = false,noAdsEnable = true)
        }

//        binding.fourNineDollarButton.setOnClickListener {
//            binding.textView6.disableMultiTap()
//            binding.textView7.disableMultiTap()
//            binding.fourNineDollarButton.disableMultiTap()
//            binding.oneFourNineDollarButton.disableMultiTap()
//            purchaseManager.purchaseItem(VERIFIED_ACCOUNT_ONE_YEAR, purchaseType = PurchaseType.PremiumPurchases,
//                aiEnable = false, blueTickEnable = true,noAdsEnable = false)
//        }
//        binding.oneFourNineDollarButton.setOnClickListener {
//            binding.textView6.disableMultiTap()
//            binding.textView7.disableMultiTap()
//            binding.fourNineDollarButton.disableMultiTap()
//            binding.oneFourNineDollarButton.disableMultiTap()
//            purchaseManager.purchaseItem(VERIFIED_ACCOUNT_LIFE_TIME,  purchaseType = PurchaseType.PremiumPurchases,
//                aiEnable = false, blueTickEnable = true,noAdsEnable = false)
//        }
//        binding.oneOneNineTextButton.setOnClickListener {
//
//            purchaseManager.purchaseItem(AI_ONE_YEAR, purchaseType = PurchaseType.PremiumPurchases,
//                aiEnable = true, blueTickEnable = false,noAdsEnable = false)
//        }
//        binding.threeFiveNineButton.setOnClickListener {
//            purchaseManager.purchaseItem(AI_LIFE_TIME,  purchaseType = PurchaseType.PremiumPurchases,
//                aiEnable = true, blueTickEnable = false,noAdsEnable = false)
//        }
    }



    private fun formatPrice(price: String?): String {
        return price?.let {
            // Remove non-numeric characters and then parse the string as a double
            val numericValue = it.replace("[^\\d.]".toRegex(), "").toDoubleOrNull()

            // Check if the parsing was successful and format the price
            numericValue?.let {
                String.format("₹ %.2f", it)
            } ?: "N/A"
        } ?: "N/A"
    }




    private fun blueTickBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = BlueTickInfo()
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }
    private fun noAdBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = NoAdsInfo()
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }
    private fun aiInfoBottomSheet() {
        val bottomSheetFragment: BottomSheetDialogFragment = AiFeatureInfo()
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    override fun onDestroyView() {
        super.onDestroyView()


    }
    companion object {
        val bundle = Bundle()
    }

    override fun noAdsLifeTimeClick() {
        Log.d("noAdsLifeTime", "verified tick life time")
        binding.purchasedImage.visibility = View.VISIBLE
        binding.textView6.isClickable = false
        binding.textView7.isClickable = false
        binding.purchasedImageOneYearNoAds.visibility = View.INVISIBLE
    }

    override fun noAdsOneYear() {
        Log.d("noAdsOneYear", "verified tick life time")
        binding.purchasedImageOneYearNoAds.visibility = View.VISIBLE
        binding.textView6.isClickable = false
    }

    override fun verifiedTickOneYear() {
        Log.d("verifiedTickOneYear", "verified tick life time")
        binding.purchasedImageOneYearVerifiedTick.visibility = View.VISIBLE
        binding.fourNineDollarButton.isClickable = false
    }

    override fun verifiedTickLifeTime() {
        Log.d("verifiedTick", "verified tick life time")
        binding.purchasedImageLifeTimeVerifiedTick.visibility = View.VISIBLE
        binding.fourNineDollarButton.isClickable = false
        binding.oneFourNineDollarButton.isClickable = false
        binding.purchasedImageOneYearVerifiedTick.visibility = View.INVISIBLE
    }
    interface ProductPriceCallback {
        fun onPriceFetched(price: String?)
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun DialogShown(event: DialogShown?) {
        if (event != null) {
            thanksDialogForDownload(event.dialog)
        }
    }


//    fun userClick(id:String){
//        if(id="com.wedo.noads.yearly"){
//
//        }
//        else if(id=""){
//
//        }
//        else if(id=""){
//
//        }
//        else if(id=""){
//
//            }
//    }


    private fun thanksDialogForDownload(text: String) {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.congratulations_pop_up)

        val logout = dialog?.findViewById(R.id.Logout) as TextView
        val plantText = dialog?.findViewById(R.id.ivAlert) as TextView

        plantText.text = "Thank you for planting trees. $text has been added to your wallet."

        logout.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()
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

