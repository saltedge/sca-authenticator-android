/*
 * Copyright (c) 2021 Salt Edge Inc.
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
 * @property pushToken - token which uniquely identifies Mobile Application for the Push Notification system
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
    var pushToken: String?
}
