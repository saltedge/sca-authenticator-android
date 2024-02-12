/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationRequest
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.toAuthorizationsConfirmUrl
import com.saltedge.authenticator.sdk.v2.api.retrofit.toAuthorizationsDenyUrl
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader

internal abstract class AuthorizationUpdateBaseConnector(
    val authorizationId: ID,
    val isConfirmRequest: Boolean
) : ApiResponseInterceptor<UpdateAuthorizationResponse>() {

    protected fun url(richConnection: RichConnection): String {
        val baseUrl = richConnection.connection.connectUrl
        return if (isConfirmRequest) baseUrl.toAuthorizationsConfirmUrl(authorizationId)
        else baseUrl.toAuthorizationsDenyUrl(authorizationId)
    }

    protected fun headers(richConnection: RichConnection, request: UpdateAuthorizationRequest): Map<String, String> {
        return createAccessTokenHeader(richConnection.connection.accessToken).addSignatureHeader(
            richConnection.private,
            request.data,
            request.requestExpirationTime
        )
    }

    protected fun body(encryptedPayload: EncryptedBundle) = UpdateAuthorizationRequest(data = encryptedPayload)
}
