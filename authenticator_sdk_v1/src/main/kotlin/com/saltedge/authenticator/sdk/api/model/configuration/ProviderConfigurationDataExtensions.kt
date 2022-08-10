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
        Timber.wtf(e, this.code, null)
        false
    }
}
