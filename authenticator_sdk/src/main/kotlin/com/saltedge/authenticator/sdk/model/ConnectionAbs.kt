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
package com.saltedge.authenticator.sdk.model

/**
 * Connection model abstraction
 *
 * @property guid - Alias to RSA keypair in Keystore
 * @property id - Unique id received from Authenticator API
 * @property name - Provider's name from ProviderData
 * @property code - Provider's code
 * @property logoUrl - Provider's logo url. May be empty
 * @property connectUrl - Base url of Authenticator API
 * @property accessToken - Access token for accessing Authenticator API resources
 * @property status - Connection Status. ACTIVE or INACTIVE
 *
 * @see ProviderData
 */
interface ConnectionAbs {
    var guid: String
    var id: String
    var createdAt: Long
    var updatedAt: Long
    var name: String
    var code: String
    var logoUrl: String
    var connectUrl: String
    var accessToken: String
    var status: String
}
