package com.wedo8020.app.mainui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import com.wedo8020.app.databinding.DateLayoutBinding
import com.wedo8020.app.databinding.FragmentShowProfilePostBinding
import com.wedo8020.app.mainui.home.postFragment.PostViewModel
import com.wedo8020.app.utils.CachedDataSourceFactory
import com.wedo8020.app.utils.Prefs

abstract class BasePostFragment<T : ViewDataBinding> : BaseFragment() {
    var cachedDataSourceFactory: CachedDataSourceFactory? = null
    val mViewModel by lazy { viewModels<PostViewModel>().value }
    var isGhostMode = Prefs.init().isGhostMode
    var isLHPost = Prefs.init().isLHPost
    abstract fun onSetUpUi(binding: T)

}