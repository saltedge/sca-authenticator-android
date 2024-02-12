/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.events

import com.saltedge.authenticator.models.ViewModelEvent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ViewModelEventTest {

    @Test
    @Throws(Exception::class)
    fun getContentIfNotHandledTest() {
        val viewModelEvent = ViewModelEvent(5)

        assertFalse(viewModelEvent.hasBeenHandled)
        assertEquals(5, viewModelEvent.getContentIfNotHandled())
        assertTrue(viewModelEvent.hasBeenHandled)
        assertNull(viewModelEvent.getContentIfNotHandled())
    }
}
