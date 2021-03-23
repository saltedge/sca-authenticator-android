/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.passcode.setup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fentury.applock.R
import com.fentury.applock.root.ViewModelsFactory
import com.fentury.applock.contracts.PasscodeListener
import com.fentury.applock.models.ViewModelEvent
import com.fentury.applock.root.SEAppLock
import com.fentury.applock.tools.ResId
import com.fentury.applock.widget.passcode.PasscodeInputMode
import com.fentury.applock.tools.showWarningDialog
import kotlinx.android.synthetic.main.fragment_app_lock.*
import java.lang.ClassCastException
import javax.inject.Inject

class PasscodeSetupFragment : Fragment() {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    private lateinit var viewModel: PasscodeSetupViewModel
    lateinit var callback: PasscodeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SEAppLock.passcodeComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        initViews()
    }

    private fun initViews() {
        passcodeEditView?.biometricsActionIsAvailable = false
        passcodeEditView?.listener = viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app_lock, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as PasscodeListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnNewPasscodeSetListener")
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(PasscodeSetupViewModel::class.java)

        viewModel.passcodeInputMode.observe(this, Observer<PasscodeInputMode> {
            passcodeEditView?.inputMode = it
        })
        viewModel.headerTitle.observe(this, Observer<ResId> {
            passcodeEditView.title = getString(it)
        })
        viewModel.onNewPasscodeSet.observe(this, Observer<ViewModelEvent<Unit>> {
            onNewPasscodeSet()
        })
        viewModel.showWarningDialogWithMessage.observe(this, Observer<ResId> { message ->
            activity?.showWarningDialog(message = getString(message))
        })
    }

    private fun onNewPasscodeSet() {
        callback.onNewPasscodeSet()
    }
}