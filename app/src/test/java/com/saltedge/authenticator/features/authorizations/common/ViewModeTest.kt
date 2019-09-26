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
        assertThat(ViewMode.values(), equalTo(arrayOf(
            ViewMode.LOADING,
            ViewMode.DEFAULT,
            ViewMode.CONFIRM_PROCESSING,
            ViewMode.DENY_PROCESSING,
            ViewMode.CONFIRM_SUCCESS,
            ViewMode.DENY_SUCCESS,
            ViewMode.ERROR,
            ViewMode.TIME_OUT,
            ViewMode.UNAVAILABLE
        )))
    }

    @Test
    @Throws(Exception::class)
    fun isFinalModeTest() {
        Assert.assertFalse(ViewMode.LOADING.isFinalMode())
        Assert.assertFalse(ViewMode.DEFAULT.isFinalMode())
        Assert.assertFalse(ViewMode.CONFIRM_PROCESSING.isFinalMode())
        Assert.assertFalse(ViewMode.DENY_PROCESSING.isFinalMode())
        Assert.assertTrue(ViewMode.CONFIRM_SUCCESS.isFinalMode())
        Assert.assertTrue(ViewMode.DENY_SUCCESS.isFinalMode())
        Assert.assertTrue(ViewMode.ERROR.isFinalMode())
        Assert.assertTrue(ViewMode.TIME_OUT.isFinalMode())
        Assert.assertTrue(ViewMode.UNAVAILABLE.isFinalMode())
    }

    @Test
    @Throws(Exception::class)
    fun statusImageResIdTest() {
        Assert.assertNull(ViewMode.LOADING.statusImageResId)
        Assert.assertNull(ViewMode.DEFAULT.statusImageResId)
        Assert.assertNull(ViewMode.CONFIRM_PROCESSING.statusImageResId)
        Assert.assertNull(ViewMode.DENY_PROCESSING.statusImageResId)
        assertThat(ViewMode.CONFIRM_SUCCESS.statusImageResId, equalTo(R.drawable.ic_auth_success_70))
        assertThat(ViewMode.DENY_SUCCESS.statusImageResId, equalTo(R.drawable.ic_auth_denied_70))
        assertThat(ViewMode.ERROR.statusImageResId, equalTo(R.drawable.ic_auth_error_70))
        assertThat(ViewMode.TIME_OUT.statusImageResId, equalTo(R.drawable.ic_auth_timeout_70))
        assertThat(ViewMode.UNAVAILABLE.statusImageResId, equalTo(R.drawable.ic_auth_error_70))
    }

    @Test
    @Throws(Exception::class)
    fun statusTitleResIdTest() {
        assertThat(ViewMode.LOADING.statusTitleResId, equalTo(R.string.authorizations_loading))
        assertThat(ViewMode.DEFAULT.statusTitleResId, equalTo(R.string.authorizations_loading))
        assertThat(ViewMode.CONFIRM_PROCESSING.statusTitleResId, equalTo(R.string.authorizations_processing))
        assertThat(ViewMode.DENY_PROCESSING.statusTitleResId, equalTo(R.string.authorizations_processing))
        assertThat(ViewMode.CONFIRM_SUCCESS.statusTitleResId, equalTo(R.string.authorizations_confirmed))
        assertThat(ViewMode.DENY_SUCCESS.statusTitleResId, equalTo(R.string.authorizations_denied))
        assertThat(ViewMode.ERROR.statusTitleResId, equalTo(R.string.authorizations_error))
        assertThat(ViewMode.TIME_OUT.statusTitleResId, equalTo(R.string.authorizations_time_out))
        assertThat(ViewMode.UNAVAILABLE.statusTitleResId, equalTo(R.string.authorizations_unavailable))
    }

    @Test
    @Throws(Exception::class)
    fun statusDescriptionResIdTest() {
        assertThat(ViewMode.LOADING.statusDescriptionResId, equalTo(R.string.authorizations_loading_description))
        assertThat(ViewMode.DEFAULT.statusDescriptionResId, equalTo(R.string.authorizations_loading_description))
        assertThat(ViewMode.CONFIRM_PROCESSING.statusDescriptionResId, equalTo(R.string.authorizations_processing_description))
        assertThat(ViewMode.DENY_PROCESSING.statusDescriptionResId, equalTo(R.string.authorizations_processing_description))
        assertThat(ViewMode.CONFIRM_SUCCESS.statusDescriptionResId, equalTo(R.string.authorizations_confirmed_description))
        assertThat(ViewMode.DENY_SUCCESS.statusDescriptionResId, equalTo(R.string.authorizations_denied_description))
        assertThat(ViewMode.ERROR.statusDescriptionResId, equalTo(R.string.authorizations_error_description))
        assertThat(ViewMode.TIME_OUT.statusDescriptionResId, equalTo(R.string.authorizations_time_out_description))
        assertThat(ViewMode.UNAVAILABLE.statusDescriptionResId, equalTo(R.string.authorizations_unavailable_description))
    }

    @Test
    @Throws(Exception::class)
    fun showProgressTest() {
        Assert.assertTrue(ViewMode.LOADING.showProgress)
        Assert.assertFalse(ViewMode.DEFAULT.showProgress)
        Assert.assertTrue(ViewMode.CONFIRM_PROCESSING.showProgress)
        Assert.assertTrue(ViewMode.DENY_PROCESSING.showProgress)
        Assert.assertFalse(ViewMode.CONFIRM_SUCCESS.showProgress)
        Assert.assertFalse(ViewMode.DENY_SUCCESS.showProgress)
        Assert.assertFalse(ViewMode.ERROR.showProgress)
        Assert.assertFalse(ViewMode.TIME_OUT.showProgress)
        Assert.assertFalse(ViewMode.UNAVAILABLE.showProgress)
    }
}
