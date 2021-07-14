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
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.model.AccountData
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.features.consents.common.countOfDaysLeft
import com.saltedge.authenticator.features.consents.common.toConsentTypeDescription
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.daysTillExpire
import com.saltedge.authenticator.tools.mediumTypefaceSpan
import com.saltedge.authenticator.tools.toDateFormatString
import org.joda.time.DateTime
import java.lang.ref.WeakReference

class ConsentDetailsViewModel(
    private val weakContext: WeakReference<Context>,
    private val interactor: ConsentDetailsInteractorAbs
) : ViewModel(), ConsentDetailsInteractorCallback {

    private val context: Context?
        get() = weakContext.get()
    val fragmentTitle = MutableLiveData<String>("")
    val daysLeft = MutableLiveData<String>("")
    val consentTitle = MutableLiveData<String>("")
    val consentDescription = MutableLiveData<Spanned>(SpannableStringBuilder(""))
    val consentGranted = MutableLiveData<String>("")
    val consentExpires = MutableLiveData<String>("")
    val accounts = MutableLiveData<List<AccountData>?>()
    val sharedDataVisibility = MutableLiveData<Int>(View.GONE)
    val sharedBalanceVisibility = MutableLiveData<Int>(View.GONE)
    val sharedTransactionsVisibility = MutableLiveData<Int>(View.GONE)
    val revokeQuestionEvent = MutableLiveData<ViewModelEvent<String>>()
    val revokeErrorEvent = MutableLiveData<ViewModelEvent<String>>()
    val revokeSuccessEvent = MutableLiveData<ViewModelEvent<String>>()

    init {
        context?.getString(R.string.consent_details_feature_title)?.let {
            fragmentTitle.postValue(it)
        }
        interactor.contract = this
    }

    fun setInitialData(arguments: Bundle?) {
        interactor.setInitialData(connectionGuid = arguments?.guid, consent = arguments?.consent)

        val connectionName = interactor.connectionName ?: return
        fragmentTitle.postValue(connectionName)
        interactor.consentData?.let { consent ->
            fragmentTitle.postValue(consent.tppName)
            context?.let { daysLeft.postValue(countOfDaysLeft(consent.expiresAt.daysTillExpire(), it)) }
            consentTitle.postValue(consent.consentType?.toConsentTypeDescription(context))
            consentDescription.postValue(getConsentDescription(consent.tppName, connectionName))
            consentGranted.postValue(getGrantedDate(consent.createdAt))
            consentExpires.postValue(getExpiresDate(consent.expiresAt))
            accounts.postValue(consent.accounts)
            sharedDataVisibility.postValue(if (consent.sharedData == null) View.GONE else View.VISIBLE)
            sharedBalanceVisibility.postValue(
                if (consent.sharedData?.balance == true) View.VISIBLE else View.GONE
            )
            sharedTransactionsVisibility.postValue(
                if (consent.sharedData?.transactions == true) View.VISIBLE else View.GONE
            )
        }
    }

    fun onRevokeActionClick() {
        context?.let {
            val template = it.getString(R.string.revoke_consent_message)
            val tppName = interactor.consentData?.tppName ?: ""
            revokeQuestionEvent.postValue(ViewModelEvent(String.format(template, tppName)))
        }
    }

    fun onRevokeConfirmedByUser() {
        interactor.revokeConsent()
    }

    override fun onConsentRevokeFailure(error: String) {
        revokeErrorEvent.postValue(ViewModelEvent(error))
    }

    override fun onConsentRevokeSuccess(consentID: ID) {
        revokeSuccessEvent.postValue(ViewModelEvent(consentID))
    }

    private fun getConsentDescription(tppName: String, providerName: String): Spanned {
        val appContext = context ?: return SpannedString("")
        val template = appContext.getString(R.string.consent_granted_to)
        val consentDescription = String.format(template, tppName, providerName)
        val tppNameIndex = consentDescription.indexOf(tppName, 0)
        val providerNameIndex = consentDescription.indexOf(providerName, 0)
        val spannedDescription = SpannableStringBuilder(consentDescription)
        spannedDescription.apply {
            if (tppName.isNotEmpty()) {
                setSpan(
                    appContext.mediumTypefaceSpan, tppNameIndex, tppNameIndex + tppName.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (providerName.isNotEmpty()) {
                setSpan(
                    appContext.mediumTypefaceSpan,
                    providerNameIndex,
                    providerNameIndex + providerName.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannedDescription
    }

    private fun getGrantedDate(grantedAt: DateTime): String {
        return context?.let {
            "${it.getString(R.string.granted)}: ${grantedAt.toDateFormatString(it)}"
        } ?: ""
    }

    private fun getExpiresDate(expiresAt: DateTime): String {
        return context?.let {
            "${it.getString(R.string.expires)}: ${expiresAt.toDateFormatString(it)}"
        } ?: ""
    }

    companion object {
        val Bundle.consent: ConsentData?
            get() = getSerializable(KEY_DATA) as? ConsentData

        fun newBundle(connectionGuid: GUID, consent: ConsentData): Bundle {
            return Bundle().apply {
                putString(KEY_GUID, connectionGuid)
                putSerializable(KEY_DATA, consent)
            }
        }
    }
}
