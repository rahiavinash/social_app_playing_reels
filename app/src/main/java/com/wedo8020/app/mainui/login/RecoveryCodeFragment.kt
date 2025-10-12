package com.wedo8020.app.mainui.login

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentRecoveryCodeBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.utils.Prefs

class RecoveryCodeFragment : BaseFragment() {
    private lateinit var binding :FragmentRecoveryCodeBinding
    private var recoveryCode : String?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =  FragmentRecoveryCodeBinding.inflate(inflater,container,false)

        Log.e("acccesTokaen", Prefs.init().accessToken)
        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            recoveryCode = it.getString("recoveryCode")
        }
        hideKeyboardOnOutSideClick(binding.root)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

        }
        val fullText = binding.notRecoveredNameText.text
        val spannableString = SpannableString(fullText)

        val startIndex = fullText.indexOf("CAN NOT")
        val endIndex = startIndex + "CAN NOT".length

        // Set color to red for "CAN NOT"
        spannableString.setSpan(
            ForegroundColorSpan(Color.RED),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.doneButton.isEnabled = false
        binding.doneButton.setOnClickListener {
                findNavController().navigate(R.id.interestFragment)
        }
        binding.notRecoveredNameText.text = spannableString
        binding.recoveryCode.text = recoveryCode

        binding.copyCode.setOnClickListener {
           // copyTextToClipboard(binding.recoveryCode.text.toString())
            openTextApps()
            checkIfFragmentAttached {

                binding.doneButton.isEnabled = true
                binding.doneButton.setBackgroundDrawable(requireContext().getDrawable(R.drawable.blue_button_background_two))
                binding.doneButton.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }

        }

    }

    private fun copyTextToClipboard(text: String) {
        val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", text)
        clipboardManager.setPrimaryClip(clipData)

        showToasts("Copied to Clipboard")
    }
    private fun openTextApps() {

        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Your Authorization Code : \n ${binding.recoveryCode.text}")

        val resolveInfoList: List<ResolveInfo> =
            requireContext().packageManager.queryIntentActivities(sendIntent, 0)

        if (resolveInfoList.isNotEmpty()) {
            val chooserIntent = Intent.createChooser(sendIntent, "Open with")
            chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(chooserIntent)
        } else {
            // Handle the case where no app can handle the intent
            Toast.makeText(requireContext(), "No compatible app found", Toast.LENGTH_SHORT).show()
        }

    }
    private fun showToasts(message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_layout, requireView().findViewById(R.id.toast_layout))

        val text = layout.findViewById<TextView>(R.id.toast_text)
        text.text = message

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
//        toast.setGravity(Gravity.TOP,0,100)
        toast.show()
    }


}