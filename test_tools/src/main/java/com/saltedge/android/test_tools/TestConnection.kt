/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.android.test_tools

import com.saltedge.authenticator.core.model.ConnectionAbs

data class TestConnection(
    override var guid: String = "",
    override var id: String = "",
    override var createdAt: Long = 0L,
    override var updatedAt: Long = 0L,
    override var name: String = "",
    override var code: String = "",
    override var connectUrl: String = "",
    override var logoUrl: String = "",
    override var accessToken: String = "",
    override var status: String = "",
    override var supportEmail: String? = "",
    override var consentManagementSupported: Boolean? = true,
    override var geolocationRequired: Boolean? = true,
    override var providerRsaPublicKeyPem: String = "",
    override var apiVersion: String = "",
    override var pushToken: String? = ""
) : ConnectionAbs
