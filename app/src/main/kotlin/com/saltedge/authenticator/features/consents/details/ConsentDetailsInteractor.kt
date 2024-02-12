/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.details

import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.contract.ConsentRevokeListener
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION

class ConsentDetailsInteractor(
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val v1ApiManager: AuthenticatorApiManagerAbs,
    private val v2ApiManager: ScaServiceClientAbs,
) : ConsentDetailsInteractorAbs, ConsentRevokeListener {

    private var optRichConnection: RichConnection? = null
    private var _consentData: ConsentData? = null
    override var contract: ConsentDetailsInteractorCallback? = null
    override val consentData: ConsentData?
        get() = _consentData
    override val connectionName: String?
        get() = optRichConnection?.connection?.name

    override fun setInitialData(connectionGuid: GUID?, consent: ConsentData?) {
        val connection = connectionsRepository.getByGuid(connectionGuid) ?: return
        this.optRichConnection = connection.toRichConnection(keyStoreManager)
        this._consentData = consent
    }

    override fun revokeConsent() {
        val richConnection = optRichConnection ?: return
        val consentID = consentData?.id ?: return
        when (richConnection.connection.apiVersion) {
            API_V2_VERSION -> {
                v2ApiManager.revokeConsent(
                    consentID = consentID,
                    richConnection = richConnection,
                    callback = this
                )
            }
            API_V1_VERSION -> {
                v1ApiManager.revokeConsent(
                    consentId = consentID,
                    connectionAndKey = richConnection,
                    resultCallback = this
                )
            }
        }
    }

    override fun onConsentRevokeFailure(error: ApiErrorData) {
        contract?.onConsentRevokeFailure(error.errorMessage)
    }

    override fun onConsentRevokeSuccess(consentID: ID) {
        contract?.onConsentRevokeSuccess(consentID)
    }
}

interface ConsentDetailsInteractorAbs {
    var contract: ConsentDetailsInteractorCallback?
    val consentData: ConsentData?
    val connectionName: String?
    fun setInitialData(connectionGuid: GUID?, consent: ConsentData?)
    fun revokeConsent()
}

interface ConsentDetailsInteractorCallback {
    fun onConsentRevokeFailure(error: String)
    fun onConsentRevokeSuccess(consentID: ID)
}
