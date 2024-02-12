/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.common

import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.tools.ResId

data class ConnectionItem(
    val guid: String,
    val connectionId: String,
    var name: String,
    val logoUrl: String,
    val email: String?,
    var statusDescription: String,
    var statusDescriptionColorRes: ResId,
    val isActive: Boolean,
    var isChecked: Boolean,
    val apiVersion: String,
    val shouldRequestLocationPermission: Boolean,
    var consentsCount: Int = 0
) {
    val isV2Api: Boolean
        get() = apiVersion == API_V2_VERSION
}
