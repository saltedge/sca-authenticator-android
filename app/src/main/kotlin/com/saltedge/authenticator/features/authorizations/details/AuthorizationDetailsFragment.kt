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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.TIME_VIEW_UPDATE_TIMEOUT
import com.saltedge.authenticator.features.authorizations.confirmPasscode.ConfirmPasscodeDialog
import com.saltedge.authenticator.features.authorizations.details.di.AuthorizationDetailsModule
import com.saltedge.authenticator.interfaces.UpActionImageListener
import com.saltedge.authenticator.sdk.constants.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.sdk.constants.KEY_CONNECTION_ID
import com.saltedge.authenticator.sdk.constants.KEY_ID
import com.saltedge.authenticator.tool.*
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.biometric.showAuthorizationConfirm
import com.saltedge.authenticator.widget.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_authorization_details.*
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
            connectionId = arguments?.getString(KEY_CONNECTION_ID),
            authorizationId = arguments?.getString(KEY_AUTHORIZATION_ID)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityComponents?.updateAppbarTitle(getString(R.string.authorization_feature_title))
        return inflater.inflate(R.layout.fragment_authorization_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contentView?.setActionClickListener(this)
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
        presenterContract.onFragmentResume()
    }

    override fun onPause() {
        presenterContract.onFragmentPause()
        super.onPause()
    }

    override fun onStop() {
        biometricPrompt.resultCallback = null
        presenterContract.viewContract = null
        super.onStop()
    }

    override fun getUpActionImage(): Int? = R.drawable.ic_close_white_24dp

    override fun updateViewContent() {
        headerView?.setLogoUrl(presenterContract.providerLogo)
        headerView?.setTitle(presenterContract.providerName)
        presenterContract.startTime?.let { startTime -> presenterContract.endTime?.let { endTime ->
            headerView?.setProgressTime(startTime, endTime)
        } }

        contentView?.setTitle(presenterContract.title)
        contentView?.setDescription(presenterContract.description)
    }

    override fun updateTimeViews() {
        headerView?.onTimeUpdate()
    }

    override fun setHeaderVisibility(show: Boolean) {
        headerView?.setInvisible(!show)
    }

    override fun setContentVisibility(show: Boolean) {
        contentView?.setInvisible(!show)
    }

    override fun showError(errorMessage: String) {
        view?.let { Snackbar.make(it, errorMessage, Snackbar.LENGTH_LONG).show() }
    }

    override fun closeView() {
        activity?.finishFragment()
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

    override fun closeViewWithErrorResult(errorMessage: String) {
        setCompleteView(
            drawableResId = R.drawable.ic_complete_error_70,
            titleText = getString(R.string.authorizations_finished_error),
            subTitleText = errorMessage,
            alpha = 0.75f
        )

        completeView?.animate()?.alpha(1f)?.setDuration(1000)?.withEndAction { closeView() }
    }

    override fun closeViewWithTimeOutResults() {
        setCompleteView(
            drawableResId = R.drawable.ic_time_out_70,
            titleText = getString(R.string.authorizations_time_out),
            subTitleText = getString(R.string.authorizations_time_out_description),
            actionResId = R.string.actions_ok
        )

        completeView?.animate()?.alpha(1f)?.setDuration(1000)?.withEndAction { closeView() }
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

    private fun setCompleteView(
        drawableResId: Int,
        titleText: String,
        subTitleText: String? = null,
        actionResId: Int? = null,
        alpha: Float = 0.1f
    ) {
        completeView?.alpha = alpha
        completeView?.setVisible(true)
        completeView?.setTitleText(titleText)
        subTitleText?.let { completeView?.setSubtitleText(it) }
        actionResId?.let { completeView?.setMainActionText(it) }
        completeView?.setIconResource(drawableResId)
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
         * @param authorizationId - id of Authorization
         * @param connectionId - id of Connection for Authorization
         *
         * @return AuthorizationDetailsFragment
         */
        fun newInstance(authorizationId: String, connectionId: String): AuthorizationDetailsFragment {
            return AuthorizationDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_CONNECTION_ID, connectionId)
                    putString(KEY_AUTHORIZATION_ID, authorizationId)
                }
            }
        }
    }
}
