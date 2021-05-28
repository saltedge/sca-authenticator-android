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

    override fun toString(): String {
        return "Connection(guid='$guid', id='$id', createdAt=$createdAt, updatedAt=$updatedAt, name='$name', code='$code', connectUrl='$connectUrl', logoUrl='$logoUrl', accessToken='$accessToken', status='$status', supportEmail=$supportEmail, consentManagementSupported=$consentManagementSupported, geolocationRequired=$geolocationRequired, providerRsaPublicKeyPem='$providerRsaPublicKeyPem', apiVersion='$apiVersion')"
    }
}
