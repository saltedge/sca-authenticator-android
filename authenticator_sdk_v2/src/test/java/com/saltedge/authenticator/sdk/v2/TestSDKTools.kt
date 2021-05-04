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
package com.saltedge.authenticator.sdk.v2

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionV2Abs
import okhttp3.ResponseBody
import retrofit2.Response

object TestTools {

    val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Context>().applicationContext

    fun getString(resId: Int): String {
        return try {
            applicationContext.getString(resId) ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }
}

data class TestConnectionV2(
    override var guid: String = "",
    override var id: String = "",
    override var createdAt: Long = 0L,
    override var updatedAt: Long = 0L,
    override var name: String = "",
    override var code: String = "",
    override var connectUrl: String = "",
    override var logoUrl: String = "",
    override var accessToken: String = "",
    override var status: String = "",
    override var supportEmail: String? = "",
    override var consentManagementSupported: Boolean? = true,
    override var geolocationRequired: Boolean? = true,
    override var providerRsaPublicKeyPem: String = ""
) : ConnectionV2Abs

val defaultTestConnection: ConnectionV2Abs
    get() = TestConnectionV2(id = "333", guid = "test", connectUrl = "https://localhost", accessToken = "accessToken")

@Throws(Exception::class)
fun <T> get404Response(): Response<T> = Response.error(404, ResponseBody.create(null, get404ResponseBody()))

@Throws(Exception::class)
private fun get404ResponseBody(): String = "{\"error_class\": \"NotFound\",\"error_message\": \"Resource not found\"}"
