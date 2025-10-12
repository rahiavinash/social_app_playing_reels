package com.wedo8020.app.mainui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wedo8020.app.R
import com.wedo8020.app.mainui.base.BaseFragment


class MessagingFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        showNavBottom()
        return inflater.inflate(R.layout.fragment_messaging, container, false)
    }

}