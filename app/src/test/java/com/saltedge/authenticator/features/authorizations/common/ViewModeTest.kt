/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.R
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
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
        Assert.assertFalse(AuthorizationStatus.LOADING.isFinalStatus())
        Assert.assertFalse(AuthorizationStatus.PENDING.isFinalStatus())
        Assert.assertFalse(AuthorizationStatus.CONFIRM_PROCESSING.isFinalStatus())
        Assert.assertFalse(AuthorizationStatus.DENY_PROCESSING.isFinalStatus())
        Assert.assertTrue(AuthorizationStatus.CONFIRMED.isFinalStatus())
        Assert.assertTrue(AuthorizationStatus.DENIED.isFinalStatus())
        Assert.assertTrue(AuthorizationStatus.ERROR.isFinalStatus())
        Assert.assertTrue(AuthorizationStatus.TIME_OUT.isFinalStatus())
        Assert.assertTrue(AuthorizationStatus.UNAVAILABLE.isFinalStatus())
    }

    @Test
    @Throws(Exception::class)
    fun isProcessingModeTest() {
        Assert.assertFalse(AuthorizationStatus.LOADING.isProcessingMode())
        Assert.assertFalse(AuthorizationStatus.PENDING.isProcessingMode())
        Assert.assertTrue(AuthorizationStatus.CONFIRM_PROCESSING.isProcessingMode())
        Assert.assertTrue(AuthorizationStatus.DENY_PROCESSING.isProcessingMode())
        Assert.assertFalse(AuthorizationStatus.CONFIRMED.isProcessingMode())
        Assert.assertFalse(AuthorizationStatus.DENIED.isProcessingMode())
        Assert.assertFalse(AuthorizationStatus.ERROR.isProcessingMode())
        Assert.assertFalse(AuthorizationStatus.TIME_OUT.isProcessingMode())
        Assert.assertFalse(AuthorizationStatus.UNAVAILABLE.isProcessingMode())
    }

    @Test
    @Throws(Exception::class)
    fun statusImageResIdTest() {
        Assert.assertNull(AuthorizationStatus.LOADING.statusImageResId)
        Assert.assertNull(AuthorizationStatus.PENDING.statusImageResId)
        Assert.assertNull(AuthorizationStatus.CONFIRM_PROCESSING.statusImageResId)
        Assert.assertNull(AuthorizationStatus.DENY_PROCESSING.statusImageResId)
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
        Assert.assertTrue(AuthorizationStatus.LOADING.processingMode)
        Assert.assertFalse(AuthorizationStatus.PENDING.processingMode)
        Assert.assertTrue(AuthorizationStatus.CONFIRM_PROCESSING.processingMode)
        Assert.assertTrue(AuthorizationStatus.DENY_PROCESSING.processingMode)
        Assert.assertFalse(AuthorizationStatus.CONFIRMED.processingMode)
        Assert.assertFalse(AuthorizationStatus.DENIED.processingMode)
        Assert.assertFalse(AuthorizationStatus.ERROR.processingMode)
        Assert.assertFalse(AuthorizationStatus.TIME_OUT.processingMode)
        Assert.assertFalse(AuthorizationStatus.UNAVAILABLE.processingMode)
    }
}
