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

const val KEY_CONFIGURATION_PARAM = "configuration"
const val KEY_CONNECT_QUERY_PARAM = "connect_query"

/**
 * Validates deep link
 *
 * @receiver deep link String (e.g. authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890)
 * @return true if deeplink contains configuration url
 */
fun String.isValidDeeplink(): Boolean {
    return this.extractConnectConfigurationLink() != null
}

/**
 * Extract configuration link from deep link
 *
 * @receiver deep link String (authenticator://saltedge.com/connect?configuration=https://my_host.orj/configuration)
 * @return configuration url string (https://my_host.orj/configuration)
 */
fun String.extractConnectConfigurationLink(): String? {
    val link = Uri.parse(this).getQueryParameter(KEY_CONFIGURATION_PARAM)
    return if (link.contains("//localhost")) null else link
}

/**
 * Extract connect query data from deep link
 *
 * @receiver deep link String (e.g. authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890)
 * @return connect query string (e.g. 1234567890)
 */
fun String.extractConnectQuery(): String? {
    return Uri.parse(this).getQueryParameter(KEY_CONNECT_QUERY_PARAM)
}
