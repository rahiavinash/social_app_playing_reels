package com.wedo8020.app.mainui.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.wedo8020.app.R


class CustomSeekBar(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {
    private val seekBar: SeekBar
    private val currentTimeTextView: TextView
    private val totalTimeTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_post_seek_bar, this)
        seekBar = findViewById<SeekBar>(R.id.seekBar)
        currentTimeTextView = findViewById<TextView>(R.id.currentTimeTextView)
        totalTimeTextView = findViewById<TextView>(R.id.totalTimeTextView)

        // Initialize your SeekBar and set listeners here if needed
    } // Add methods to update progress, timestamps, and handle user interaction as needed
}
