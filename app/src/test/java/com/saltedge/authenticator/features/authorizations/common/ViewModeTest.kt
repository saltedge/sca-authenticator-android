/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.R
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.*
import org.junit.Test

class ViewModeTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        assertThat(AuthorizationStatus.values(), equalTo(arrayOf(
            AuthorizationStatus.LOADING,
            AuthorizationStatus.PENDING,
            AuthorizationStatus.CONFIRM_PROCESSING,
            AuthorizationStatus.DENY_PROCESSING,
            AuthorizationStatus.CONFIRMED,
            AuthorizationStatus.DENIED,
            AuthorizationStatus.ERROR,
            AuthorizationStatus.TIME_OUT,
            AuthorizationStatus.UNAVAILABLE
        )))
    }

    @Test
    @Throws(Exception::class)
    fun isFinalModeTest() {
        assertFalse(AuthorizationStatus.LOADING.isFinal())
        assertFalse(AuthorizationStatus.PENDING.isFinal())
        assertFalse(AuthorizationStatus.CONFIRM_PROCESSING.isFinal())
        assertFalse(AuthorizationStatus.DENY_PROCESSING.isFinal())
        assertTrue(AuthorizationStatus.CONFIRMED.isFinal())
        assertTrue(AuthorizationStatus.DENIED.isFinal())
        assertTrue(AuthorizationStatus.ERROR.isFinal())
        assertTrue(AuthorizationStatus.TIME_OUT.isFinal())
        assertTrue(AuthorizationStatus.UNAVAILABLE.isFinal())
    }

    @Test
    @Throws(Exception::class)
    fun isProcessingModeTest() {
        assertFalse(AuthorizationStatus.LOADING.isProcessing())
        assertFalse(AuthorizationStatus.PENDING.isProcessing())
        assertTrue(AuthorizationStatus.CONFIRM_PROCESSING.isProcessing())
        assertTrue(AuthorizationStatus.DENY_PROCESSING.isProcessing())
        assertFalse(AuthorizationStatus.CONFIRMED.isProcessing())
        assertFalse(AuthorizationStatus.DENIED.isProcessing())
        assertFalse(AuthorizationStatus.ERROR.isProcessing())
        assertFalse(AuthorizationStatus.TIME_OUT.isProcessing())
        assertFalse(AuthorizationStatus.UNAVAILABLE.isProcessing())
    }

    @Test
    @Throws(Exception::class)
    fun statusImageResIdTest() {
        assertNull(AuthorizationStatus.LOADING.statusImageResId)
        assertNull(AuthorizationStatus.PENDING.statusImageResId)
        assertNull(AuthorizationStatus.CONFIRM_PROCESSING.statusImageResId)
        assertNull(AuthorizationStatus.DENY_PROCESSING.statusImageResId)
        assertThat(AuthorizationStatus.CONFIRMED.statusImageResId, equalTo(R.drawable.ic_status_success))
        assertThat(AuthorizationStatus.DENIED.statusImageResId, equalTo(R.drawable.ic_status_denied))
        assertThat(AuthorizationStatus.ERROR.statusImageResId, equalTo(R.drawable.ic_status_error))
        assertThat(AuthorizationStatus.TIME_OUT.statusImageResId, equalTo(R.drawable.ic_status_timeout))
        assertThat(AuthorizationStatus.UNAVAILABLE.statusImageResId, equalTo(R.drawable.ic_status_unavailable))
    }

    @Test
    @Throws(Exception::class)
    fun statusTitleResIdTest() {
        assertThat(AuthorizationStatus.LOADING.statusTitleResId, equalTo(R.string.authorizations_loading))
        assertThat(AuthorizationStatus.PENDING.statusTitleResId, equalTo(R.string.authorizations_loading))
        assertThat(AuthorizationStatus.CONFIRM_PROCESSING.statusTitleResId, equalTo(R.string.authorizations_processing))
        assertThat(AuthorizationStatus.DENY_PROCESSING.statusTitleResId, equalTo(R.string.authorizations_processing))
        assertThat(AuthorizationStatus.CONFIRMED.statusTitleResId, equalTo(R.string.authorizations_confirmed))
        assertThat(AuthorizationStatus.DENIED.statusTitleResId, equalTo(R.string.authorizations_denied))
        assertThat(AuthorizationStatus.ERROR.statusTitleResId, equalTo(R.string.authorizations_error))
        assertThat(AuthorizationStatus.TIME_OUT.statusTitleResId, equalTo(R.string.authorizations_time_out))
        assertThat(AuthorizationStatus.UNAVAILABLE.statusTitleResId, equalTo(R.string.authorizations_unavailable))
    }

    @Test
    @Throws(Exception::class)
    fun statusDescriptionResIdTest() {
        assertThat(AuthorizationStatus.LOADING.statusDescriptionResId, equalTo(R.string.authorizations_loading_description))
        assertThat(AuthorizationStatus.PENDING.statusDescriptionResId, equalTo(R.string.authorizations_loading_description))
        assertThat(AuthorizationStatus.CONFIRM_PROCESSING.statusDescriptionResId, equalTo(R.string.authorizations_processing_description))
        assertThat(AuthorizationStatus.DENY_PROCESSING.statusDescriptionResId, equalTo(R.string.authorizations_processing_description))
        assertThat(AuthorizationStatus.CONFIRMED.statusDescriptionResId, equalTo(R.string.authorizations_confirmed_description))
        assertThat(AuthorizationStatus.DENIED.statusDescriptionResId, equalTo(R.string.authorizations_denied_description))
        assertThat(AuthorizationStatus.ERROR.statusDescriptionResId, equalTo(R.string.authorizations_error_description))
        assertThat(AuthorizationStatus.TIME_OUT.statusDescriptionResId, equalTo(R.string.authorizations_time_out_description))
        assertThat(AuthorizationStatus.UNAVAILABLE.statusDescriptionResId, equalTo(R.string.authorizations_unavailable_description))
    }

    @Test
    @Throws(Exception::class)
    fun showProgressTest() {
        assertTrue(AuthorizationStatus.LOADING.processingMode)
        assertFalse(AuthorizationStatus.PENDING.processingMode)
        assertTrue(AuthorizationStatus.CONFIRM_PROCESSING.processingMode)
        assertTrue(AuthorizationStatus.DENY_PROCESSING.processingMode)
        assertFalse(AuthorizationStatus.CONFIRMED.processingMode)
        assertFalse(AuthorizationStatus.DENIED.processingMode)
        assertFalse(AuthorizationStatus.ERROR.processingMode)
        assertFalse(AuthorizationStatus.TIME_OUT.processingMode)
        assertFalse(AuthorizationStatus.UNAVAILABLE.processingMode)
    }

    @Test
    @Throws(Exception::class)
    fun isClosedTest() {
        assertTrue("closed".isClosed)
        assertFalse("pending".isClosed)
        assertFalse("confirmed".isClosed)
        assertFalse("denied".isClosed)
        assertFalse("deny_processing".isClosed)
        assertFalse("confirm_processing".isClosed)
    }
}
