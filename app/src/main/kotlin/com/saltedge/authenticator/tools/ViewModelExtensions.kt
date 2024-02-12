/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import androidx.lifecycle.MutableLiveData
import com.saltedge.authenticator.models.ViewModelEvent

fun MutableLiveData<ViewModelEvent<Unit>>.postUnitEvent() {
    this.postValue(ViewModelEvent(Unit))
}
