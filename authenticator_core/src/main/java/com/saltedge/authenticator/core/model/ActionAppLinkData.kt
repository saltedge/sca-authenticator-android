/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.core.model

import android.net.Uri
import androidx.annotation.Keep
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.model.ID
import java.io.Serializable

@Keep
data class ActionAppLinkData(
    var apiVersion: String,
    var actionIdentifier: ID,
    var providerID: ID?,
    var connectUrl: String?,
    var returnTo: String?
) : Serializable

/**
 * Extract Instant Action data from App Link
 *
 * @receiver deep link String (e.g. authenticator://saltedge.com/action?action_uuid=123456&return_to=https://return.com&connect_url=https://someurl.com)
 * @return ActionAppLinkData object
 */
fun String.extractActionAppLinkData(): ActionAppLinkData? {
    val uri = Uri.parse(this)
    val actionIdentifier = uri.getQueryParameter(KEY_ACTION_UUID) ?: uri.getQueryParameter(KEY_ACTION_ID) ?: return null
    return ActionAppLinkData(
        apiVersion = uri.getQueryParameter(KEY_API_VERSION) ?: "1",
        actionIdentifier = actionIdentifier,
        connectUrl = uri.getQueryParameter(KEY_CONNECT_URL),
        providerID = uri.getQueryParameter(KEY_PROVIDER_ID),
        returnTo = uri.getQueryParameter(KEY_RETURN_TO)
    )
}
