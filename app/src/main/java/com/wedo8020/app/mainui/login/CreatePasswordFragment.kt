package com.wedo8020.app.mainui.login

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentCreatePasswordBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountsViewModel
import com.wedo8020.app.mainui.login.switchaccounts.SwitchAccountsViewModelFactory
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsDbCreation
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsEntity
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsRepo
import com.wedo8020.app.response.RegisterUserRequest
import com.wedo8020.app.utils.AppConstant
import com.wedo8020.app.utils.Prefs
import com.wedo8020.app.utils.Utils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects


class CreatePasswordFragment : BaseFragment() {
    private lateinit var binding : FragmentCreatePasswordBinding
    private var userName : String ?=null
    private var accountName : String?= null
    private val viewModel by viewModels<LoginViewModel>()
    private var password : String?=null
    private lateinit var viewModels : SwitchAccountsViewModel
    private lateinit var database : SwitchAccountsDbCreation
    private lateinit var repository : SwitchAccountsRepo
    private var dialog: Dialog?=null
    private var countDatabase : Int ?= null
    private var checkIfUserExist : Int ?=null
    private lateinit var factory : SwitchAccountsViewModelFactory
    private var databaseData : List<SwitchAccountsEntity> = listOf()
    private var referralId : String?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentCreatePasswordBinding.inflate(inflater,container,false)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        if (!requireActivity().intent.getStringExtra(AppConstant.CANONICAL_IDENTIFIER).isNullOrEmpty()){
            referralId= requireActivity().intent.getStringExtra(AppConstant.CANONICAL_IDENTIFIER).toString()
        }
        Log.e("referralIdDeviceGot" , referralId.toString())
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("HardwareIds", "SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.passwordField.showSoftKeyboard()
        hideKeyboardOnOutSideClick(binding.root)

        arguments?.let {
            userName = it.getString("userName")
            accountName= it.getString("accountName")
        }

        val filter = InputFilter { source, _, _, dest, _, _ ->
            if (source != null && source.contains(" ")) {
                ""
            } else null
        }
        binding.passwordField.filters = arrayOf(filter)
        binding.reEnterPassword.filters = arrayOf(filter)
        binding.createButton.isEnabled = false

        val androidId = Settings.Secure.getString(
            requireActivity().contentResolver,
            Settings.Secure.ANDROID_ID
        )
        Log.d("deviceId", "Device id is $androidId")
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
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

                else if(s?.isEmpty() == true) {
                    Log.d("PasswordEmpty", "Password is empty")
                    binding.passwordValidation.text = "Enter Password"
                    binding.passwordValidation.setTextColor(Color.parseColor("#FF0340"))
                }
                else {
                    password = s.toString()
                    validatePasswordAndMatch(s.toString())
                    updateReEnterPasswordValidation()
                }

            }
        })





        binding.createButton.setOnClickListener {
            if(Utils.isInternetAvailables(requireContext())){
                showProgressDialog()
                if(!password.isNullOrEmpty()){
                    viewModel.registerUser(RegisterUserRequest(name = accountName, username = userName, password = password,
                        deviceType = "android", deviceToken = androidId.toString(), referralId = if(referralId.isNullOrEmpty()){
                            null
                        } else {
                            referralId
                        }))
                }
                viewModel.registerUser.observe(viewLifecycleOwner, Observer {
                        response ->
                    if(response.success == true){
                        dialog?.dismiss()

                        val recoveryCode = response.data?.recoveryCode.toString()
                        Log.d("recoveryCode", "Code $recoveryCode")

                        Prefs.init().accessToken= response.data?.accessToken.toString()
                        Prefs.init().refreshToken = response.data?.refreshToken.toString()
                        Prefs.init().isLogIn = true



                                Log.d("databaseCount", "$countDatabase rows of database")
                                if(countDatabase==2){
                                    Log.d("databaseCount", "$countDatabase rows of database $countDatabase")
                                    viewModels.updateSecondRow(SwitchAccountsEntity(username = userName, password = password,
                                        deviceId = androidId, profile = null, isLogin = true,loginTimestamp = getCurrentDateTime()))
                                }
                                else if(countDatabase==1){
                                    Log.d("databaseCount", "$countDatabase rows of database $countDatabase")
                                    viewModels.addAccount(SwitchAccountsEntity(username = userName, password = password,
                                        deviceId = androidId, profile= null, isLogin = true, loginTimestamp = getCurrentDateTime()))
                                }
                                else if(countDatabase==0){
                                    Log.d("databaseCount", "$countDatabase rows of database $countDatabase")
                                    viewModels.addAccount(SwitchAccountsEntity(username = userName, password = password,
                                        deviceId = androidId, profile= null, isLogin = true, loginTimestamp = getCurrentDateTime()))
                                }



                        Log.d("accessToken", "Sent token : ${response.data?.accessToken}")
                        bundle.putString("recoveryCode",recoveryCode)
                        findNavController().navigate(R.id.recoveryCodeFragment, bundle)
                        Log.d("successMessageRegister", "API Hit success$ ${response.success}  ${response.success}")
                    }
                    else {
                        dialog?.dismiss()
                        Utils.showToast(response.message.toString())
                    }
                })
            }
        }


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

                }
                else {
                    validateReEnterPasswordAndMatch(s.toString())
                    updateReEnterPasswordValidation()


                }







            }
        })


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




    }

    private fun validatePasswordAndMatch(password: String) {

        if (password.length < 4) {
            binding.passwordValidation.visibility = View.VISIBLE
            binding.passwordValidation.setTextColor(Color.parseColor("#FF0340"))
            binding.passwordValidation.text="Minimum length 4"
        }
        if (password.length >= 4) {
            binding.passwordValidation.visibility = View.INVISIBLE

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDateTime(): String {

        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentTime.format(formatter)
    }




    private fun updateReEnterPasswordValidation() {
        val passwordField: String = binding.passwordField.text.toString().trim()
        val reEnterPasswordField: String = binding.reEnterPassword.text.toString().trim()

        if (passwordField.isNotEmpty() && reEnterPasswordField.isNotEmpty() && passwordField.length >= 4) {
            Log.d("passwordValidation", "Both are not empty and password length is at least 4")
            if (passwordField != reEnterPasswordField) {
                binding.createButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                binding.createButton.setBackgroundResource(R.drawable.white_button_background_two)
                binding.createButton.isEnabled = false
                binding.reEnterPasswordValidation.visibility = View.VISIBLE
                binding.reEnterPasswordValidation.setTextColor(Color.parseColor("#FF0340"))
                binding.reEnterPasswordValidation.text = "Incorrect Password"
            } else {
                binding.reEnterPasswordValidation.visibility = View.GONE
                binding.createButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.createButton.setBackgroundResource(R.drawable.blue_button_background_two)
                binding.createButton.isEnabled = true
            }
        } else {

            if(reEnterPasswordField.isEmpty()){
                Log.d("reEnterPassword", "I am invoked")
                binding.reEnterPasswordValidation.text = "Re-enter Password"
            }
            else {
                binding.reEnterPasswordValidation.visibility = View.GONE
                binding.createButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                binding.createButton.setBackgroundResource(R.drawable.white_button_background_two)
                binding.createButton.isEnabled = false
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

    private fun validateReEnterPasswordAndMatch(password: String) {

        if (password.length < 4) {
            binding.reEnterPasswordValidation.visibility = View.VISIBLE
            binding.reEnterPasswordValidation.setTextColor(Color.parseColor("#FF0340"))
            binding.reEnterPasswordValidation.text="Minimum length 4"
        }
        if (password.length >= 4) {
            binding.reEnterPasswordValidation.visibility = View.GONE

        }
    }

    companion object{
        val bundle = Bundle()
    }


}