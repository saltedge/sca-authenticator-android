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
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
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
        authorizationId: String?,
        confirmRequestIsInProgress: Boolean
    )

    abstract fun onConfirmDenySuccess(authorizationId: String, success: Boolean)
    abstract fun baseViewContract(): BaseAuthorizationViewContract?

    fun onAuthorizeActionSelected(isConfirmed: Boolean, quickConfirmMode: Boolean = false) {
        when {
            isConfirmed && !quickConfirmMode -> {
                if (biometricTools.isBiometricReady(appContext)) {
                    baseViewContract()?.askUserBiometricConfirmation()
                } else {
                    baseViewContract()?.askUserPasscodeConfirmation()
                }
            }
            isConfirmed -> sendConfirmRequest()
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

    override fun onConfirmDenyFailure(error: ApiErrorData) {
        updateConfirmProgressState(
            authorizationId = currentViewModel?.authorizationId,
            confirmRequestIsInProgress = false
        )
    }

    override fun onConfirmDenySuccess(result: ConfirmDenyResultData) {
        if (result.isValid()) {
            result.authorizationId?.let { onConfirmDenySuccess(it, result.success == true) }
        } else {
            updateConfirmProgressState(
                authorizationId = currentViewModel?.authorizationId,
                confirmRequestIsInProgress = false
            )
        }
    }

    private fun onAuthorizationConfirmedByUser() {
        sendConfirmRequest()
    }

    private fun sendConfirmRequest() {
        apiManager.confirmAuthorization(
            connectionAndKey = currentConnectionAndKey ?: return,
            authorizationId = currentViewModel?.authorizationId ?: return,
            authorizationCode = currentViewModel?.authorizationCode,
            resultCallback = this
        )
        updateConfirmProgressState(
            authorizationId = currentViewModel?.authorizationId,
            confirmRequestIsInProgress = true
        )
    }

    private fun sendDenyRequest() {
        apiManager.denyAuthorization(
            connectionAndKey = currentConnectionAndKey ?: return,
            authorizationId = currentViewModel?.authorizationId ?: return,
            authorizationCode = currentViewModel?.authorizationCode,
            resultCallback = this
        )
        updateConfirmProgressState(
            authorizationId = currentViewModel?.authorizationId,
            confirmRequestIsInProgress = true
        )
    }
}
