/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.passcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ViewModelsFactory
import com.saltedge.authenticator.features.main.showWarningSnack
import com.saltedge.authenticator.interfaces.DialogHandlerListener
import com.saltedge.authenticator.app.authenticatorApp
import com.saltedge.authenticator.databinding.FragmentBaseListBinding
import com.saltedge.authenticator.databinding.FragmentEditPasscodeBinding
import com.saltedge.authenticator.tools.popBackStack
import com.saltedge.authenticator.tools.showWarningDialog
import com.saltedge.authenticator.widget.fragment.BaseFragment
import javax.inject.Inject

class PasscodeEditFragment : BaseFragment(), DialogHandlerListener {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    lateinit var viewModel: PasscodeEditViewModel
    private var alertDialog: AlertDialog? = null
    private lateinit var binding: FragmentEditPasscodeBinding

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
        binding = FragmentEditPasscodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityComponents?.updateAppbar(
            titleResId = R.string.settings_passcode_description,
            backActionImageResId = R.drawable.ic_appbar_action_back
        )
        binding.passcodeEditView.biometricsActionIsAvailable = false
    }

    override fun onStart() {
        super.onStart()
        binding.passcodeEditView.listener = viewModel
    }

    override fun onStop() {
        binding.passcodeEditView.listener = null
        super.onStop()
    }

    override fun closeActiveDialogs() {
        if (alertDialog?.isShowing == true) alertDialog?.dismiss()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(PasscodeEditViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.titleRes.observe(this, Observer {
            binding.passcodeEditView.title = getString(it)
        })
        viewModel.loaderVisibility.observe(this, Observer {
            binding.loaderView.root.visibility = it
        })
        viewModel.passcodeInputMode.observe(this, Observer {
            binding.passcodeEditView.inputMode = it
        })
        viewModel.initialPasscode.observe(this, Observer {
            binding.passcodeEditView.initialPasscode = it
        })
        viewModel.infoEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                view?.let {
                    activity?.showWarningSnack(
                        textResId = messageRes,
                        snackBarDuration = Snackbar.LENGTH_SHORT
                    )
                }
            }
        })
        viewModel.warningEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { messageRes ->
                alertDialog = activity?.showWarningDialog(messageRes)
            }
        })
        viewModel.closeViewEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { popBackStack() }
        })
    }
}
