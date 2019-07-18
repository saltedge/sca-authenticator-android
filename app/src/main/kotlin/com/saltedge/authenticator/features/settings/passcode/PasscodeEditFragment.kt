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
package com.saltedge.authenticator.features.settings.passcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.passcode.di.PasscodeEditModule
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.fragment.BaseFragment
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import com.saltedge.authenticator.widget.passcode.PasscodeInputViewListener
import kotlinx.android.synthetic.main.fragment_edit_passcode.*
import javax.inject.Inject

class PasscodeEditFragment : BaseFragment(), PasscodeEditContract.View, PasscodeInputViewListener {

    @Inject lateinit var presenterContract: PasscodeEditContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        activityComponents?.updateAppbarTitle(getString(R.string.settings_passcode))
        return inflater.inflate(R.layout.fragment_edit_passcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            passcodeInputView?.biometricsActionIsAvailable = false
            passcodeInputView?.listener = this
        } catch (e: Exception) {
            e.log()
        }
    }

    override fun onStart() {
        super.onStart()
        presenterContract.viewContract = this
        presenterContract.onViewCreated()
    }

    override fun onStop() {
        presenterContract.viewContract = null
        super.onStop()
    }

    override fun initInputMode(mode: PasscodeInputView.InputMode, passcode: String) {
        passcodeInputView?.initInputMode(mode, passcode)
    }

    override fun updateViewContent(titleTextResId: Int, positiveActionTextResId: Int) {
        titleView?.setText(titleTextResId)
        passcodeInputView?.setPositiveActionText(positiveActionTextResId)
    }

    override fun showProgress() {
        loaderView?.setVisible(show = true)
    }

    override fun hideProgress() {
        if (isVisible) loaderView?.setVisible(show = false)
    }

    override fun closeView() {
        if (isVisible) activity?.finishFragment()
    }

    override fun showWarning(messageResId: Int) {
        if (isVisible) activity?.showWarningDialog(messageResId)
    }

    override fun showInfo(messageResId: Int) {
        if (isVisible) view?.let {
            Snackbar.make(it, messageResId, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onBiometricInputSelected() {}

    override fun onPasscodeInputCanceledByUser() {
        activity?.finishFragment()
    }

    override fun onEnteredPasscodeIsValid() {
        presenterContract.enteredCurrentPasscode()
    }

    override fun onEnteredPasscodeIsInvalid() {
    }

    override fun onNewPasscodeEntered(mode: PasscodeInputView.InputMode, passcode: String) {
        presenterContract.enteredNewPasscode(mode)
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        presenterContract.newPasscodeConfirmed(passcode)
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addPasscodeEditModule(PasscodeEditModule())?.inject(this)
    }
}
