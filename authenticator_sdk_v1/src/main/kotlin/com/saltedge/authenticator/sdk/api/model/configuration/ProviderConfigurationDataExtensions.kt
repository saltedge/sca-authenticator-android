/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.configuration

import com.saltedge.authenticator.sdk.constants.API_V1_VERSION
import timber.log.Timber

/**
 * Checks validity of Provider model
 *
 * @receiver Encrypted authorization
 * @return boolean. true if `name`, `code` and `connectUrl` fields are present
 * and version code equal to API_VERSION constant
 */
fun ProviderConfigurationData.isValid(): Boolean {
    return try {
        name.isNotEmpty() &&
            code.isNotEmpty() &&
            connectUrl.isNotEmpty() &&
            !connectUrl.contains("/localhost") &&
            version == API_V1_VERSION
    } catch (e: Exception) {
        if (this.code.isNullOrEmpty()) Timber.e(e)
        else Timber.wtf(e, this.code, null)
        false
    }
}
