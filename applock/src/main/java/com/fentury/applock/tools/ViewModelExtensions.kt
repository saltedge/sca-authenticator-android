/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools

import androidx.lifecycle.MutableLiveData
import com.fentury.applock.models.ViewModelEvent

internal fun MutableLiveData<ViewModelEvent<Unit>>.postUnitEvent() {
    this.postValue(ViewModelEvent(Unit))
}