package com.wedo8020.app.mutlipleimage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.wedo8020.app.R
import com.wedo8020.app.databinding.DialogCameraBinding
import com.wedo8020.app.mainui.home.createpost.CreatePostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog


class MultipleActivity : AppCompatActivity() {
    var select: ConstraintLayout? = null
    var previous:ImageView? = null
    var next:ImageView? = null
    var imageView: ViewPager? = null
    var cons: ConstraintLayout? = null
    var consi: ConstraintLayout? = null
    var PICK_IMAGE_MULTIPLE = 100
    var imageEncoded: String? = null
    var total: TextView? = null
    var allowPandVAccess: TextView? = null
    var nextBT: TextView? = null
    var doneBT: TextView? = null
    var settingTV: TextView? = null
    var mArrayUri: ArrayList<Uri>? = null
    var sliderDots:LinearLayout? = null
    var position = 0
    var imagesEncodedList: List<String>? = null
    var headerCons: ConstraintLayout? = null
    var headerCons1: ConstraintLayout? = null
    var oneCL: ConstraintLayout? = null

    lateinit var imagePager:ImageSlide
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiple)
        select = findViewById(R.id.uploadPandVCL)
//        total = findViewById(R.id.text)
        imageView = findViewById(R.id.imagePager)
        previous = findViewById(R.id.previousBT)
        sliderDots = findViewById(R.id.sliderDots)
        allowPandVAccess = findViewById(R.id.allowPandVAccess)
        settingTV = findViewById(R.id.settingTV)
        headerCons = findViewById(R.id.hedaerUpCL1)
        headerCons1 = findViewById(R.id.hedaerUpCL)
        oneCL = findViewById(R.id.oneCL)
        mArrayUri = ArrayList()

        // showing all images in imageswitcher

        // showing all images in imageswitcher
        next = findViewById(R.id.nextVBT)
        nextBT = findViewById(R.id.nextBT)
        doneBT = findViewById(R.id.doneBT)
        cons = findViewById(R.id.uploadCL)

        // click here to select next image

        // click here to select next image
        next!!.setOnClickListener {
            if (imageView?.adapter != null && imageView?.currentItem != (imageView?.adapter?.count!! - 1)) {
                imageView?.currentItem = imageView?.currentItem!! + 1

            }


//            doneBT?.visibility = View.VISIBLE
//            nextBT?.visibility = View.GONE
//            position = imageView?.currentItem!!
//            position++
//            imageView?.currentItem = position
//            doneBT?.visibility = View.VISIBLE
//            nextBT?.visibility = View.GONE
//            if (position < mArrayUri!!.size - 1) {
//                // increase the position by 1
//
//                position++
//                imageView!!.currentItem = position
//            } else {
//                Toast.makeText(this@MainActivity, "Last Image Already Shown", Toast.LENGTH_SHORT)
//                    .show()
//            }
        }

        doneBT!!.setOnClickListener {
          //  findNavController().navigate(R.id.editPostFragment)
            val intent = Intent(this, CreatePostFragment::class.java)
            intent.putExtra("resId",mArrayUri)
            startActivity(intent)
        }

        nextBT!!.setOnClickListener(View.OnClickListener {

            position = imageView?.currentItem!!
            position++
            imageView?.currentItem = position
//            doneBT?.visibility = View.VISIBLE
//            nextBT?.visibility = View.GONE

//            if (position < mArrayUri!!.size - 1) {
//                // increase the position by 1
//
//                position++
//                imageView!!.currentItem = position
//            } else {
//                Toast.makeText(this@MainActivity, "Last Image Already Shown", Toast.LENGTH_SHORT)
//                    .show()
//            }
        })



        // click here to view previous image

        // click here to view previous image
        previous!!.setOnClickListener(View.OnClickListener {

            if (imageView?.adapter != null && imageView?.currentItem != 0) {
                imageView?.currentItem = imageView?.currentItem!! - 1
            }
//            if (position > 0) {
//                // decrease the position by 1
//                position--
//                imageView!!.currentItem = position
////                doneBT?.visibility = View.VISIBLE
////                nextBT?.visibility = View.GONE
//            }
        })

        imageView!!.setOnTouchListener(object : OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                return true
            }
        })

        // click here to select image

        // click here to select image
        select!!.setOnClickListener(View.OnClickListener { // initialising intent
            showCameraDialog()
        })
    }


    private fun showCameraDialog() {

        val dialog = BottomSheetDialog(this)
        val dialogCameraBinding = DialogCameraBinding.inflate(layoutInflater)
        val viewBottom = dialogCameraBinding.root



        dialogCameraBinding.cameraCV.setOnClickListener {
            dialog.dismiss()
            openCamera()
        }
        dialogCameraBinding.galleryCV.setOnClickListener {
            dialog.dismiss()
            openGallery()
        }

        dialogCameraBinding.cancelBtn.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
//
        dialog.setCancelable(false)
        dialog.setContentView(viewBottom)
        dialog.show()
    }

    private fun openCamera() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, 200)
    }

    private fun openGallery() {
        val intent = Intent()

        // setting type to select to be image
        intent.type = "image/*"

        // allowing multiple image to be selected
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_MULTIPLE
        )

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // When an Image is picked
        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
            // Get the Image from data
            position = if (data.clipData != null) {
                mArrayUri!!.clear()
                val mClipData = data.clipData
                val cout = data.clipData!!.itemCount
                if (cout>10){
                    Toast.makeText(this, "You can select a maximum of 10 images", Toast.LENGTH_LONG).show()
                }else
                {
                    for (i in 0 until cout) {
                        // adding imageuri in array
                        val imageurl = data.clipData!!.getItemAt(i).uri
                        mArrayUri!!.add(imageurl)
                    }
                }

                // setting 1st selected image into image switcher
                0
            }
            else {
                mArrayUri!!.clear()
                val imageurl = data.data
                mArrayUri!!.add(imageurl!!)
                0
            }
            createDotSlider(mArrayUri!!.size,this)
            imagePager =ImageSlide(this,mArrayUri)
            imageView!!.adapter = imagePager

            headerCons1?.visibility = View.GONE
            oneCL?.visibility = View.GONE
            select?.visibility = View.GONE
            allowPandVAccess?.visibility = View.GONE
            settingTV?.visibility = View.GONE

            imageView?.visibility = View.VISIBLE
            sliderDots?.visibility = View.VISIBLE
            previous?.visibility = View.VISIBLE
            next?.visibility = View.VISIBLE
            headerCons?.visibility = View.VISIBLE




        } else {
            // show this if no image is selected
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }
    private fun createDotSlider(imageCount:Int,context: Context){

        sliderDots?.removeAllViews()
        val dots = arrayOfNulls<ImageView>(imageCount)
        for (i in 0 until imageCount) {
            dots[i] = ImageView(context)
            dots[i]!!.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.inactive_dot
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            sliderDots?.addView(dots[i], params)
        }
        dots[0]!!.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot));
        imageView?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (position == imageView?.adapter?.count?.minus(1)) {
                    doneBT?.visibility = View.VISIBLE
                nextBT?.visibility = View.GONE
                }else{
                    doneBT?.visibility = View.GONE
                    nextBT?.visibility = View.VISIBLE
                }
                for (i in 0 until imageCount) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.inactive_dot
                        )
                    )

                }

                dots[position]?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.active_dot
                    )
                )

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }
}