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
import androidx.viewpager.widget.ViewPager
import com.saltedge.authenticator.R
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

    @Inject
    lateinit var presenter: OnboardingSetupPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setContentView(R.layout.activity_onboarding)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        presenter.viewContract = this
    }

    override fun onStop() {
        presenter.viewContract = null
        super.onStop()
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        presenter.onOnboardingPageSelected(position)
    }

    override fun onClick(v: View?) {
        presenter.onViewClick(v?.id ?: return)
    }

    override fun onBiometricInputSelected() {}

    override fun onPasscodeInputCanceledByUser() {
        presenter.passcodeInputCanceledByUser()
    }

    override fun onEnteredPasscodeIsValid() {}

    override fun onEnteredPasscodeIsInvalid() {}

    override fun onNewPasscodeEntered(mode: PasscodeInputView.InputMode, passcode: String) {
        presenter.enteredNewPasscode(inputMode = mode)
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        presenter.newPasscodeConfirmed(passcode)
    }

    override fun updatePageIndicator(position: Int) {
        pageIndicatorView?.selection = position
    }

    override fun hideSkipViewAndShowProceedView() {
        skipActionView?.setVisible(show = false)
        proceedToSetup?.setVisible(show = true)
    }

    override fun hideOnboardingAndShowPasscodeSetupView() {
        onboardingLayout?.setVisible(show = false)
        setupLayout?.setVisible(show = true)
    }

    override fun hidePasscodeInputAndShowSetupView() {
        passcodeInputView?.setVisible(show = false)
        setupLogoImage?.setVisible(show = true)
        setupActionsLayout?.setVisible(show = true)
    }

    override fun showMainActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java)
            .apply { putExtra(KEY_SKIP_PIN, true) }
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
    }

    override fun showWarningDialogWithMessage(message: String) {
        this.showWarningDialog(message)
    }

    override fun updateSetupViews(
        setupStepProgress: Float,
        headerTitle: Int,
        headerDescription: Int,
        showPasscodeCancel: Boolean?,
        passcodePositiveActionText: Int?,
        setupImageResId: Int,
        actionText: Int
    ) {
        stepProgressView?.setStepProgress(setupStepProgress)
        titleView?.setText(headerTitle)
        descriptionView?.setText(headerDescription)
        setupLogoImage?.setImageResource(setupImageResId)
        actionView?.setText(actionText)

        showPasscodeCancel?.let { passcodeInputView?.cancelActionIsAvailable = it }
        passcodePositiveActionText?.let { passcodeInputView?.setPositiveActionText(it) }
    }

    override fun setPasscodeInputMode(inputMode: PasscodeInputView.InputMode) {
        passcodeInputView?.initInputMode(inputMode = inputMode)
    }

    override fun hideSkipView() {
        skipSetupActionView?.setInvisible(true)
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
        presenter.onboardingViewModels.let {
            onboardingPager?.adapter = OnboardingPagerAdapter(this, it)
            onboardingPager?.currentItem = 0
            pageIndicatorView?.setCount(it.size)
            pageIndicatorView?.selection = 0
        }
        skipActionView?.setOnClickListener(this)
        proceedToSetup?.setOnClickListener(this)
        skipActionView?.setVisible(show = true)
        proceedToSetup?.setVisible(show = false)
    }

    private fun initSetupViews() {
        stepProgressView?.stepCount = presenter.setupStepCount

        passcodeInputView?.biometricsActionIsAvailable = false
        passcodeInputView?.cancelActionIsAvailable = false
        passcodeInputView?.listener = this

        actionView?.setOnClickListener(this)
        skipSetupActionView?.setOnClickListener(this)
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addOnboardingSetupModule(OnboardingSetupModule())?.inject(
            this
        )
    }
}
