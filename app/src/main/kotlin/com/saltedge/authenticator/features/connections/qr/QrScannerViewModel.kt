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
package com.saltedge.authenticator.features.connections.qr

import android.content.Context
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.vision.barcode.Barcode
import com.saltedge.authenticator.R
import com.saltedge.authenticator.events.ViewModelEvent
import com.saltedge.authenticator.sdk.tools.isValidDeeplink
import com.saltedge.authenticator.tool.ResId

class QrScannerViewModel(val appContext: Context) : ViewModel(), LifecycleObserver {

    var closeActivity = MutableLiveData<ViewModelEvent<Unit>>()
        private set
    var returnResultAndFinish = MutableLiveData<String>()
        private set
    var showError = MutableLiveData<Int?>()
        private set

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.closeImageView -> closeActivity.postValue(ViewModelEvent(Unit))
        }
    }

    fun onReceivedCodes(codes: SparseArray<Barcode>) {
        val deeplinks = mutableListOf<String>()
        codes.forEach { _, value ->
            if (value.displayValue.isValidDeeplink()) {
                deeplinks.add(value.displayValue)
            }
        }
        deeplinks.firstOrNull()?.let { deeplink ->
            returnResultAndFinish.postValue(deeplink)
        }
    }

    fun showErrorMessage(errorName: ResId?) {
        showError.postValue(errorName)
    }
}
