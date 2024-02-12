/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.select

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.features.connections.common.ConnectionItem
import com.saltedge.authenticator.models.ViewModelEvent

class SelectConnectionsViewModel : ViewModel(), LifecycleObserver {

    val listItems = MutableLiveData<List<ConnectionItem>>()
    val listItemsValues: List<ConnectionItem>
        get() = listItems.value ?: emptyList()
    val onListItemClickEvent = MutableLiveData<ViewModelEvent<Int>>()
    val onProceedClickEvent = MutableLiveData<GUID>()

    fun setInitialData(data: List<ConnectionItem>) {
        listItems.postValue(data)
    }

    fun onListItemClick(itemIndex: Int) {
        onListItemClickEvent.postValue(ViewModelEvent(itemIndex))
    }

    fun changeStateItem(item: ConnectionItem) {
        listItemsValues.forEach { it.isChecked = false }
        item.isChecked = true
    }

    fun proceedConnection(guid: GUID) {
        onProceedClickEvent.postValue(guid)
    }
}
