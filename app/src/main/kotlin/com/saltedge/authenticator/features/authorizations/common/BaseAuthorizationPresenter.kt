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
import com.saltedge.authenticator.sdk.model.AuthorizationID
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.ConnectionID
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResultData
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptCallback

abstract class BaseAuthorizationPresenter(
    internal val appContext: Context,
    internal val biometricTools: BiometricToolsAbs,
    internal val apiManager: AuthenticatorApiManagerAbs
) : BiometricPromptCallback, PasscodePromptCallback, ConfirmAuthorizationResult {

    var currentViewModel: AuthorizationViewModel? = null
    var currentConnectionAndKey: ConnectionAndKey? = null

    abstract fun onAuthorizeStart(connectionID: ConnectionID, authorizationID: AuthorizationID, type: ActionType)
    abstract fun onConfirmDenySuccess(success: Boolean, connectionID: ConnectionID, authorizationID: AuthorizationID)
    abstract fun baseViewContract(): BaseAuthorizationViewContract?

    fun onAuthorizeActionSelected(
        requestType: ActionType,
        quickConfirmMode: Boolean = false
    ) {
        val viewModel = currentViewModel ?: return
        if (!viewModel.canBeAuthorized) return
        when(requestType) {
            ActionType.CONFIRM -> sendConfirmRequest()
            ActionType.DENY -> sendDenyRequest()
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

    override fun onConfirmDenySuccess(result: ConfirmDenyResultData, connectionID: ConnectionID) {
        onConfirmDenySuccess(
            success = result.success == true && result.authorizationId?.isNotEmpty() == true,
            connectionID = connectionID,
            authorizationID = result.authorizationId ?: ""
        )
    }

    private fun onAuthorizationConfirmedByUser() {
        sendConfirmRequest()
    }

    private fun sendConfirmRequest() {
        val viewModel = currentViewModel ?: return
        val connectionAndKey = currentConnectionAndKey ?: return
        onAuthorizeStart(connectionID = viewModel.connectionID, authorizationID = viewModel.authorizationID, type = ActionType.CONFIRM)
        apiManager.confirmAuthorization(
            connectionAndKey = connectionAndKey,
            authorizationId = viewModel.authorizationID,
            authorizationCode = viewModel.authorizationCode,
            resultCallback = this
        )
    }

    private fun sendDenyRequest() {
        val viewModel = currentViewModel ?: return
        val connectionAndKey = currentConnectionAndKey ?: return
        onAuthorizeStart(connectionID = viewModel.connectionID, authorizationID = viewModel.authorizationID, type = ActionType.DENY)
        apiManager.denyAuthorization(
            connectionAndKey = connectionAndKey,
            authorizationId = viewModel.authorizationID,
            authorizationCode = viewModel.authorizationCode,
            resultCallback = this
        )
    }
}
