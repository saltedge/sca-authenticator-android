/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.consents.list

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.features.consents.common.countDescription
import com.saltedge.authenticator.features.consents.common.countOfDays
import com.saltedge.authenticator.features.consents.common.toConsentTypeDescription
import com.saltedge.authenticator.features.consents.details.ConsentDetailsViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.appendColoredText
import com.saltedge.authenticator.tools.daysTillExpire
import kotlinx.coroutines.CoroutineScope
import org.joda.time.DateTime
import java.lang.ref.WeakReference

class ConsentsListViewModel(
    private val weakContext: WeakReference<Context>,
    private val interactor: ConsentsListInteractorAbs,
) : ViewModel(),
    LifecycleObserver,
    ConsentsListInteractorCallback
{
    val listItems = MutableLiveData<List<ConsentItem>>()
    val onListItemClickEvent = MutableLiveData<ViewModelEvent<Bundle>>()
    val onConsentRemovedEvent = MutableLiveData<ViewModelEvent<String>>()
    val logoUrlData = MutableLiveData<String>()
    val connectionTitleData = MutableLiveData<String>()
    val consentsCount = MutableLiveData<String>()

    override val coroutineScope: CoroutineScope
        get() = viewModelScope

    private val context: Context?
        get() = weakContext.get()

    init {
        interactor.contract = this
    }

    fun setInitialData(bundle: Bundle?) {
        interactor.updateConnection(bundle?.guid)?.let {
            logoUrlData.postValue(it.logoUrl)
            connectionTitleData.postValue(it.name)
        }
        interactor.onNewConsentsReceived(bundle?.consents ?: emptyList())
    }

    fun refreshConsents() {
        interactor.updateConsents()
    }

    fun onListItemClick(itemIndex: Int) {
        val consentId = listItems.value?.getOrNull(itemIndex)?.id ?: return
        val consent: ConsentData = interactor.getConsent(consentId) ?: return
        onListItemClickEvent.postValue(ViewModelEvent(
            ConsentDetailsViewModel.newBundle(consent.connectionGuid, consent)
        ))
    }

    fun onRevokeConsent(consentId: ID?) {
        val removedConsent = interactor.removeConsent(consentId ?: return) ?: return
        context?.let {
            val template = it.getString(R.string.consent_revoked_for)
            val message = String.format(template, removedConsent.tppName)
            onConsentRemovedEvent.postValue(ViewModelEvent(message))
        }
    }

    override fun onDatasetChanged(consents: List<ConsentData>) {
        val items = consents.toViewModels()
        listItems.postValue(items)
        context?.let { consentsCount.postValue(consents.countDescription(it)) }
    }

    private fun List<ConsentData>.toViewModels(): List<ConsentItem> {
        return this.map {
            ConsentItem(
                id = it.id,
                tppName = it.tppName,
                consentTypeDescription = it.consentType?.toConsentTypeDescription(context) ?: "",
                expiresAtDescription = it.expiresAt.toExpiresAtDescription()
            )
        }
    }

    private fun DateTime.toExpiresAtDescription(): Spanned {
        val appContext = context ?: return SpannedString("")
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
            result.appendColoredText("$expiresInString ", R.color.dark_60_and_grey_100, appContext)
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
