package com.wedo8020.app.mainui.activities

import android.os.Bundle
import com.wedo8020.app.R
import com.wedo8020.app.mainui.base.BaseActivity

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setStatusBarTransparent()
    }



}