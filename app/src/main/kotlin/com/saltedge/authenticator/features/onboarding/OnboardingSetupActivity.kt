/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.databinding.ActivityOnboardingBinding
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.showWarningDialog
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode
import com.saltedge.authenticator.widget.security.KEY_SKIP_PIN
import timber.log.Timber
import javax.inject.Inject

class OnboardingSetupActivity : AppCompatActivity(),
    ViewPager.OnPageChangeListener,
    View.OnClickListener
{
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    lateinit var viewModel: OnboardingSetupViewModel
    private var binding: ActivityOnboardingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupViewModel()
        initViews()
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        viewModel.onOnboardingPageSelected(position)
    }

    override fun onClick(v: View?) {
        viewModel.onViewClick(v?.id ?: return)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(OnboardingSetupViewModel::class.java)

        viewModel.setupLayoutVisibility.observe(this, Observer<Int> { visibility ->
            binding?.setupLayout?.visibility = visibility
        })
        viewModel.setupLayoutVisibility.observe(this, Observer<Int> { visibility ->
            binding?.setupLayout?.visibility = visibility
        })
        viewModel.passcodeInputViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.passcodeEditView?.visibility = visibility
        })
        viewModel.onboardingLayoutVisibility.observe(this, Observer<Int> { visibility ->
            binding?.onboardingLayout?.visibility = visibility
        })
        viewModel.proceedViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.proceedToSetup?.visibility = visibility
        })
        viewModel.skipViewVisibility.observe(this, Observer<Int> { visibility ->
            binding?.actionLayout?.visibility = visibility
        })
        viewModel.pageIndicator.observe(this, Observer<Int> { position ->
            binding?.pageIndicatorView?.selection = position
        })
        viewModel.passcodeInputMode.observe(this, Observer<PasscodeInputMode> {
            binding?.passcodeEditView?.inputMode = it
        })
        viewModel.headerTitle.observe(this, Observer<ResId> {
            binding?.passcodeEditView?.title = getString(it)
        })
        viewModel.showMainActivity.observe(this, Observer<ViewModelEvent<Unit>> {
            showMainActivity()
        })
        viewModel.showWarningDialogWithMessage.observe(this, Observer<ResId> { message ->
            this.showWarningDialog(message = getString(message))
        })
        viewModel.moveNext.observe(this, Observer<ViewModelEvent<Unit>> {
            binding?.onboardingPager?.currentItem = binding?.onboardingPager?.currentItem?.plus(1) ?: 0
        })
    }

    private fun initViews() {
        try {
            initOnboardingViews()
            binding?.passcodeEditView?.biometricsActionIsAvailable = false
            binding?.passcodeEditView?.listener = viewModel
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun initOnboardingViews() {
        binding?.onboardingPager?.clearOnPageChangeListeners()
        binding?.onboardingPager?.addOnPageChangeListener(this)
        viewModel.onboardingViewModels.let {
            binding?.onboardingPager?.adapter = OnboardingPagerAdapter(this, it)
            binding?.onboardingPager?.currentItem = 0
            binding?.pageIndicatorView?.setCount(it.size)
            binding?.pageIndicatorView?.selection = 0
        }
        binding?.skipActionView?.setOnClickListener(this)
        binding?.proceedToSetup?.setOnClickListener(this)
        binding?.nextActionView?.setOnClickListener(this)
    }

    private fun showMainActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java)
            .apply { putExtra(KEY_SKIP_PIN, true) }
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
    }
}
