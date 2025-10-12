package com.wedo8020.app.mainui.login

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentSelfCertificationBinding

class SelfCertificationFragment : Fragment() {
    private lateinit var binding : FragmentSelfCertificationBinding
    private val viewModel by viewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentSelfCertificationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.signUpButton.isEnabled = false

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        val fullText = binding.termsConditions.text

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

        spannableString.setSpan(termsClickableSpan, binding.termsConditions.text.indexOf("Terms of Use"), fullText.indexOf("Terms of Use") + "Terms of Use".length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), binding.termsConditions.text.indexOf("Terms of Use"), fullText.indexOf("Terms of Use") + "Terms of Use".length, 0)

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

        spannableString.setSpan(privacyClickableSpan, binding.termsConditions.text.indexOf("Privacy Policy"), fullText.indexOf("Privacy Policy") + "Privacy Policy".length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), binding.termsConditions.text.indexOf("Privacy Policy"), fullText.indexOf("Privacy Policy") + "Privacy Policy".length, 0)

        binding.termsConditions.text = spannableString

        binding.termsConditions.movementMethod = LinkMovementMethod.getInstance()
        binding.termsConditions.highlightColor = Color.TRANSPARENT // This prevents the TextView from highlighting when clicked
        binding.termsConditions.text = spannableString
        binding.termsConditions.movementMethod = LinkMovementMethod.getInstance()



        val fullTextTerms = binding.termsConditionsValidate.text

        val spannableStringOne = SpannableString(fullTextTerms)

        val termsClickableSpanOne = object : ClickableSpan() {
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

        spannableStringOne.setSpan(termsClickableSpanOne, binding.termsConditionsValidate.text.indexOf("Terms of Use"),  fullTextTerms.indexOf("Terms of Use") + "Terms of Use".length, 0)
        spannableStringOne.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), binding.termsConditionsValidate.text.indexOf("Terms of Use"),  fullTextTerms.indexOf("Terms of Use") + "Terms of Use".length, 0)

        val privacyClickableSpanOne = object : ClickableSpan() {
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

        spannableStringOne.setSpan(privacyClickableSpanOne, binding.termsConditionsValidate.text.indexOf("Privacy Policy"),  fullTextTerms.indexOf("Privacy Policy") + "Privacy Policy".length, 0)
        spannableStringOne.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), binding.termsConditionsValidate.text.indexOf("Privacy Policy"),  fullTextTerms.indexOf("Privacy Policy") + "Privacy Policy".length, 0)

        binding.termsConditionsValidate.text = spannableStringOne

        binding.termsConditionsValidate.movementMethod = LinkMovementMethod.getInstance()
        binding.termsConditionsValidate.highlightColor = Color.TRANSPARENT // This prevents the TextView from highlighting when clicked
        binding.termsConditionsValidate.text = spannableStringOne
        binding.termsConditionsValidate.movementMethod = LinkMovementMethod.getInstance()

        if (viewModel.isCheckBoxChecked) {
            binding.checked.visibility = View.VISIBLE
            binding.signUpButton.isEnabled = true
            binding.signUpButton.setBackgroundResource(R.drawable.blue_button_background_two)
            binding.signUpButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        binding.unchecked.setOnClickListener {

            viewModel.isCheckBoxChecked = true

            if (binding.checked.visibility == View.VISIBLE) {
                binding.checked.visibility = View.GONE
                binding.signUpButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.Grey))
                binding.signUpButton.setBackgroundResource(R.drawable.white_button_background_two)
                binding.signUpButton.isEnabled = false
            } else {
                binding.checked.visibility = View.VISIBLE
                binding.signUpButton.isEnabled = true
                binding.signUpButton.setBackgroundResource(R.drawable.blue_button_background_two)
                binding.signUpButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }


        binding.signUpButton.setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
        }
    }


}