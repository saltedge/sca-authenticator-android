/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.passcode.edit

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fentury.applock.R
import com.fentury.applock.root.ViewModelsFactory
import com.fentury.applock.contracts.PasscodeListener
import com.fentury.applock.root.Constants.Companion.KEY_TITLE
import com.fentury.applock.root.SEAppLock
import com.fentury.applock.tools.ResId
import com.fentury.applock.tools.showWarningDialog
import com.fentury.applock.tools.showWarningSnack
import com.fentury.applock.widget.biometric.BiometricPromptAbs
import com.fentury.applock.widget.biometric.BiometricPromptCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_app_lock.passcodeEditView
import kotlinx.android.synthetic.main.fragment_edit_passcode.*
import kotlinx.android.synthetic.main.view_keypad.view.*
import kotlinx.android.synthetic.main.view_passcode_input.view.*
import java.lang.ClassCastException
import javax.inject.Inject

private const val KEY_CONFIRMATION_MODE = "confirmation_mode"

class PasscodeEditFragment: Fragment(), BiometricPromptCallback {

    @Inject lateinit var viewModelFactory: ViewModelsFactory
    @Inject lateinit var biometricPrompt: BiometricPromptAbs
    private lateinit var viewModel: PasscodeEditViewModel
    private var alertDialog: AlertDialog? = null
    private var callback: PasscodeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SEAppLock.passcodeComponent.inject(this)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_passcode, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as PasscodeListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnNewPasscodeSetListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        passcodeEditView?.biometricsActionIsAvailable = arguments?.getBoolean(KEY_CONFIRMATION_MODE) ?: false
        passcodeEditView?.keypadView?.forgotActionView?.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        passcodeEditView?.listener = viewModel
        biometricPrompt.resultCallback = this
    }

    override fun onStop() {
        passcodeEditView?.listener = null
        biometricPrompt.resultCallback = null
        biometricPrompt.dismissBiometricPrompt()
        callback = null
        super.onStop()
    }

    override fun biometricAuthFinished() {
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
    }

    override fun biometricsCanceledByUser() {
        biometricPrompt.dismissBiometricPrompt()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(PasscodeEditViewModel::class.java)
        viewModel.bindLifecycleObserver(lifecycle = lifecycle)

        viewModel.setInitialData(
            titleTextRes = arguments?.getInt(KEY_TITLE),
            isConfirmationMode = arguments?.getBoolean(KEY_CONFIRMATION_MODE)
        )

        viewModel.titleRes.observe(this, Observer {
            passcodeEditView?.title = getString(it)
        })
        viewModel.descriptionVisibility.observe(this, Observer {
            passcodeEditView?.descriptionView?.visibility = it
            passcodeEditView?.descriptionView?.text = SEAppLock.passcodeDescriptionText
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
                view?.let {
                    passcodeEditView?.showWarningSnack(
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
            event.getContentIfNotHandled()?.let {
                callback?.onNewPasscodeSet()
            }
        })
        viewModel.verifySuccessEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
            }
        })
        viewModel.verifyFailEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
            }
        })
        viewModel.showBiometricPromptEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                if (biometricPrompt.resultCallback != null) {
                    activity?.let { it1 ->
                        biometricPrompt.showBiometricPrompt(
                            context = it1,
                            title = SEAppLock.applicationName ?: "",
                            descriptionResId = R.string.actions_confirm,
                            negativeActionTextResId = R.string.actions_cancel
                        )
                    }
                }
            }
        })
    }

    companion object {
        fun newInstance(titleRes: ResId, isPasscodeConfirmationMode: Boolean): PasscodeEditFragment {
            return PasscodeEditFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_TITLE, titleRes)
                    putBoolean(KEY_CONFIRMATION_MODE, isPasscodeConfirmationMode)
                }
            }
        }
    }
}