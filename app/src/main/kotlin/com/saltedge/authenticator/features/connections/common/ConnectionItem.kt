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
