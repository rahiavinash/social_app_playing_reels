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
import androidx.navigation.fragment.findNavController
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentCreateAccountBinding
import com.wedo8020.app.mainui.login.switchaccounts.roomdB.SwitchAccountsEntity


class CreateAccountFragment : Fragment() {

    private lateinit var binding : FragmentCreateAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentCreateAccountBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.backButton.setOnClickListener {
            findNavController().popBackStack(R.id.loginSignUpFragment, false)


        }

        val fullText = binding.beforeText.text

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

        spannableString.setSpan(termsClickableSpan, binding.beforeText.text.indexOf("Terms of Use"), fullText.indexOf("Terms of Use") + "Terms of Use".length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), binding.beforeText.text.indexOf("Terms of Use"), fullText.indexOf("Terms of Use") + "Terms of Use".length, 0)

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

        spannableString.setSpan(privacyClickableSpan, binding.beforeText.text.indexOf("Privacy Policy"), fullText.indexOf("Privacy Policy") + "Privacy Policy".length, 0)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#2E94D2")), binding.beforeText.text.indexOf("Privacy Policy"), fullText.indexOf("Privacy Policy") + "Privacy Policy".length, 0)

        binding.beforeText.text = spannableString

        binding.beforeText.movementMethod = LinkMovementMethod.getInstance()
        binding.beforeText.highlightColor = Color.TRANSPARENT // This prevents the TextView from highlighting when clicked
        binding.beforeText.text = spannableString
        binding.beforeText.movementMethod = LinkMovementMethod.getInstance()

        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.selfCertificationFragment)
        }
    }






}