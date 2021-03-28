/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.features.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.OnboardingSetupBinding
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.authenticatorApp
import kotlinx.android.synthetic.main.activity_onboarding.*
import javax.inject.Inject

class OnboardingFragment : Fragment(),
    ViewPager.OnPageChangeListener,
    View.OnClickListener
{
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    lateinit var viewModel: OnboardingSetupViewModel
    private lateinit var binding: OnboardingSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.activity_onboarding,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        viewModel.onOnboardingPageSelected(position)
    }

    override fun onClick(v: View?) {
        viewModel.onViewClick(v?.id ?: return)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(OnboardingSetupViewModel::class.java)
        lifecycle.addObserver(viewModel)

        viewModel.pageIndicator.observe(this, Observer<Int> { position ->
            pageIndicatorView?.selection = position
        })
        viewModel.moveNext.observe(this, Observer<ViewModelEvent<Unit>> {
            onboardingPager.currentItem = onboardingPager.currentItem + 1
        })
        viewModel.showPasscodeSetup.observe(this, Observer<ViewModelEvent<Unit>> {
            findNavController().navigate(R.id.passcodeSetupFragment)
        })
    }

    private fun setupViews() {
        onboardingPager?.clearOnPageChangeListeners()
        onboardingPager?.addOnPageChangeListener(this)
        viewModel.onboardingViewModels.let {
            val context = activity?.applicationContext ?: return
            onboardingPager?.adapter = OnboardingPagerAdapter(context, it)
            onboardingPager?.currentItem = 0
            pageIndicatorView?.setCount(it.size)
            pageIndicatorView?.selection = 0
        }
        skipActionView?.setOnClickListener(this)
        proceedToSetup?.setOnClickListener(this)
        nextActionView?.setOnClickListener(this)
    }
}
