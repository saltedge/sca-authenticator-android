/*
 * Copyright (c) 2021 Salt Edge Inc.
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
