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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CONSENT_REQUEST_CODE
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConsentType
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.features.consents.common.countDescription
import com.saltedge.authenticator.features.consents.common.countOfDays
import com.saltedge.authenticator.features.consents.details.ConsentDetailsViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.toRichConnection
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import com.saltedge.authenticator.sdk.tools.CryptoToolsV1Abs
import com.saltedge.authenticator.tools.appendColoredText
import com.saltedge.authenticator.tools.daysTillExpire
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime

class ConsentsListViewModel(
    private val appContext: Context,
    private val connectionsRepository: ConnectionsRepositoryAbs,
    private val keyStoreManager: KeyManagerAbs,
    private val apiManager: AuthenticatorApiManagerAbs,
    private val cryptoTools: CryptoToolsV1Abs,
    private val defaultDispatcher: CoroutineDispatcher
) : ViewModel(), LifecycleObserver, FetchEncryptedDataListener {

    val listItems = MutableLiveData<List<ConsentItemViewModel>>()
    val onListItemClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onConsentRemovedEvent = MutableLiveData<ViewModelEvent<String>>()
    val logoUrl = MutableLiveData<String>()
    val connectionTitle = MutableLiveData<String>()
    val consentsCount = MutableLiveData<String>()
    private var consents: List<ConsentData> = emptyList()
    private var richConnection: RichConnection? = null

    override fun onFetchEncryptedDataResult(result: List<EncryptedData>, errors: List<ApiErrorData>) {
        viewModelScope.launch(defaultDispatcher) {
            val decryptedConsents = decryptConsents(encryptedList = result)
            withContext(Dispatchers.Main) { onReceivedNewConsents(result = decryptedConsents) }
        }
    }

    fun setInitialData(bundle: Bundle?) {
        bundle?.guid?.let { connectionGuid ->
            richConnection = connectionsRepository.getByGuid(connectionGuid)?.let {
                logoUrl.postValue(it.logoUrl)
                connectionTitle.postValue(it.name)
                it.toRichConnection(keyStoreManager)
            }
        }
        onReceivedNewConsents(bundle?.consents ?: emptyList())
    }

    fun refreshConsents() {
        richConnection?.let {
            apiManager.getConsents(
                connectionsAndKeys = listOf(it),
                resultCallback = this
            )
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val consentId = data?.getStringExtra(KEY_ID)
        val revokedConsent: ConsentData? = consents.firstOrNull { it.id == consentId }
        if (requestCode == CONSENT_REQUEST_CODE && resultCode == Activity.RESULT_OK && revokedConsent != null) {
            val newConsents: MutableList<ConsentData> = consents.toMutableList()
            newConsents.remove(revokedConsent)
            if (newConsents != consents) {
                val template = appContext.getString(R.string.consent_revoked_for)
                val message = String.format(template, revokedConsent.tppName)
                onConsentRemovedEvent.postValue(ViewModelEvent(message))

                onReceivedNewConsents(newConsents)
            }
        }
    }

    fun revokeConsent(consentId: String?) {
        val revokedConsent: ConsentData? = consents.firstOrNull { it.id == consentId }
        if (revokedConsent != null) {
            val newConsents: MutableList<ConsentData> = consents.toMutableList()
            newConsents.remove(revokedConsent)
            if (newConsents != consents) {
                val template = appContext.getString(R.string.consent_revoked_for)
                val message = String.format(template, revokedConsent.tppName)
                onConsentRemovedEvent.postValue(ViewModelEvent(message))

                onReceivedNewConsents(newConsents)
            }
        }
    }

    fun onListItemClick(itemIndex: Int) {
        val connectionGuid = richConnection?.connection?.guid ?: return
        onListItemClickEvent.postValue(ViewModelEvent(
            ConsentDetailsViewModel.newBundle(connectionGuid, consents[itemIndex])
        ))
    }

    private fun decryptConsents(encryptedList: List<EncryptedData>): List<ConsentData> {
        val key = richConnection?.private ?: return emptyList()
        return encryptedList.mapNotNull {
            cryptoTools.decryptConsentData(encryptedData = it, rsaPrivateKey = key)
        }
    }

    private fun onReceivedNewConsents(result: List<ConsentData>) {
        consents = result
        listItems.postValue(result.toViewModels())
        consentsCount.postValue(consents.countDescription(appContext))
    }

    private fun List<ConsentData>.toViewModels(): List<ConsentItemViewModel> {
        return this.map {
            ConsentItemViewModel(
                id = it.id,
                tppName = it.tppName,
                consentTypeDescription = it.consentType?.toConsentTypeDescription() ?: "",
                expiresAtDescription = it.expiresAt.toExpiresAtDescription()
            )
        }
    }

    private fun ConsentType.toConsentTypeDescription(): String {
        return appContext.getString(when (this) {
            ConsentType.AISP -> R.string.consent_title_aisp
            ConsentType.PISP_FUTURE -> R.string.consent_title_pisp_future
            ConsentType.PISP_RECURRING -> R.string.consent_title_pisp_recurring
        })
    }

    private fun DateTime.toExpiresAtDescription(): Spanned {
        val expiresInString = appContext.getString(R.string.expires_in)
        val daysLeftCount = this.daysTillExpire()
        val daysTillExpireDescription = countOfDays(daysLeftCount, appContext)

        val result = SpannableStringBuilder()
        return if (daysLeftCount > 7) {
            result.appendColoredText(
                "$expiresInString $daysTillExpireDescription",
                R.color.dark_60_and_grey_100,
                appContext
            )
        } else {
            result
                .appendColoredText("$expiresInString ", R.color.dark_60_and_grey_100, appContext)
                .appendColoredText(daysTillExpireDescription, R.color.red_and_red_light, appContext)
        }
    }

    companion object {
        val Bundle.consents: List<ConsentData>?
            get() = getSerializable(KEY_DATA) as? List<ConsentData>

        fun newBundle(connectionGuid: GUID, consents: List<ConsentData>?): Bundle {
            return Bundle().apply {
                putString(KEY_GUID, connectionGuid)
                putSerializable(KEY_DATA, ArrayList<ConsentData>(consents ?: emptyList()))
            }
        }
    }
}
