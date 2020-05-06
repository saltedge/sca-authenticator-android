/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.databinding.OnboardingSetupBinding
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.tools.log
import com.saltedge.authenticator.tools.showWarningDialog
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import com.saltedge.authenticator.widget.passcode.PasscodeInputViewListener
import com.saltedge.authenticator.widget.security.KEY_SKIP_PIN
import kotlinx.android.synthetic.main.activity_onboarding.*
import javax.inject.Inject

class OnboardingSetupActivity : AppCompatActivity(),
    ViewPager.OnPageChangeListener,
    View.OnClickListener,
    PasscodeInputViewListener {

    lateinit var viewModel: OnboardingSetupViewModel
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var binding: OnboardingSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
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

    override fun onBiometricInputSelected() {}

    override fun onPasscodeInputCanceledByUser() {
        viewModel.passcodeInputCanceledByUser()
    }

    override fun onEnteredPasscodeIsValid() {}

    override fun onEnteredPasscodeIsInvalid() {
        viewModel.reEnterPasscode()
    }

    override fun onNewPasscodeEntered(mode: PasscodeInputView.InputMode, passcode: String) {
        viewModel.enteredNewPasscode(inputMode = mode)
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        viewModel.newPasscodeConfirmed(passcode)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(OnboardingSetupViewModel::class.java)
        binding.onboardingSetupViewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        viewModel.pageIndicator.observe(this, Observer<Int> { position ->
            pageIndicatorView?.selection = position
        })
        viewModel.setPasscodeInputMode.observe(this, Observer<PasscodeInputView.InputMode> {
            passcodeInputView?.initInputMode(inputMode = it)
        })
        viewModel.showMainActivity.observe(this, Observer<ViewModelEvent<Unit>> {
            showMainActivity()
        })
        viewModel.showWarningDialogWithMessage.observe(this, Observer<String> { message ->
            this.showWarningDialog(message = message)
        })
        viewModel.moveNext.observe(this, Observer<ViewModelEvent<Unit>> {
            onboardingPager.currentItem = onboardingPager.currentItem + 1
        })
    }

    private fun initViews() {
        try {
            initOnboardingViews()
            initSetupViews()
        } catch (e: Exception) {
            e.log()
        }
    }

    private fun initOnboardingViews() {
        onboardingPager?.clearOnPageChangeListeners()
        onboardingPager?.addOnPageChangeListener(this)
        viewModel.onboardingViewModels.let {
            onboardingPager?.adapter = OnboardingPagerAdapter(this, it)
            onboardingPager?.currentItem = 0
            pageIndicatorView?.setCount(it.size)
            pageIndicatorView?.selection = 0
        }
        skipActionView?.setOnClickListener(this)
        proceedToSetup?.setOnClickListener(this)
        nextActionView?.setOnClickListener(this)
    }

    private fun initSetupViews() {
        passcodeInputView?.biometricsActionIsAvailable = false
        passcodeInputView?.listener = this
    }

    private fun showMainActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java)
            .apply { putExtra(KEY_SKIP_PIN, true) }
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
    }
}
