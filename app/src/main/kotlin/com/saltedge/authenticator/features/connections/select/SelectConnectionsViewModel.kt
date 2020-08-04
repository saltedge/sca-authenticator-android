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
package com.saltedge.authenticator.features.connections.select

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.features.connections.common.ConnectionItemViewModel
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.sdk.model.GUID

class SelectConnectionsViewModel : ViewModel(), LifecycleObserver {

    val listItems = MutableLiveData<List<ConnectionItemViewModel>>()
    val listItemsValues: List<ConnectionItemViewModel>
        get() = listItems.value ?: emptyList()
    val onListItemClickEvent = MutableLiveData<ViewModelEvent<Int>>()
    val onProceedClickEvent = MutableLiveData<GUID>()

    fun setInitialData(data: List<ConnectionItemViewModel>) {
        listItems.postValue(data)
    }

    fun onListItemClick(itemIndex: Int) {
        onListItemClickEvent.postValue(ViewModelEvent(itemIndex))
    }

    fun changeStateItem(item: ConnectionItemViewModel) {
        listItemsValues.forEach { it.isChecked = false }
        item.isChecked = true
    }

    fun proceedConnection(guid: GUID) {
        onProceedClickEvent.postValue(guid)
    }
}
