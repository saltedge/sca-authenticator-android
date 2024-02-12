/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID

/**
 * Consent revoke request result
 */
interface ConsentRevokeListener {
    fun onConsentRevokeSuccess(consentID: ID)
    fun onConsentRevokeFailure(error: ApiErrorData)
}
