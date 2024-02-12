/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.common

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.model.ConsentType
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.isActive
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.v2.ScaServiceClientAbs
import com.saltedge.authenticator.sdk.v2.api.API_V2_VERSION
import com.saltedge.authenticator.sdk.v2.api.contract.FetchConsentsListener

fun ConsentType?.toConsentTypeDescription(context: Context?): String {
    return context?.getString(when (this) {
        ConsentType.AISP -> R.string.consent_title_aisp
        ConsentType.PISP_FUTURE -> R.string.consent_title_pisp_future
        ConsentType.PISP_RECURRING -> R.string.consent_title_pisp_recurring
        else -> R.string.consent_unknown
    }) ?: ""
}

/**
 * Get size of collection and create string like `%count consents`
 *
 * @receiver collection of consents
 * @param appContext
 * @return spanned string
 */
fun List<ConsentData>.countDescription(appContext: Context): String {
    return if (this.isEmpty()) ""
    else appContext.resources.getQuantityString(
        R.plurals.count_of_consents,
        this.count(),
        this.count()
    )
}

/**
 * Create prefix string for each item of connections list.
 * Prefix has format `%count consents ·`
 *
 * @param count of consents
 * @param appContext
 * @return spanned string
 */
fun consentsCountPrefixForConnection(count: Int, appContext: Context): String {
    if (count < 1) return ""
    val quantityString = appContext.resources.getQuantityString(
        R.plurals.count_of_consents,
        count,
        count
    )
    return "$quantityString\u30FB"
}

/**
 * Create string with count of days like `@count day left`
 *
 * @param countOfDays
 * @param appContext
 * @return string
 */
fun countOfDaysLeft(countOfDays: Int, appContext: Context): String {
    val template = appContext.getString(R.string.days_left)
    return String.format(template, countOfDays(countOfDays, appContext))
}

/**
 * Create string with count of days like `@count day`
 *
 * @param countOfDays
 * @param appContext
 * @return string
 */
fun countOfDays(countOfDays: Int, appContext: Context): String {
    return appContext.resources.getQuantityString(
        R.plurals.count_of_days,
        countOfDays,
        countOfDays
    )
}

fun requestUpdateConsents(
    richConnections: List<RichConnection>,
    v1ApiManager: AuthenticatorApiManagerAbs,
    v2ApiManager: ScaServiceClientAbs,
    v1Callback: FetchEncryptedDataListener,
    v2Callback: FetchConsentsListener
) {
    val splitRichConnections = richConnections
        .filter { it.connection.isActive() }
        .partition { it.connection.apiVersion == API_V2_VERSION }

    val v2RichConnections = splitRichConnections.first
    if (v2RichConnections.isNotEmpty()) {
        v2ApiManager.fetchConsents(richConnections = v2RichConnections, callback = v2Callback)
    }
    val otherRichConnections = splitRichConnections.second
    if (otherRichConnections.isNotEmpty()) {
        v1ApiManager.getConsents(connectionsAndKeys = otherRichConnections, resultCallback = v1Callback)
    }
}

fun List<EncryptedData>.decryptConsents(
    cryptoTools: BaseCryptoToolsAbs,
    richConnections: List<RichConnection>,
    apiVersion: String
): List<ConsentData> {
    return this.mapNotNull { data ->
        richConnections.firstOrNull { it.connection.id == data.connectionId }?.let {
            cryptoTools.decryptConsentData(
                encryptedData = data,
                rsaPrivateKey = it.private,
                connectionGUID = it.connection.guid,
                consentID = if (apiVersion == API_V2_VERSION) data.id else null
            )
        }
    }
}

