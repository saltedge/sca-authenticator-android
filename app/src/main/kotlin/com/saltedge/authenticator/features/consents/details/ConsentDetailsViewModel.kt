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
package com.saltedge.authenticator.features.consents.details

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.daysTillExpire
import com.saltedge.authenticator.tools.toDateFormatString
import org.joda.time.DateTime

class ConsentDetailsViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel() {
    val fragmentTitle = MutableLiveData<String>(appContext.getString(R.string.consent_details_feature_title))
    val daysLeft = MutableLiveData<String>("")
    val consentTitle = MutableLiveData<String>("")
    val consentDescription = MutableLiveData<String>("")
    val consentGranted = MutableLiveData<String>("")
    val consentExpires = MutableLiveData<String>("")
    val accounts = MutableLiveData<List<AccountData>?>()

    private var connectionAndKey: ConnectionAndKey? = null

    fun setInitialData(data: ConsentData?, connectionGuid: GUID) {
        val connection = connectionsRepository.getByGuid(connectionGuid) ?: return
        connectionAndKey = keyStoreManager.createConnectionAndKeyModel(connection)
        fragmentTitle.postValue(connection.name)
        data?.let { consent ->
            fragmentTitle.postValue(consent.tppName)
            daysLeft.postValue(calculateRemainedDays(consent.expiresAt))
            consentTitle.postValue(getConsentTitle(consent.consentType))
            consentDescription.postValue(getConsentDescription(consent.tppName, connection.name))
            consentGranted.postValue(getGrantedDate(consent.createdAt))
            consentExpires.postValue(getExpiresDate(consent.expiresAt))
            accounts.postValue(data.accounts)
        }
    }

    private fun calculateRemainedDays(expiresAt: DateTime): String {
        val daysLeftCount = expiresAt.daysTillExpire()
        return appContext.resources.getQuantityString(
            R.plurals.count_of_days_left,
            daysLeftCount,
            daysLeftCount
        )
    }

    private fun getConsentTitle(consentTypeString: String): String {
        return appContext.getString(when (consentTypeString.toConsentType()) {
            ConsentType.AISP -> R.string.consent_title_aisp
            ConsentType.PISP_FUTURE -> R.string.consent_title_pisp_future
            ConsentType.PISP_RECURRING -> R.string.consent_title_pisp_recurring
            else -> R.string.consent_unknown
        })
    }

    private fun getConsentDescription(tppName: String, providerName: String): String {
        return String.format(appContext.getString(R.string.consent_granted_to), tppName, providerName)
    }

    private fun getGrantedDate(grantedAt: DateTime): String {
        return "${appContext.getString(R.string.consent_granted)}: ${grantedAt.toDateFormatString(appContext)}"
    }

    private fun getExpiresDate(expiresAt: DateTime): String {
        return "${appContext.getString(R.string.consent_expires)}: ${expiresAt.toDateFormatString(appContext)}"
    }
}
