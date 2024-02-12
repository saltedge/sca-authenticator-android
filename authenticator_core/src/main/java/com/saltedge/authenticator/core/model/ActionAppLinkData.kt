/*
 * Copyright (c) 2021 Salt Edge Inc.
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
