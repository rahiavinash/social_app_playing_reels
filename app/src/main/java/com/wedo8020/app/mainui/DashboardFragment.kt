package com.wedo8020.app.mainui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.wedo8020.app.R
import com.wedo8020.app.databinding.FragmentDashboardBinding
import com.wedo8020.app.mainui.base.BaseFragment
import com.wedo8020.app.mainui.home.postFragment.PostViewModel


open class DashboardFragment : BaseFragment() {
    private lateinit var navController:NavController
    private val authViewModel by viewModels<PostViewModel>()

    companion object{
        lateinit var binding: FragmentDashboardBinding


       /* fun hideNavigation(isVisiblity:Boolean){
            if (isVisiblity){
                Log.e("2324567675432",isVisiblity.toString())
                binding.bottomNavigation.visibility=View.VISIBLE
            }else{
                Log.e("2324567675432aaaa",isVisiblity.toString())
                binding.bottomNavigation.visibility=View.GONE
            }

        }*/
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentDashboardBinding.inflate(inflater,container,false)



        setUpUI()



        return binding.root
    }

    private fun setUpUI(){
        binding.apply {
            var navHostFragment =
                childFragmentManager.findFragmentById(R.id.homeHostFragment) as NavHostFragment
            navController = navHostFragment.navController
        }

     //   setUpBottomNavigation()

    }

/*

    private fun setUpBottomNavigation(){
        binding.apply {
            homeTab.setOnClickListener {
                homeTabClick()
            }
            xChangeTab.setOnClickListener {
                xChangeTabClick()
            }
            uploadTab.setOnClickListener {
                uploadTabClick()
            }
            messagingTab.setOnClickListener {
                messagingTabClick()
            }
            profileTab.setOnClickListener {
                profileTabClick()
            }
        }
    }

    private fun homeTabClick(){
        binding.apply {
            homeTitle.visibility = View.VISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.pureWhite))
            xChangeTitle.visibility=View.INVISIBLE
            uploadImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            uploadTitle.visibility = View.INVISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            messagineTitle.visibility = View.INVISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            profileTitle.visibility = View.INVISIBLE
            navController.navigate(R.id.homeFragment)
        }
    }
    private fun xChangeTabClick(){
        binding.apply {
            homeTitle.visibility = View.INVISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            xChangeTitle.visibility=View.VISIBLE
            uploadImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            uploadTitle.visibility = View.INVISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            messagineTitle.visibility = View.INVISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            profileTitle.visibility = View.INVISIBLE
            navController.navigate(R.id.xchangeFragment)
        }
    }
    private fun uploadTabClick(){
        binding.apply {
            homeTitle.visibility = View.INVISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            xChangeTitle.visibility=View.INVISIBLE
            uploadImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.pureWhite))
            uploadTitle.visibility = View.VISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            messagineTitle.visibility = View.INVISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            profileTitle.visibility = View.INVISIBLE
            navController.navigate(R.id.uploadFragment)
        }
    }
    private fun messagingTabClick(){
        binding.apply {
            homeTitle.visibility = View.INVISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            xChangeTitle.visibility=View.INVISIBLE
            uploadImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            uploadTitle.visibility = View.INVISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.pureWhite))
            messagineTitle.visibility = View.VISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            profileTitle.visibility = View.INVISIBLE
            navController.navigate(R.id.messagingFragment)
        }
    }
    private fun profileTabClick(){
        binding.apply {
            homeTitle.visibility = View.INVISIBLE
            homeImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            xChangeTitle.visibility=View.INVISIBLE
            uploadImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            uploadTitle.visibility = View.INVISIBLE
            messagingImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.unselectedColor))
            messagineTitle.visibility = View.INVISIBLE
            profileImg.imageTintList = ColorStateList.valueOf(requireActivity().getColor(R.color.pureWhite))
            profileTitle.visibility = View.VISIBLE
            navController.navigate(R.id.profileFragment)
        }


    }
*/


}