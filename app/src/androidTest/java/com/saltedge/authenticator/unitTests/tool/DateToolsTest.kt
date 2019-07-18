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
package com.saltedge.authenticator.unitTests.tool

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.tool.MILLIS_IN_MINUTE
import com.saltedge.authenticator.tool.isAfterOrEqual
import com.saltedge.authenticator.tool.millisToRemainedMinutes
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DateToolsTest {

    @Test
    @Throws(Exception::class)
    fun constTest() {
        assertThat(MILLIS_IN_MINUTE, equalTo(60000L))
    }

    @Test
    @Throws(Exception::class)
    fun isAfterOrEqualTest() {
        Assert.assertFalse(DateTime(100L).isAfterOrEqual(DateTime(101L)))
        Assert.assertTrue(DateTime(100L).isAfterOrEqual(DateTime(100L)))
        Assert.assertTrue(DateTime(100L).isAfterOrEqual(DateTime(99L)))
    }

    @Test
    @Throws(Exception::class)
    fun remainedPinWaitTimeTest() {
        assertThat(millisToRemainedMinutes(-MILLIS_IN_MINUTE), equalTo(-1))
        assertThat(millisToRemainedMinutes(0L), equalTo(0))
        assertThat(millisToRemainedMinutes(MILLIS_IN_MINUTE - 1L), equalTo(1))
        assertThat(millisToRemainedMinutes(119998), equalTo(2))
        assertThat(millisToRemainedMinutes(2 * MILLIS_IN_MINUTE), equalTo(2))
    }
}
