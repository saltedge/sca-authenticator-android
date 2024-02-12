/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import com.saltedge.authenticator.core.model.extractActionAppLinkData
import com.saltedge.authenticator.core.model.extractConnectAppLinkData

/**
 * Validates deep link
 *
 * @receiver deep link String (e.g. authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890)
 * @return true if deeplink contains configuration url or action data
 */
fun String.isValidAppLink(): Boolean {
    return this.extractConnectAppLinkData() != null || this.extractActionAppLinkData() != null
}
