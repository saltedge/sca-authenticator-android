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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.events.ViewModelEvent
import com.saltedge.authenticator.features.main.MainActivity
import com.saltedge.authenticator.features.onboarding.di.OnboardingSetupModule
import com.saltedge.authenticator.features.security.KEY_SKIP_PIN
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import com.saltedge.authenticator.widget.passcode.PasscodeInputViewListener
import kotlinx.android.synthetic.main.activity_onboarding.*
import javax.inject.Inject

class OnboardingSetupActivity : AppCompatActivity(),
    OnboardingSetupContract.View,
    ViewPager.OnPageChangeListener,
    View.OnClickListener,
    PasscodeInputViewListener {

    lateinit var viewModel: OnboardingSetupViewModel
    @Inject lateinit var viewModelFactory: OnboardingSetupViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setupViewModel()
        setContentView(R.layout.activity_onboarding)
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

    override fun onEnteredPasscodeIsInvalid() {}

    override fun onNewPasscodeEntered(mode: PasscodeInputView.InputMode, passcode: String) {
        viewModel.enteredNewPasscode(inputMode = mode)
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        viewModel.newPasscodeConfirmed(passcode)
    }

    // After QR code
    override fun showMainActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java)
            .apply { putExtra(KEY_SKIP_PIN, true) }
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
    }

    override fun showWarningDialogWithMessage(message: String) {
        this.showWarningDialog(message = message)
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addOnboardingSetupModule(OnboardingSetupModule())?.inject(
            this
        )
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(OnboardingSetupViewModel::class.java)

        viewModel.pageIndicator.observe(this, Observer<ViewModelEvent<Int>> {
            it?.getContentIfNotHandled()?.let { position ->
                pageIndicatorView?.selection = position
            }
        })

        viewModel.hideSkipViewAndShowProceedView.observe(this, Observer<ViewModelEvent<Boolean>> {
            it?.getContentIfNotHandled()?.let { hideSkipViewAndShowProceedView ->
                if (hideSkipViewAndShowProceedView) {  //extract logic in view model, try to use databinding
                    actionLayout?.setVisible(show = false)
                    proceedToSetup?.setVisible(show = true)
                }
            }
        })


        //PASSCODE
        viewModel.hideOnboardingAndShowPasscodeSetupView.observe(this, Observer<ViewModelEvent<Boolean>> {
            it?.getContentIfNotHandled()?.let { hideOnboardingAndShowPasscodeSetupView ->
                if (hideOnboardingAndShowPasscodeSetupView) {  //extract logic in view model, try to use databinding
                    onboardingLayout?.setVisible(show = false)
                    setupLayout?.setVisible(show = true)
                }
            }
        })

        viewModel.setPasscodeInputMode.observe(this, Observer<ViewModelEvent<PasscodeInputView.InputMode>> {
            it?.getContentIfNotHandled()?.let { inputMode ->
                passcodeInputView?.initInputMode(inputMode = inputMode)
            }
        })

        //updateSetupViews
        viewModel.headerTitle.observe(this, Observer<ViewModelEvent<Int>> {
            it?.getContentIfNotHandled()?.let { getSetupTitleResId ->
                titleView?.setText(getSetupTitleResId)
            }
        })
        viewModel.headerDescription.observe(this, Observer<ViewModelEvent<Int>> {
            it?.getContentIfNotHandled()?.let { getSetupSubtitleResId ->
                descriptionView?.setText(getSetupSubtitleResId)
            }
        })
        viewModel.showPasscodeCancel.observe(this, Observer<ViewModelEvent<Boolean>> {
            it?.getContentIfNotHandled()?.let { shouldShowPasscodeInputNegativeActionView ->
                shouldShowPasscodeInputNegativeActionView?.let { passcodeInputView?.cancelActionIsAvailable = it }
            }
        })
        viewModel.passcodePositiveActionText.observe(this, Observer<ViewModelEvent<Int>> {
            it?.getContentIfNotHandled()?.let { getPositivePasscodeActionViewText ->
                getPositivePasscodeActionViewText?.let { passcodeInputView?.setPositiveActionText(it) }
            }
        })
        viewModel.hidePasscodeInputAndShowSetupView.observe(this, Observer<ViewModelEvent<Boolean>> {
            it?.getContentIfNotHandled()?.let { hidePasscodeInputAndShowSetupView ->
                if (hidePasscodeInputAndShowSetupView) {
                    passcodeInputView?.setVisible(show = false)
                    showMainActivity()
                }
            }
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
        actionLayout?.setVisible(show = true)
        proceedToSetup?.setVisible(show = false)
    }

    private fun initSetupViews() {
        passcodeInputView?.biometricsActionIsAvailable = false
        passcodeInputView?.cancelActionIsAvailable = false
        passcodeInputView?.listener = this
    }
}
