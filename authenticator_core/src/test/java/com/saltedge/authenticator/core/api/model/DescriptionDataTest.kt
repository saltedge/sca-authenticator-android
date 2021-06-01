/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.core.api.model

import junit.framework.Assert.assertTrue
import org.joda.time.DateTime
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DescriptionDataTest {

    @Test
    @Throws(Exception::class)
    fun hasPaymentContentTest() {
        val descriptionData = DescriptionData(
            payment = DescriptionPaymentData(
                payee = "Brand Shop Limited",
                amount = "4120.00",
                account = "Checking Account",
                paymentDate = DateTime.now(),
                fee = "1.09",
                exchangeRate = "1.18",
                reference = "Tag Heuer Carrera Calibre 01 Watch"
            ),
            extra = ExtraData(
                actionDate = DateTime.now().plusMinutes(23),
                device = "Google Chrome on iPhone 11 Pro",
                location = "Munich, Germany",
                ip = "195.22.222.80"
            )
        )

        assertTrue(descriptionData.hasPaymentContent)
        assertTrue(descriptionData.hasExtraContent)
    }

    @Test
    @Throws(Exception::class)
    fun hasHtmlContentTest() {
        val descriptionData = DescriptionData(
            html = "<b>TPP</b> is requesting your authorization to access account information data from <b>Demo Bank</b>"
        )

        assertTrue(descriptionData.hasHtmlContent)
        assertFalse(descriptionData.hasExtraContent)
        assertFalse(descriptionData.hasPaymentContent)
        assertFalse(descriptionData.hasTextContent)
    }

    @Test
    @Throws(Exception::class)
    fun hasTextContentTest() {
        val descriptionData = DescriptionData(
            text = "TPP is requesting your authorization to access account information data from Demo Bank",
            extra = ExtraData(
                actionDate = DateTime.now().plusMinutes(23),
                device = "Google Chrome on iPhone 11 Pro",
                location = "Munich, Germany",
                ip = "195.22.222.80"
            )
        )

        assertFalse(descriptionData.hasHtmlContent)
        assertTrue(descriptionData.hasExtraContent)
        assertFalse(descriptionData.hasPaymentContent)
        assertTrue(descriptionData.hasTextContent)
    }
}
