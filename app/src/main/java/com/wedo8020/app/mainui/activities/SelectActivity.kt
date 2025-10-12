package com.wedo8020.app.mainui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wedo8020.app.databinding.ActivitySelectBinding

class SelectActivity : AppCompatActivity() {
    private lateinit var b: ActivitySelectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.selectToolbar)

    }
}
