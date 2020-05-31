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
package com.saltedge.authenticator.features.settings.passcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.tools.finishFragment
import com.saltedge.authenticator.tools.showWarningDialog
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_edit_passcode.*
import javax.inject.Inject

class PasscodeEditFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    lateinit var viewModel: PasscodeEditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_passcode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityComponents?.updateAppbar(
            titleResId = R.string.settings_passcode,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        passcodeEditView?.biometricsActionIsAvailable = false
    }

    override fun onStart() {
        super.onStart()
        passcodeEditView?.listener = viewModel
    }

    override fun onStop() {
        passcodeEditView?.listener = null
        super.onStop()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(PasscodeEditViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.titleRes.observe(this, Observer {
            passcodeEditView?.title = getString(it)
        })
        viewModel.loaderVisibility.observe(this, Observer {
            loaderView?.visibility = it
        })
        viewModel.passcodeInputMode.observe(this, Observer {
            passcodeEditView?.inputMode = it
        })
        viewModel.initialPasscode.observe(this, Observer {
            passcodeEditView?.initialPasscode = it
        })
        viewModel.infoEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                view?.let { Snackbar.make(it, messageRes, Snackbar.LENGTH_SHORT).show() }
            }
        })
        viewModel.warningEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                if (isVisible) activity?.showWarningDialog(messageRes)
            }
        })
        viewModel.closeViewEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { activity?.finishFragment() }
        })
    }
}
