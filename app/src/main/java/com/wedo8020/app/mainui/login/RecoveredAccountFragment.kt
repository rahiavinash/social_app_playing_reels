package com.wedo8020.app.mainui.login

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.BuildConfig
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentRecoveredAccountBinding
import com.wedo8020.app.mainui.activities.MainActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountsViewModel
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountsViewModelFactory
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsDbCreation
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsEntity
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsRepo
import com.wedo8020.app.request.LoginRequest
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects

class RecoveredAccountFragment : BaseFragment() {
    private lateinit var binding : FragmentRecoveredAccountBinding
    private var userName : String ?=null
    private var userNameOne : String ?=null
    private var tempPassword : String ?=null
    private var Password : String ?=null
    private var androidId : String ?=null
    private var dialog: Dialog?=null
    private var countDatabase : Int ?= null
    private var checkIfUserExist : Int ?=null
    private var check : Boolean ?= true
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var viewModels : SwitchAccountsViewModel
    private lateinit var database : SwitchAccountsDbCreation    
    private lateinit var repository : SwitchAccountsRepo
    private lateinit var factory : SwitchAccountsViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding =  FragmentRecoveredAccountBinding.inflate(inflater,container,false)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        binding.userName.isClickable = false
        binding.passwordField.showSoftKeyboard()
        return binding.root
    }

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOnOutSideClick(binding.root)
        arguments?.let {

            userName = it.getString("username")
            binding.userName.setText(it.getString("username"))
            tempPassword= it.getString("password")
            Log.d("successBundle", "$userName $tempPassword")
        }
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890.-_"
        val filter = InputFilter { source, _, _, _, _, _ ->
            if (source != null && allowedChars.contains(source)) {
                null
            } else {
                ""
            }
        }


        //binding.userName.isEnabled= false
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val filterOne = InputFilter { source, _, _, dest, _, _ ->
            if (source != null && source.contains(" ")) {
                ""
            } else null
        }
        binding.passwordField.filters = arrayOf(filterOne)
        binding.reEnterPassword.filters = arrayOf(filterOne)

        androidId = Settings.Secure.getString(
            requireActivity().contentResolver,
            Settings.Secure.ANDROID_ID
        )
        database= SwitchAccountsDbCreation(requireContext())
        repository = SwitchAccountsRepo(database)
        factory = SwitchAccountsViewModelFactory(repository)
        viewModels = ViewModelProvider(this, factory)[SwitchAccountsViewModel::class.java]


        viewModels.getRowsCount()
        viewModels.userExists(userName.toString())

        //databaseData = viewModels.getAllAccounts() as ArrayList<SwitchAccountsEntity>
        viewModels.dataCount.observe(viewLifecycleOwner, Observer { count ->
            countDatabase = count
        })

        viewModels.userExists.observe(viewLifecycleOwner){
                userExist ->
            checkIfUserExist = userExist
            Log.d("userExistData", "user exist : $userExist")
        }
        binding.passwordField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {


                if((s?.length ?:0)> 20){
                    val truncatedText = s?.subSequence(0, 20)
                    binding.passwordField.setText(truncatedText)
                    binding.passwordField.setSelection(truncatedText?.length ?: 0)
                }

                if(s?.isEmpty() == true) {
                    Log.d("PasswordEmpty", "Password is empty")
                    binding.reEnterPasswordValidation.visibility = View.GONE
                }
                else if(s?.isEmpty() == false) {
                    if(s.toString() != binding.reEnterPassword.text.toString() && !binding.reEnterPassword.text.isNullOrEmpty()){
                        binding.reEnterPasswordValidation.setTextColor(Color.parseColor("#FF0340"))
                        binding.reEnterPasswordValidation.text ="Incorrect Password"
                        binding.reEnterPasswordValidation.visibility = View.VISIBLE
                    }
                    else {
                        binding.reEnterPasswordValidation.visibility = View.GONE
                    }
                }

            }
        })


        binding.reEnterPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun afterTextChanged(s: Editable?) {
                if((s?.length ?:0)> 20){
                    val truncatedText = s?.subSequence(0, 20)
                    binding.passwordField.setText(truncatedText)
                    binding.passwordField.setSelection(truncatedText?.length ?: 0)
                }
                if(s.toString().isNullOrEmpty()){
                    binding.reEnterPasswordValidation.visibility = View.GONE
                }
                else if(s?.isEmpty() == false) {
                    if(s.toString() != binding.passwordField.text.toString() && !binding.passwordField.text.isNullOrEmpty()){
                        binding.reEnterPasswordValidation.setTextColor(Color.parseColor("#FF0340"))
                        binding.reEnterPasswordValidation.text ="Incorrect Password"
                        binding.reEnterPasswordValidation.visibility = View.VISIBLE
                    }
                    else {
                        binding.reEnterPasswordValidation.visibility = View.GONE
                    }
                }







            }
        })

        val fullText = binding.privacyPolicyText.text

        val spannableString = SpannableString(fullText)

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = getString(R.string.TERM_URL)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }
        spannableString.setSpan(termsClickableSpan, binding.privacyPolicyText.text.indexOf("Terms of Use"), fullText.indexOf("Terms of Use") + "Terms of Use".length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), fullText.indexOf("Terms of Use"), fullText.indexOf("Terms of Use") + "Terms of Use".length, 0)

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = getString(R.string.PRIVACY_URL)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }
        }
        spannableString.setSpan(privacyClickableSpan, binding.privacyPolicyText.text.indexOf("Privacy Policy"), fullText.indexOf("Privacy Policy") + "Privacy Policy".length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), fullText.indexOf("Privacy Policy"), fullText.indexOf("Privacy Policy") + "Privacy Policy".length, 0)

        binding.privacyPolicyText.text = spannableString
        binding.privacyPolicyText.movementMethod = LinkMovementMethod.getInstance()

        binding.showPassword.setOnClickListener {
            binding.hidePassword.visibility = View.VISIBLE
            binding.showPassword.visibility = View.GONE
            binding.passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.passwordField.setSelection(binding.passwordField.text.length)
        }

        binding.hidePassword.setOnClickListener {

            binding.hidePassword.visibility = View.GONE
            binding.showPassword.visibility = View.VISIBLE
            binding.passwordField.transformationMethod =null
            binding.passwordField.setSelection(binding.passwordField.text.length)
        }

        binding.reEnterShowPassword.setOnClickListener {
            binding.reEnterHidePassword.visibility = View.VISIBLE
            binding.reEnterShowPassword.visibility = View.GONE
            binding.reEnterPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.reEnterPassword.setSelection(binding.reEnterPassword.text.length)
        }
        binding.reEnterHidePassword.setOnClickListener {
            binding.reEnterHidePassword.visibility = View.GONE
            binding.reEnterShowPassword.visibility = View.VISIBLE
            binding.reEnterPassword.transformationMethod = null
            binding.reEnterPassword.setSelection(binding.reEnterPassword.text.length)
        }

        setUpUi()





    }

    override fun onResume() {
        super.onResume()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpUi() {


        binding.loginButton.setOnClickListener {
            if(isInternetAvailable(requireContext())) {

                if (binding.userName.text.isNullOrEmpty()) {
                    Utils.showToast("Enter username")
                }
                else if (binding.passwordField.text.isNullOrEmpty()) {
                    Utils.showToast("Enter Create NEW Password")
                } else if (binding.passwordField.text.length < 4) {
                    Utils.showToast("Minimum password length is 4")
                } else if (binding.reEnterPassword.text.isNullOrEmpty()) {
                    Utils.showToast("Enter Re-enter NEW Password")
                } else if (binding.passwordField.text.toString() != binding.reEnterPassword.text.toString()) {
                    Utils.showToast("New password and confirm new password must be same")
                } else {
                    viewModel.login(
                        LoginRequest(
                            deviceToken = androidId,
                            username = binding.userName.text.toString(),
                            password = binding.passwordField.text.toString(),
                            isRecover = true,
                            appVersion = BuildConfig.VERSION_NAME

                        )
                    )
                }
            }
                else {
                binding.loginButton.text = "Login"
                binding.animationLoader.visibility = View.GONE
                Utils.showToast("Please check your internet connection.")
                }
            }


        observer()

        }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun observer() {
        viewModel.loginData.observe(viewLifecycleOwner, Observer { it ->
            if (it != null) {
                Log.d("loginSuccess", "api is logged in successfully ${it}")
                if (it.success) {

                    Log.d("loginSuccess", "api is logged in successfully ${it}")
                    Prefs.init().also { pref ->
                        pref.accessToken = it.data?.accessToken.toString()
                        pref.refreshToken = it.data?.refreshToken.toString()
                        pref.isLogIn = true
                    }


                    if (countDatabase == 0) {
                        Log.d("userExistData", "user exist : $countDatabase")
                        viewModels.addAccount(
                            SwitchAccountsEntity(
                                username = userName,
                                password = binding.passwordField.text.toString(),
                                deviceId = androidId,
                                profile = null,
                                isLogin = true,
                                loginTimestamp = getCurrentDateTime()
                            )
                        )

                    } else if (countDatabase == 2) {


                        if (checkIfUserExist == 1) {
                            Log.d("userExistData", "count is 2 user exist : $checkIfUserExist")
                            userName?.let {
                                viewModels.updatePassword(
                                    username = userName.toString(),
                                    password = binding.passwordField.text.toString()
                                )
                            }
                        }

                    else {
                        Log.d("userExistData", "count is 2 and user exist : $countDatabase")
                        viewModels.updateSecondRow(
                            SwitchAccountsEntity(
                                username = userName,
                                password = binding.passwordField.text.toString(),
                                deviceId = androidId,
                                profile = it.data?.profile.toString(),
                                isLogin = true,
                                loginTimestamp = getCurrentDateTime()
                            )
                        )
                    }
                    }




                    else if (countDatabase == 1) {

                        if (checkIfUserExist == 0) {
                            Log.d(
                                "userExistData",
                                "data is 1 and user not exist : $checkIfUserExist"
                            )
                            viewModels.addAccount(
                                SwitchAccountsEntity(
                                    username = userName,
                                    password = binding.passwordField.text.toString(),
                                    deviceId = androidId,
                                    profile = null,
                                    isLogin = true,
                                    loginTimestamp = getCurrentDateTime()
                                )
                            )

                        } else {
                            Log.d("userExistData", "data is 1 and user exist : $countDatabase")
                            userName?.let {
                                viewModels.updatePassword(
                                    username = userName.toString(),
                                    password = binding.passwordField.text.toString()
                                )
                            }
                        }

                    }


                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()

                } else {
                    Utils.showToast(it.message.toString())
                }
            }


        })

            viewModel.showLoading.observe(viewLifecycleOwner){
                if (it){
                    binding.loginButton.text=""
                    binding.animationLoader.visibility=View.VISIBLE
                }else{
                    binding.loginButton.text="Login"
                    binding.animationLoader.visibility=View.GONE
                }
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateTime(): String {

        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentTime.format(formatter)
    }



}