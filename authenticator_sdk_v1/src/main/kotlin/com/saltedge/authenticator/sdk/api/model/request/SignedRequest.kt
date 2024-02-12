/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.request

internal data class SignedRequest(
    val requestUrl: String,
    val headersMap: Map<String, String>
)
