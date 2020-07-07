/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.consents.list

import android.text.Spanned

class ConsentItemViewModel(
    val id: String,
    var tppName: String,
    var consentTypeDescription: String,
    var expiresAtDescription: Spanned
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConsentItemViewModel

        if (id != other.id) return false
        if (tppName != other.tppName) return false
        if (consentTypeDescription != other.consentTypeDescription) return false
        if (expiresAtDescription.toString() != other.expiresAtDescription.toString()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + tppName.hashCode()
        result = 31 * result + consentTypeDescription.hashCode()
        result = 31 * result + expiresAtDescription.toString().hashCode()
        return result
    }

    override fun toString(): String {
        return "ConsentItemViewModel(id='$id', tppName='$tppName'," +
            " consentTypeDescription='$consentTypeDescription', expiresAtDescription=$expiresAtDescription)"
    }
}
