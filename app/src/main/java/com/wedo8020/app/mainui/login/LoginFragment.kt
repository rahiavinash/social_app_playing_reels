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
import android.text.TextPaint
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
import com.wedo8020.app.databinding.FragmentLoginBinding
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


class LoginFragment : BaseFragment() {

    private lateinit var binding: FragmentLoginBinding
    private val mViewModel by viewModels<LoginViewModel>()
    private lateinit var viewModel : SwitchAccountsViewModel
    private lateinit var database : SwitchAccountsDbCreation
    private lateinit var repository : SwitchAccountsRepo
    private var countDatabase : Int ?= null
    private var checkIfUserExist : Int ?=null
    private lateinit var factory : SwitchAccountsViewModelFactory
    private var userName : String?=null
    private var password : String?=null
    private var dialog: Dialog?=null
    private var androidId : String?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        if (!::binding.isInitialized) {
            binding = FragmentLoginBinding.inflate(inflater, container, false)
        }
        binding.username.showSoftKeyboard()
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("HardwareIds")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890.-_"

        val filter = InputFilter { source, _, _, dest, _, _ ->
            if (source != null && !source.all { allowedChars.contains(it) }) {
                ""
            } else null
        }




        val filterTwo = InputFilter { source, _, _, dest, _, _ ->
            if (source != null && source.contains(" ")) {
                ""
            } else null
        }

        binding.username.filters = arrayOf(filter)
        binding.password.filters = arrayOf(filterTwo)

        androidId = Settings.Secure.getString(
            requireActivity().contentResolver,
            Settings.Secure.ANDROID_ID
        )

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        hideKeyboardOnOutSideClick(binding.root)


        database= SwitchAccountsDbCreation(requireContext())
        repository = SwitchAccountsRepo(database)
        factory = SwitchAccountsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[SwitchAccountsViewModel::class.java]

        viewModel.getRowsCount()


        //databaseData = viewModels.getAllAccounts() as ArrayList<SwitchAccountsEntity>
        viewModel.dataCount.observe(viewLifecycleOwner, Observer { count ->
            countDatabase = count
        })

        viewModel.userExists.observe(viewLifecycleOwner){
                userExist ->
            checkIfUserExist = userExist
            Log.d("userExistData", "user exist : $userExist")
        }

        binding.username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
               if(!(s.isNullOrBlank())){
                   userName = s.toString()
                   Log.d("userExistData", "Username exist is $userName")
                   viewModel.userExists(userName.toString())
               }
            }
        })

        binding.hidePassword.setOnClickListener {
            binding.hidePassword.visibility = View.GONE
            binding.showPassword.visibility = View.VISIBLE
            binding.password.transformationMethod =null
            binding.password.setSelection(binding.password.text.length)
        }
        binding.showPassword.setOnClickListener {
            binding.hidePassword.visibility = View.VISIBLE
            binding.showPassword.visibility = View.GONE
            binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.password.setSelection(binding.password.text.length)
        }


        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if(!(s.isNullOrBlank())){
                   password = s.toString()
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

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
            }
        }

        spannableString.setSpan(termsClickableSpan, binding.privacyPolicyText.text.indexOf("Terms of Use"), fullText.indexOf("Terms of Use") + "Terms of Use".length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), binding.privacyPolicyText.text.indexOf("Terms of Use"), fullText.indexOf("Terms of Use") + "Terms of Use".length, 0)

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = getString(R.string.PRIVACY_URL)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
            }
        }

        spannableString.setSpan(privacyClickableSpan, binding.privacyPolicyText.text.indexOf("Privacy Policy"), fullText.indexOf("Privacy Policy") + "Privacy Policy".length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), binding.privacyPolicyText.text.indexOf("Privacy Policy"), fullText.indexOf("Privacy Policy") + "Privacy Policy".length, 0)

        binding.privacyPolicyText.text = spannableString

        binding.privacyPolicyText.movementMethod = LinkMovementMethod.getInstance()
        binding.privacyPolicyText.highlightColor = Color.TRANSPARENT // This prevents the TextView from highlighting when clicked
        binding.privacyPolicyText.text = spannableString
        binding.privacyPolicyText.movementMethod = LinkMovementMethod.getInstance()


        setUpUi()
        observer()
    }


    private fun setUpUi() {

        checkIfFragmentAttached {

        }
        binding.login.setOnClickListener {
            checkIfFragmentAttached {
            if (isInternetAvailable(requireContext())) {

                loginUser(binding.username.text.toString(), binding.password.text.toString())
//                if (binding.username.text.toString().isEmpty()) {
//
//                } else if (binding.password.text.toString().isEmpty()) {
//
//                } else {
//
//                    mViewModel.login(
//                        LoginRequest(
//                            deviceToken = androidId,
//                            username = userName.toString(),
//                            password = password.toString(),
//                            appVersion = BuildConfig.VERSION_NAME
//                        )
//                    )
//
//                }
            } else {
                binding.login.text = "Login"
                binding.animationLoader.visibility = View.GONE
                Utils.showToast("Please check your internet connection.")
            }
        }


        }


    }


    fun loginUser(username : String, password : String){

        if(username.isEmpty()){
            Utils.showToast("Please enter user name")
        }
        else if (password.isEmpty()){
            Utils.showToast("Please enter password")
        }
        else {
            if (binding.username.text.toString().isEmpty()) {

            } else if (binding.password.text.toString().isEmpty()) {

            } else {

                mViewModel.login(
                    LoginRequest(
                        deviceToken = androidId,
                        username = userName.toString(),
                        password = password.toString(),
                        appVersion = BuildConfig.VERSION_NAME
                    )
                )

            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observer() {

        mViewModel.loginData.observe(viewLifecycleOwner, Observer {
            response ->
                Log.d("loginSuccess", "api is logged in successfully $response")
                if (response.success) {
                    Prefs.init().clear()
                    Prefs.init().also { pref ->
                        pref.accessToken = response.data?.accessToken.toString()
                        pref.refreshToken = response.data?.refreshToken.toString()
                        pref.isLogIn = true
                        pref.username = userName.toString()
                        pref.userProfile = response.data?.profile.toString()
                    }

                    Log.d("timeZoneDate", "${getCurrentDateTime()}")
//                    viewModel.dataCount.observe(viewLifecycleOwner, Observer{
//                        count->
//                        if(count<2){
//                            viewModel.addAccount(SwitchAccountsEntity(username = binding.username.text.toString(), password = binding.password.text.toString(),
//                                deviceId = androidId, profile= response.data?.profile.toString(), isLogin = true, loginTimestamp = getCurrentDateTime()))
//                        }
//                        else if(count==2){
//                            viewModel.updateSecondRow(SwitchAccountsEntity(username = binding.username.text.toString(), password = binding.password.text.toString(),
//                                deviceId = androidId, profile = response.data?.profile.toString(), isLogin = true,loginTimestamp = getCurrentDateTime()))
//                        }
//                    })

                        if (countDatabase ==0) {
                            // If count is less than 2, add a new account
                            viewModel.addAccount(SwitchAccountsEntity(
                                username = binding.username.text.toString(),
                                password = binding.password.text.toString(),
                                deviceId = androidId,
                                profile = response.data?.profile.toString(),
                                isLogin = true,
                                loginTimestamp = getCurrentDateTime()
                            ))
                        } else if (countDatabase == 2) {
                            // If count is exactly 2, update the second row if the user exists

                                if (checkIfUserExist == 0) {
                                    viewModel.updateSecondRow(SwitchAccountsEntity(
                                        username = binding.username.text.toString(),
                                        password = binding.password.text.toString(),
                                        deviceId = androidId,
                                        profile = response.data?.profile.toString(),
                                        isLogin = true,
                                        loginTimestamp = getCurrentDateTime()
                                    ))
                                }

                        }

                        else if(countDatabase==1){

                                if (checkIfUserExist == 0) {
                                    viewModel.addAccount(SwitchAccountsEntity(
                                        username = binding.username.text.toString(),
                                        password = binding.password.text.toString(),
                                        deviceId = androidId,
                                        profile = response.data?.profile.toString(),
                                        isLogin = true,
                                        loginTimestamp = getCurrentDateTime()
                                    ))
                                }

                        }






                    Log.d("loginResponse", " ${response.success} ${response.data}")
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else  {
                    Log.d("loginSuccess", "api is logged in successfully $response")


                        Utils.showToast(response.message.toString())

//                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }

        })

        mViewModel.showLoading.observe(requireActivity()){
           if (it){
               binding.login.text=""
               binding.animationLoader.visibility=View.VISIBLE
            }else{
               binding.login.text="Login"
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

