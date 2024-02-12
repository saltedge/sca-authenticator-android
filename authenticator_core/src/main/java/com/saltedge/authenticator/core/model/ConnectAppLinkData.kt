/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.model

import android.net.Uri
import androidx.annotation.Keep
import timber.log.Timber
import java.io.Serializable

@Keep
data class ConnectAppLinkData(
    var configurationUrl: String,
    var connectQuery: String? = null
) : Serializable {
    val apiVersion: String
        get() {
            return try {
                val segments = configurationUrl.split("/")
                val index = segments.indexOf("authenticator") + 1
                val apiVersionValue = segments[index].replace("v", "")
                if (apiVersionValue.isNotEmpty()) apiVersionValue else "1"
            } catch (e: Exception) {
                Timber.e(e)
                "1"
            }
        }

    val isV2Api: Boolean
        get() = apiVersion == "2"
}

const val KEY_CONFIGURATION_PARAM = "configuration"
const val KEY_CONNECT_QUERY_PARAM = "connect_query"

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
