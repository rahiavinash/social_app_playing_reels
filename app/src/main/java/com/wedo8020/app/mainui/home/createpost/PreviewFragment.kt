package com.wedo8020.app.mainui.home.createpost

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentPreviewBinding
import com.wedo8020.app.mainui.activities.SplashActivity
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.viewPagerUploadImage.ImageSlider
import java.lang.Math.min

class PreviewFragment : BaseFragment() {
    private lateinit var mBinding : FragmentPreviewBinding
    private var receivedUri: ArrayList<Uri>? = arrayListOf()
    private var compressedImagesUri: ArrayList<Uri>? = arrayListOf()
    private var  videoUri : Uri? = Uri.EMPTY
    private var  mediaType : String = ""
    private lateinit var imageView : ViewPager
    lateinit var imagePager: ImageSlider


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_preview, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = mBinding.imageViewPager
        val previousButton = mBinding.previousButtonSlider
        val nextButton = mBinding.nextButtonSlider




        arguments?.let {
            receivedUri =
                it.getParcelableArrayList("compressedImages")

            val retrieveBase64Video1: String? = it.getString("video")
            if (retrieveBase64Video1 != null) {
                videoUri = retrieveBase64Video1.toUri()
                Log.d("videoUriCompressedVideo", videoUri.toString())

            }
            val mediaTypeOne : String? = it.getString("mediaType")
            if (mediaTypeOne != null) {
                mediaType = mediaTypeOne

            }
        }
        Log.d("ImagesCompressedxxxxxxxx", receivedUri.toString())
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            receivedUri?.clear()
            findNavController().navigateUp()
        }

        mBinding.video.setOnPreparedListener {
            it.start()
        }
        mBinding.nextButton.setOnClickListener {
            bundle.putParcelable("video", videoUri)
            findNavController().navigate(R.id.thumbNailFragment, bundle)
        }
        mBinding.doneButton.setOnClickListener {
            checkIfFragmentAttached {
                if (isInternetAvailable(requireContext())) {
                    Log.d("hahahanothappening", "images are getting upload")
//                lifecycleScope.launch{
//                    coroutineScope {
//                        val deferredOne = async {
//                            ImageCompressionUtil.compressImages(requireContext(), receivedUri)
//                        }.await()
//                        Log.d("hahahanothappening", "${deferredOne}")
//                        receivedUri = deferredOne
                    bundle.putParcelableArrayList("images", receivedUri)

                    bundle.putString("mediaType", mediaType)
                    findNavController().navigate(R.id.createPostFragment, bundle)

                }
            }






        }


        if(mediaType == "image"){

            mBinding.sliderDots.visibility = View.VISIBLE
            mBinding.nextButton.visibility = View.INVISIBLE
            mBinding.doneButton.visibility = View.VISIBLE
            mBinding.imageView1.visibility = View.INVISIBLE
            mBinding.video.visibility = View.GONE
            mBinding.imageViewPager.visibility = View.VISIBLE
            mBinding.previousButtonSlider.visibility = View.VISIBLE
            mBinding.nextButtonSlider.visibility = View.VISIBLE


            receivedUri?.let {

                    imagePager = ImageSlider(requireContext(), it)
                    imageView.adapter = imagePager
                    imagePager.notifyDataSetChanged()
                    imagePager.updateDataSet(it)
                    createDotSlider(requireContext(), it.size)
            }

//            val adapter = ImageSlide(requireContext(), receivedUri)
//            Log.d("adapter", adapter.count.toString())
//            imageView.adapter = adapter

            if (imageView.currentItem == 0) {
                previousButton.visibility = View.INVISIBLE
            } else {
                previousButton.visibility = View.VISIBLE
            }

            val adapterCount = imageView.adapter?.count ?: 0
            if (adapterCount > 0 && imageView.currentItem == adapterCount - 1) {
                nextButton.visibility = View.INVISIBLE
            } else {
                nextButton.visibility = View.VISIBLE
            }

            if((receivedUri?.size ?: "noImage") == 1){
                mBinding.sliderDots.visibility = View.INVISIBLE

            }
        }

        else if(mediaType == "video") {
            Log.d("video111", "You are selecting video")
            imageView.visibility = View.GONE
            mBinding.nextButton.visibility = View.VISIBLE
            mBinding.doneButton.visibility = View.INVISIBLE
            mBinding.imageViewPager.visibility = View.INVISIBLE
            mBinding.imageView1.visibility = View.INVISIBLE
            mBinding.video.visibility = View.VISIBLE
            mBinding.previousButtonSlider.visibility = View.INVISIBLE
            mBinding.nextButtonSlider.visibility = View.INVISIBLE


            try {
                mBinding.video.setVideoURI(videoUri)
                mBinding.video.start()
            } catch (e: Exception) {
                Log.e("PreviewFragment", "Error setting video URI: ${e.message}")
            }

        }

        mBinding.backButton.setOnClickListener {
            receivedUri?.clear()
            findNavController().navigateUp()
        }

        mBinding.nextButtonSlider.setOnClickListener {

            val adapter = imageView.adapter
            if (adapter != null && adapter.count > 0) {
                imageView.currentItem = (imageView.currentItem + 1) % adapter.count
            } else {

                Log.e("UploadFragment", "Adapter is null or has a count of 0")
            }

        }
        mBinding.previousButtonSlider.setOnClickListener {
            if (imageView.adapter != null && imageView.currentItem != 0) {
                imageView.currentItem = imageView.currentItem - 1
            }
        }



        imageView.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {

                    previousButton.visibility = View.INVISIBLE
                } else {
                    previousButton.visibility = View.VISIBLE
                }

                val adapter = imageView.adapter
                if (adapter != null && position == adapter.count - 1) {
                    nextButton.visibility = View.INVISIBLE
                } else {
                    nextButton.visibility = View.VISIBLE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

    }

    override fun onResume() {
        super.onResume()

        if(!isGalleryPermissionGranted(requireContext())){
            navigateToSplashScreen()
        }
    }
    private fun isGalleryPermissionGranted(context: Context): Boolean {
        val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE


        if(ContextCompat.checkSelfPermission(context, readImagePermission) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun navigateToSplashScreen() {

        val intent = Intent(requireContext(), SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }



    private fun createDotSlider(context: Context, imageCount: Int) {
        mBinding.sliderDots.removeAllViews()
        val dotCount = min(imageCount, 10) // Ensure maximum of 10 dots
        val dots = arrayOfNulls<ImageView>(10) // Initialize array with length 10
        for (i in 0 until dotCount) {
            dots[i] = ImageView(context)
            dots[i]?.setImageDrawable(
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
            mBinding.sliderDots.addView(dots[i], params)
        }
        dots[0]?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot))
        imageView.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until dotCount) {
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



    override fun onStop() {
        super.onStop()

    }


    override fun onDestroyView() {
        super.onDestroyView()

    }





    companion object {
        var bundle  = Bundle()
    }


}