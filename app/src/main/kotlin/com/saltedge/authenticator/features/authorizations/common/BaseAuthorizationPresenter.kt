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
package com.saltedge.authenticator.features.authorizations.common

import android.content.Context
import com.saltedge.authenticator.features.authorizations.confirmPasscode.PasscodePromptCallback
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationResult
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.AuthorizationID
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResultData
import com.saltedge.authenticator.sdk.model.response.isValid
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptCallback

abstract class BaseAuthorizationPresenter(
    val appContext: Context,
    val biometricTools: BiometricToolsAbs,
    val apiManager: AuthenticatorApiManagerAbs
) : BiometricPromptCallback, PasscodePromptCallback, ConfirmAuthorizationResult {

    var currentViewModel: AuthorizationViewModel? = null
    var currentConnectionAndKey: ConnectionAndKey? = null

    abstract fun updateConfirmProgressState(
        connectionID: ConnectionID,
        authorizationID: AuthorizationID,
        confirmRequestIsInProgress: Boolean
    )

    abstract fun onConfirmDenySuccess(authorizationId: String, connectionID: ConnectionID, success: Boolean)
    abstract fun baseViewContract(): BaseAuthorizationViewContract?

    fun onAuthorizeActionSelected(
        confirmRequest: Boolean,
        quickConfirmMode: Boolean = false
    ) {
        when {
            confirmRequest && !quickConfirmMode -> {
                if (biometricTools.isBiometricReady(appContext)) {
                    baseViewContract()?.askUserBiometricConfirmation()
                } else {
                    baseViewContract()?.askUserPasscodeConfirmation()
                }
            }
            confirmRequest -> sendConfirmRequest()
            else -> sendDenyRequest()
        }
    }

    override fun biometricAuthFinished() {
        onAuthorizationConfirmedByUser()
    }

    override fun biometricsCanceledByUser() {
        baseViewContract()?.askUserPasscodeConfirmation()
    }

    override fun successAuthWithPasscode() {
        onAuthorizationConfirmedByUser()
    }

    override fun passcodePromptCanceledByUser() {}

    override fun onConfirmDenyFailure(
        error: ApiErrorData,
        connectionID: ConnectionID,
        authorizationID: AuthorizationID
    ) {
        updateConfirmProgressState(
            connectionID = connectionID,
            authorizationID = authorizationID,
            confirmRequestIsInProgress = false
        )
    }

    override fun onConfirmDenySuccess(result: ConfirmDenyResultData, connectionID: ConnectionID) {
        if (result.isValid()) {
            result.authorizationId?.let {
                onConfirmDenySuccess(
                    authorizationId = it,
                    connectionID = connectionID,
                    success = result.success == true
                )
            }
        }
    }

    private fun onAuthorizationConfirmedByUser() {
        sendConfirmRequest()
    }

    private fun sendConfirmRequest() {
        val viewModel = currentViewModel ?: return
        val connectionAndKey = currentConnectionAndKey ?: return
        apiManager.confirmAuthorization(
            connectionAndKey = connectionAndKey,
            authorizationId = viewModel.authorizationID,
            authorizationCode = viewModel.authorizationCode,
            resultCallback = this
        )
        updateConfirmProgressState(
            connectionID = viewModel.connectionID,
            authorizationID = viewModel.authorizationID,
            confirmRequestIsInProgress = true
        )
    }

    private fun sendDenyRequest() {
        val viewModel = currentViewModel ?: return
        val connectionAndKey = currentConnectionAndKey ?: return
        apiManager.denyAuthorization(
            connectionAndKey = connectionAndKey,
            authorizationId = viewModel.authorizationID,
            authorizationCode = viewModel.authorizationCode,
            resultCallback = this
        )
        updateConfirmProgressState(
            connectionID = viewModel.connectionID,
            authorizationID = viewModel.authorizationID,
            confirmRequestIsInProgress = true
        )
    }
}
