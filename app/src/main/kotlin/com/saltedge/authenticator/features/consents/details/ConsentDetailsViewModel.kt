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
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.features.consents.common.countOfDays
import com.saltedge.authenticator.features.consents.common.countOfDaysLeft
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.constants.KEY_DATA
import com.saltedge.authenticator.sdk.contract.ConsentRevokeListener
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.response.ConsentRevokeResponseData
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.tools.daysTillExpire
import com.saltedge.authenticator.tools.toDateFormatString
import org.joda.time.DateTime

class ConsentDetailsViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyStoreManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs
) : ViewModel(), ConsentRevokeListener {

    val fragmentTitle = MutableLiveData<String>(appContext.getString(R.string.consent_details_feature_title))
    val daysLeft = MutableLiveData<String>("")
    val consentTitle = MutableLiveData<String>("")
    val consentDescription = MutableLiveData<String>("")
    val consentGranted = MutableLiveData<String>("")
    val consentExpires = MutableLiveData<String>("")
    val accounts = MutableLiveData<List<AccountData>?>()
    val sharedDataVisibility = MutableLiveData<Int>(View.GONE)
    val sharedBalanceVisibility = MutableLiveData<Int>(View.GONE)
    val sharedTransactionsVisibility = MutableLiveData<Int>(View.GONE)
    val revokeAlertEvent = MutableLiveData<ViewModelEvent<String>>()
    val revokeErrorEvent = MutableLiveData<ViewModelEvent<String>>()
    val revokeSuccessEvent = MutableLiveData<ViewModelEvent<String>>()
    private var consentData: ConsentData? = null
    private var connectionAndKey: ConnectionAndKey? = null

    override fun onConsentRevokeFailure(error: ApiErrorData) {
        revokeErrorEvent.postValue(ViewModelEvent(error.errorMessage))
    }

    override fun onConsentRevokeSuccess(result: ConsentRevokeResponseData) {
        result.consentId?.let { revokeSuccessEvent.postValue(ViewModelEvent(it)) }
    }

    fun setInitialData(data: ConsentData?, connectionGuid: GUID) {
        val connection = connectionsRepository.getByGuid(connectionGuid) ?: return
        connectionAndKey = keyStoreManager.createConnectionAndKeyModel(connection)
        fragmentTitle.postValue(connection.name)
        this.consentData = data
        data?.let { consent ->
            fragmentTitle.postValue(consent.tppName)
            daysLeft.postValue(countOfDaysLeft(consent.expiresAt.daysTillExpire(), appContext))
            consentTitle.postValue(getConsentTitle(consent.consentTypeString))
            consentDescription.postValue(getConsentDescription(consent.tppName, connection.name))
            consentGranted.postValue(getGrantedDate(consent.createdAt))
            consentExpires.postValue(getExpiresDate(consent.expiresAt))
            accounts.postValue(data.accounts)
            sharedDataVisibility.postValue(if (data.sharedData == null) View.GONE else View.VISIBLE)
            sharedBalanceVisibility.postValue(
                if (data.sharedData?.balance == true) View.VISIBLE else View.GONE
            )
            sharedTransactionsVisibility.postValue(
                if (data.sharedData?.transactions == true) View.VISIBLE else View.GONE
            )
        }
    }

    fun onRevokeClick() {
        val template = appContext.getString(R.string.revoke_consent_message)
        revokeAlertEvent.postValue(ViewModelEvent(String.format(template, fragmentTitle.value)))
    }

    fun onRevokeConfirmed() {
        apiManager.revokeConsent(
            consentId = consentData?.id ?: return,
            connectionAndKey = connectionAndKey ?: return,
            resultCallback = this
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
        return "${appContext.getString(R.string.granted)}: ${grantedAt.toDateFormatString(appContext)}"
    }

    private fun getExpiresDate(expiresAt: DateTime): String {
        return "${appContext.getString(R.string.expires)}: ${expiresAt.toDateFormatString(appContext)}"
    }

    companion object {
        val Bundle.consents: ConsentData?
            get() = getSerializable(KEY_DATA) as? ConsentData

        fun newBundle(connectionGuid: GUID, consents: ConsentData): Bundle {
            return Bundle().apply {
                putString(KEY_GUID, connectionGuid)
                putSerializable(KEY_DATA, consents)
            }
        }
    }
}
