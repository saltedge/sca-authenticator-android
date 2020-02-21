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
package com.saltedge.authenticator.sdk.tools

import android.net.Uri
import com.saltedge.authenticator.sdk.model.appLink.ActionAppLinkData
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData

const val KEY_CONFIGURATION_PARAM = "configuration"
const val KEY_CONNECT_QUERY_PARAM = "connect_query"
const val KEY_ACTION_UUID_PARAM = "action_uuid"
const val KEY_CONNECT_URL_PARAM = "connect_url"
const val KEY_RETURN_TO_PARAM = "return_to"

/**
 * Validates deep link
 *
 * @receiver deep link String (e.g. authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890)
 * @return true if deeplink contains configuration url
 */
fun String.isValidDeeplink(): Boolean {
    return this.extractActionAppLinkData() != null || this.extractConnectAppLinkData() != null
}

/**
 * Extract connection initiation data from App Link
 *
 * @receiver deep link String (e.g. authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890)
 * @return ConnectAppLinkData object
 */
fun String.extractConnectAppLinkData(): ConnectAppLinkData? {
    val uri = Uri.parse(this)
    val configurationUrl = uri.getQueryParameter(KEY_CONFIGURATION_PARAM) ?: return null
    val configurationUri = Uri.parse(configurationUrl)
    return if (configurationUri.host?.contains(".") == true) {
        ConnectAppLinkData(
            configurationUrl = configurationUrl,
            connectQuery = uri.getQueryParameter(KEY_CONNECT_QUERY_PARAM)
        )
    } else null
}

/**
 * Extract action data from App Link
 *
 * @receiver deep link String (e.g. authenticator://saltedge.com/action?action_uuid=123456&return_to=http://return.com&connect_url=http://someurl.com)
 * @return ActionAppLinkData object
 */
fun String.extractActionAppLinkData(): ActionAppLinkData? {
    val uri = Uri.parse(this)
    return ActionAppLinkData(
        actionUuid = uri.getQueryParameter(KEY_ACTION_UUID_PARAM) ?: return null,
        connectUrl = uri.getQueryParameter(KEY_CONNECT_URL_PARAM) ?: return null,
        returnTo = uri.getQueryParameter(KEY_RETURN_TO_PARAM)
    )
}
