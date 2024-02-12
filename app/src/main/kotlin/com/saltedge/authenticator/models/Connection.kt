/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.models

import androidx.annotation.Keep
import com.saltedge.authenticator.core.model.ConnectionAbs
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Connection : RealmObject(), ConnectionAbs {
    @PrimaryKey @Keep @Required
    override var guid: String = ""
    @Keep @Required
    override var id: String = ""
    @Keep
    override var createdAt: Long = 0L
    @Keep
    override var updatedAt: Long = 0L

    @Keep @Required
    override var name: String = ""
    @Keep @Required
    override var code: String = ""
    @Keep @Required
    override var connectUrl: String = ""
    @Keep @Required
    override var logoUrl: String = ""
    @Keep @Required
    override var accessToken: String = ""
    @Keep @Required
    override var status: String = ""
    @Keep
    override var supportEmail: String? = null
    @Keep
    override var consentManagementSupported: Boolean? = null
    @Keep
    override var geolocationRequired: Boolean? = null
    @Keep @Required
    override var providerRsaPublicKeyPem: String = ""
    @Keep @Required
    override var apiVersion: String = "1"
    @Keep
    override var pushToken: String? = null

    val isV2Api: Boolean
        get() = apiVersion == "2"

    override fun toString(): String {
        return "Connection(guid='$guid', id='$id', createdAt=$createdAt, updatedAt=$updatedAt, name='$name', code='$code', connectUrl='$connectUrl', logoUrl='$logoUrl', accessToken='$accessToken', status='$status', supportEmail=$supportEmail, consentManagementSupported=$consentManagementSupported, geolocationRequired=$geolocationRequired, providerRsaPublicKeyPem='$providerRsaPublicKeyPem', apiVersion='$apiVersion', pushToken='$pushToken')"
    }
}
