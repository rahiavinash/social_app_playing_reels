package com.wedo8020.app.mainui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import com.wedo8020.app.databinding.ActivityOnBoardingVideoBinding
import com.wedo8020.app.mainui.base.BaseActivity

class OnBoardingVideoActivity : BaseActivity() {

    private lateinit var binding: ActivityOnBoardingVideoBinding
    private var videoUrl : String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustFontScale(resources.configuration,1.0f)
        setStatusBarTransparent()

        // Get the Bitmap of the bottomNavigation LinearLayout



        videoUrl = intent.getStringExtra("videoUrl")

        binding.bottomNavigation.post {

            val width = binding.bottomNavigation.width
            val height = binding.bottomNavigation.height


            if (width > 0 && height > 0) {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val blur = RenderEffect.createBlurEffect(
                        15f, 15f, Shader.TileMode.CLAMP
                    )
                    binding.bottomNavigation.setRenderEffect(blur)
                } else {

                    val bottomNavigationBitmap =
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bottomNavigationBitmap)
                    binding.bottomNavigation.draw(canvas)


                    val blurredBitmap = blurBitmap(
                        this,
                        bottomNavigationBitmap,
                        25f
                    ) // Adjust blur radius as needed

                    binding.bottomNavigation.background = BitmapDrawable(resources, blurredBitmap)

                }

            } else {
                // Handle the case where dimensions are invalid
                Log.e("Error", "Invalid dimensions for creating bitmap")
            }
        }

        binding.video.setOnPreparedListener {
            it.start()
        }
        if(videoUrl !=null){
            Log.e("PreviewFragment", " video URI: $videoUrl")
            binding.video.setVideoURI(videoUrl?.toUri())
            binding.video.start()
        }
        else {
            val intent = Intent(this, MainActivity::class.java)
            Log.e("PreviewFragmentCat", " video URI: $videoUrl")
            startActivity(intent)
            finish()
        }

        binding.video.setOnCompletionListener { mediaPlayer ->

            val intent = Intent(this, MainActivity::class.java)
            Log.e("PreviewFragmentCat", " video URI: $videoUrl")
            startActivity(intent)
            finish()
        }


    }



    // Function to blur a bitmap
    fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        // Create a blurred Bitmap using RenderScript
        val inputBitmap = Bitmap.createBitmap(bitmap)
        val outputBitmap = Bitmap.createBitmap(inputBitmap.width, inputBitmap.height, inputBitmap.config)

        val rs = RenderScript.create(context)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

        val inputAllocation = Allocation.createFromBitmap(rs, inputBitmap)
        val outputAllocation = Allocation.createFromBitmap(rs, outputBitmap)

        script.setRadius(radius)
        script.setInput(inputAllocation)
        script.forEach(outputAllocation)
        outputAllocation.copyTo(outputBitmap)

        return outputBitmap
    }





}