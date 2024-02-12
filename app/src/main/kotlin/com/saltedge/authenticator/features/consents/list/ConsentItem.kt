/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.list

import android.text.Spanned

class ConsentItem(
    val id: String,
    var tppName: String,
    var consentTypeDescription: String,
    var expiresAtDescription: Spanned
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConsentItem

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
