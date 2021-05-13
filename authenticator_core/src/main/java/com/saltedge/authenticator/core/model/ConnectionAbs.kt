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

/**
 * Connection model abstraction
 *
 * @property guid - unique alias of RSA key in Keystore
 * @property id - unique Connection ID in SCA Service
 * @property name - name of Provider from Configuration
 * @property code - unique code of Provider (Provider ID) from Configuration
 * @property logoUrl - logo URL of Provider. May be empty
 * @property connectUrl - base URL of SCA Service
 * @property accessToken - access token for accessing SCA Service resources
 * @property status - connection Status (ACTIVE or INACTIVE)
 * @property supportEmail - support email if Provider from Configuration
 * @property consentManagementSupported - consent management is supported by Provider or not. Flag from Configuration
 * @property geolocationRequired - collection of geolocation data is mandatory or not. Flag from Configuration
 * @property providerRsaPublicKeyPem - asymmetric RSA Public Key (in PEM format) linked to the Provider
 */
interface ConnectionAbs {
    var guid: GUID
    var id: String
    var createdAt: Long
    var updatedAt: Long
    var name: String
    var code: String
    var logoUrl: String
    var connectUrl: String
    var accessToken: String
    var status: String
    var supportEmail: String?
    var consentManagementSupported: Boolean?
    var geolocationRequired: Boolean?
    var providerRsaPublicKeyPem: String
    var apiVersion: String
}
