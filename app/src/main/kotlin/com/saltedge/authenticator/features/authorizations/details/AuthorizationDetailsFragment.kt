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
package com.saltedge.authenticator.features.authorizations.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_MODE
import com.saltedge.authenticator.app.TIME_VIEW_UPDATE_TIMEOUT
import com.saltedge.authenticator.features.authorizations.common.AuthorizationViewModel
import com.saltedge.authenticator.features.authorizations.confirmPasscode.ConfirmPasscodeDialog
import com.saltedge.authenticator.features.authorizations.details.di.AuthorizationDetailsModule
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.sdk.constants.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.sdk.constants.KEY_CONNECTION_ID
import com.saltedge.authenticator.sdk.constants.KEY_DATA
import com.saltedge.authenticator.sdk.constants.KEY_ID
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.biometric.showAuthorizationConfirm
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_authorization_details.*
import kotlinx.android.synthetic.main.view_action_buttons.*
import java.util.*
import javax.inject.Inject

class AuthorizationDetailsFragment : BaseFragment(),
    AuthorizationDetailsContract.View,
    View.OnClickListener,
    UpActionImageListener {

    @Inject
    lateinit var presenterContract: AuthorizationDetailsPresenter
    @Inject
    lateinit var biometricPrompt: BiometricPromptAbs
    @Inject
    lateinit var timeViewUpdateTimer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        presenterContract.setInitialData(
            connectionId = arguments?.getString(KEY_CONNECTION_ID) ?: "",
            authorizationId = arguments?.getString(KEY_AUTHORIZATION_ID),
            viewModel = arguments?.getSerializable(KEY_DATA) as AuthorizationViewModel?,
            quickConfirmMode = arguments?.getBoolean(KEY_MODE) ?: false
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityComponents?.updateAppbarTitle(getString(R.string.authorizations_feature_title))
        return inflater.inflate(R.layout.fragment_authorization_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        negativeActionView?.setOnClickListener(this)
        positiveActionView?.setOnClickListener(this)
        connectionLogoView?.setOnClickListener(this)
        completeView?.setOnClickListener(this)
        activityComponents?.hideNavigationBar()
    }

    override fun onDestroyView() {
        activityComponents?.showNavigationBar()
        super.onDestroyView()
    }

    override fun onClick(view: View?) {
        presenterContract.onViewClick(view?.id ?: return)
    }

    override fun onStart() {
        super.onStart()
        presenterContract.viewContract = this
        biometricPrompt.resultCallback = presenterContract
    }

    override fun onResume() {
        super.onResume()
        presenterContract.onViewResume()
        updateViewContent()
    }

    override fun onPause() {
        presenterContract.onViewPause()
        super.onPause()
    }

    override fun onStop() {
        biometricPrompt.resultCallback = null
        presenterContract.viewContract = null
        super.onStop()
    }

    override fun getUpActionImage(): Int? = R.drawable.ic_close_white_24dp

    override fun updateViewContent() {
        updateLayoutsVisibility()
        timerTextView?.text = presenterContract.remainedTimeDescription
        progressBar?.max = presenterContract.maxProgressSeconds
        progressBar?.progress = presenterContract.secondsFromStartDate
        titleTextView?.text = presenterContract.title
        providerNameView?.text = presenterContract.providerName

        descriptionTextView?.setVisible(show = presenterContract.shouldShowDescriptionTextView)
        descriptionWebView?.setVisible(show = presenterContract.shouldShowDescriptionWebView)
        if (presenterContract.shouldShowDescriptionWebView) {
            descriptionWebView?.loadData(
                presenterContract.description,
                "text/html; charset=utf-8",
                "UTF-8"
            )
        } else {
            descriptionTextView?.movementMethod = ScrollingMovementMethod()
            descriptionTextView?.text = presenterContract.description
        }
        if (presenterContract.shouldShowProviderLogo) {
            connectionLogoView?.visibility = View.VISIBLE
            connectionLogoView?.loadImage(
                imageUrl = presenterContract.providerLogo,
                placeholderId = R.drawable.ic_logo_bank_placeholder
            )
        } else {
            connectionLogoView?.visibility = View.GONE
        }
    }

    override fun updateTimeView(remainedSecondsTillExpire: Int, remainedTimeDescription: String) {
        activity?.runOnUiThread {
            progressBar?.progress = remainedSecondsTillExpire
            timerTextView?.text = remainedTimeDescription
        }
    }

    override fun setActionsLayoutVisibility(show: Boolean) {
        actionsLayout?.setInvisible(!show)
        actionsLayout?.isEnabled = show
    }

    override fun showError(errorMessage: String) {
        view?.let { Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG).show() }
    }

    override fun closeView() {
        activity?.finishFragment()
    }

    override fun closeViewWithErrorResult(errorMessage: String) {
        completeView?.setTitleText(getString(R.string.authorizations_finished_error))
        completeView?.setSubtitleText(errorMessage)
        completeView?.setIconResource(R.drawable.ic_complete_error_70)
        completeView?.alpha = 0.75f
        completeView?.setVisible(true)
        completeView?.animate()?.alpha(1f)?.setDuration(1000)?.withEndAction { closeView() }
    }

    override fun closeViewWithSuccessResult(authorizationId: String) {
        targetFragment?.onActivityResult(
            targetRequestCode,
            Activity.RESULT_OK,
            Intent().putExtra(KEY_ID, authorizationId)
        )
        setCompleteView(
            drawableResId = R.drawable.ic_complete_ok_70,
            titleText = getString(R.string.authorizations_finished_successfully)
        )
        completeView?.animate()?.alpha(1f)?.setDuration(1000)?.withEndAction { closeView() }
    }

    override fun showTimeOutView() {
        setCompleteView(
            drawableResId = R.drawable.ic_time_out_70,
            titleText = getString(R.string.authorizations_time_out),
            subTitleText = getString(R.string.authorizations_time_out_description),
            actionResId = R.string.actions_ok
        )
        completeView?.animate()?.alpha(1f)
    }

    private fun setCompleteView(
        drawableResId: Int,
        titleText: String,
        subTitleText: String? = null,
        actionResId: Int? = null
    ) {
        completeView?.alpha = 0.1f
        completeView?.setVisible(true)
        completeView?.setTitleText(titleText)
        subTitleText?.let { completeView?.setSubtitleText(it) }
        actionResId?.let { completeView?.setMainActionText(it) }
        completeView?.setIconResource(drawableResId)
    }

    override fun askUserBiometricConfirmation() {
        activity?.let { biometricPrompt.showAuthorizationConfirm(it) }
    }

    override fun askUserPasscodeConfirmation() {
        activity?.showDialogFragment(
            ConfirmPasscodeDialog.newInstance(resultCallback = presenterContract)
        )
    }

    override fun startTimer() {
        timeViewUpdateTimer = Timer()
        timeViewUpdateTimer.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    presenterContract.onTimerTick()
                }
            }
        }, 0, TIME_VIEW_UPDATE_TIMEOUT)
    }

    override fun stopTimer() {
        timeViewUpdateTimer.cancel()
        timeViewUpdateTimer.purge()
    }

    private fun updateLayoutsVisibility() {
        setHeaderVisibility(presenterContract.shouldShowTimeView)
        progressBarView?.setVisible(presenterContract.shouldShowProgressView)
        setActionsLayoutVisibility(presenterContract.shouldShowActionsLayout)
    }

    private fun setHeaderVisibility(show: Boolean) {
        timerTextView?.setVisible(show)
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addAuthorizationDetailsModule(AuthorizationDetailsModule())?.inject(
            this
        )
    }

    companion object {
        /**
         * Creates new instance of AuthorizationDetailsFragment
         *
         * @param viewModel - model from AuthorizationsList
         *
         * @return AuthorizationDetailsFragment
         */
        fun newInstance(viewModel: AuthorizationViewModel): AuthorizationDetailsFragment {
            return newInstance(
                authorizationId = viewModel.authorizationId,
                connectionId = viewModel.connectionId,
                viewModel = viewModel
            )
        }

        /**
         * Creates new instance of AuthorizationDetailsFragment
         *
         * @param authorizationId - id of Authorization
         * @param connectionId - id of Connection for Authorization
         * @param quickConfirmMode - if true then Details component should not ask biometric/passcode confirmation
         * @param viewModel - model from AuthorizationsList
         *
         * @return AuthorizationDetailsFragment
         */
        fun newInstance(
            authorizationId: String,
            connectionId: String,
            quickConfirmMode: Boolean = false,
            viewModel: AuthorizationViewModel? = null
        ): AuthorizationDetailsFragment {
            return AuthorizationDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_CONNECTION_ID, connectionId)
                    putString(KEY_AUTHORIZATION_ID, authorizationId)
                    putBoolean(KEY_MODE, quickConfirmMode)
                    putSerializable(KEY_DATA, viewModel)
                }
            }
        }
    }
}
