package com.wedo8020.app.mainui.home.profilesetting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentProfileSettingsBinding
import com.wedo8020.app.mainui.activities.LoginActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.exchangescreen.PurchasesViewModel
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountFragment
import com.wedo8020.app.response.LeaderboardRequest
import com.wedo8020.app.utils.DeepLinkType
import com.wedo8020.app.utils.Extension.share
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import com.wedo8020.app.utils.Utils.buildLink
import java.util.Objects
//import com.google.firebase.dynamiclinks.DynamicLink
//import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

class ProfileSettingsFragment : BaseFragment() {
    private lateinit var mBindIng: FragmentProfileSettingsBinding
    private val authViewModel by viewModels<PostViewModel>()
    var dialog: Dialog? = null
    var dialogs: Dialog? = null
    var description=""
    private lateinit var viewModel : PurchasesViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBindIng =
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile_settings, container, false)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        viewModel = ViewModelProvider(this)[PurchasesViewModel::class.java]

        //Todo hide BottomNavigation
        hideNavBottom()
        return mBindIng.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.prizeDetails()
        viewModel.prizeDetailsResponse.observe(viewLifecycleOwner, Observer {
                response ->
            if(response.success){
                Log.e("sasASAsASAsAS" , response.data?.shareAppText.toString())
                description = response.data?.shareAppText.toString()
            }
        })

        /** Change the center text in red color */
        val fullText = getString(R.string.restricted_blocked_reported)
        val spannable = SpannableString(fullText)
        val wordToColor = "Blocked"
        val startIndex = fullText.indexOf(wordToColor)
        val endIndex = startIndex + wordToColor.length
        spannable.setSpan(
            ForegroundColorSpan(Color.RED),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        mBindIng.tvBlockedAccounts.text = spannable


        mBindIng.tvLogout.setOnClickListener {
            logout()
        }
        mBindIng.IvSettingBack.setOnClickListener {
            findNavController().navigateUp()
        }
        mBindIng.tvDelete.setOnClickListener {
            findNavController().navigate(R.id.deleteFragment)
        }
        mBindIng.clShareWeDo.setOnClickListener {
            mBindIng.clShareWeDo.disableBtn()
            shares()
        }


        mBindIng.clBlockedAccount.setOnClickListener {
            findNavController().navigate(R.id.blockedUserFragment)
        }

        mBindIng.clExperience.setOnClickListener {
            findNavController().navigate(R.id.action_profileSettingsFragment_to_userExperienceFragment)
        }

        mBindIng.clChooseInterests.setOnClickListener {
            findNavController().navigate(R.id.changeYourInterestFragment)
        }
        mBindIng.ivFeedback.setOnClickListener {
            findNavController().navigate(R.id.feedBackFragment)
        }

        mBindIng.clDownLoadMyData.setOnClickListener {
            findNavController().navigate(R.id.action_profileSettingsFragment_to_downLoadMyDataFragment)
        }
        mBindIng.tvSwitchCreateNewAccount.setOnClickListener {
            showSwitchAccountBottomSheet()
        }

        mBindIng.clTermAndCondition.setOnClickListener {
            val url = getString(R.string.TERM_URL)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)

        }


        mBindIng.tvPrivacyPolicy.setOnClickListener {
            val url = getString(R.string.PRIVACY_URL)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }


        mBindIng.clCompetitionandPrizesPolicy.setOnClickListener {
            val url = getString(R.string.COMPETITIONS_PRIZES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        mBindIng.cltvConstitution.setOnClickListener {
            val url = getString(R.string.OUR_CONSITITUTION)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        mBindIng.clCommunity.setOnClickListener {
            val url = getString(R.string.COMMUNITY)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        mBindIng.clEtics.setOnClickListener {
            val url = getString(R.string.OUR_ETHICS)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        mBindIng.clFAQs.setOnClickListener {
            val url = getString(R.string.FAQ_LINK)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }


    fun logout() {
        dialog?.setCancelable(false)
        Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog?.setContentView(R.layout.logout_are_you_sure)

        val logout = dialog?.findViewById(R.id.Logout) as TextView
        val ivClose = dialog?.findViewById(R.id.ivClose) as ImageView

        ivClose.setOnClickListener {
            dialog?.dismiss()
        }
        logout.setOnClickListener {
            if (isInternetAvailable(requireActivity())) {
                showProgressDialogs()
                authViewModel.userLogout()
                setLogoutObserver()
            } else {
                dialogs?.dismiss()
                Utils.showToast("Please check your internet connection.")
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun showProgressDialogs() {
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        dialogs?.setCancelable(false)
        Objects.requireNonNull(dialogs?.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialogs?.setContentView(R.layout.progressbar)
        dialogs?.show()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setLogoutObserver() {
        authViewModel.userLogoutData.observe(requireActivity()) { response ->
            if (response != null) {
                if (response.success) {
                    dialogs?.dismiss()
                    Prefs.init().clear()
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }
        authViewModel.showLoading.observe(requireActivity()) {
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun shares() {
       /* val links = generateDeepLink(
            DeepLinkType.PROFILE,
            param1,
            binding?.tvName?.text.toString() + "(${binding?.tvUsername?.text}) on Wedo80",
            ""
        )*/
        val link = generateDeepLink(DeepLinkType.SHARE_WEDO,Prefs.init().profileId ,description ,"")
        share(
            "${description}\n"+link

        )

      /*  val link = buildLink(Prefs.init().profileId)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(
            Intent.EXTRA_TEXT, "Use this link to download WeDo 80. " +
                    "Just by using the App we’re helping the world. " +
                    "WeDo 80 gives away 80% of it’s profits to fight climate change and poverty and 10% to content creators. " +
                    "Isn’t that Next Gen Social?" +
                    "\n\n${link}`" +
                    ""
        )
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, "Share To:"))*/
    }

    private fun showSwitchAccountBottomSheet() {
        val bottomSheetFragment = SwitchAccountFragment(R.layout.fragment_switch_account)
       /* bundle.putString("posting", mBinding.tvPublic.text.toString())
        bottomSheetFragment.arguments = bundle*/
        bottomSheetFragment.height = 800
        bottomSheetFragment.show(childFragmentManager , bottomSheetFragment.tag)
    }
}