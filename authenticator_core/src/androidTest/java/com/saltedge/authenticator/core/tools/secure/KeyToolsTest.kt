/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2023 Salt Edge Inc.
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
package com.saltedge.authenticator.core.tools.secure

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.PublicKey

@RunWith(AndroidJUnit4::class)
class KeyToolsTest {

    @Test
    @Throws(Exception::class)
    fun pemToPublicKeyTestCase1() {
        // Given PKCS1 key
        val pemKey = "-----BEGIN RSA PUBLIC KEY-----\n" +
            "MIIBigKCAYEAz2JhxKyAj7dSyZYyF9Ry9TDgV7kfujqfAyI4D7TeulJHQx3XfhNK\n" +
            "LQUUbUGc5uYYt3XmtaYTQK0oxDbOuhMFjaGQ34CHDdb7vRz72LCTEpOJXeFoN1nj\n" +
            "vpLzJowZbhJb2vmBC6Q7Qu7DMhnCARrF+D5D8KCa3Bc1+Z5wJ8ybp2ShGGJNDVyr\n" +
            "0nusFp94el97wnaKWEqYpYkvyXHKmSkLwdK38Vjb625Mt18KGcCvTc0EfactN2yu\n" +
            "NAA2VDtjtGldW1fX2dgMo4ksfgKC9TipbjZjRwQsybplEvDOoxgaCZ7MimIQTrfN\n" +
            "sLOPG1WNAAnGJWNlfRGcRHaXr+tsbPi1cR4MecQszdqKnEwRaqrFfFep6nk2/G+f\n" +
            "zhNukSBf+fU4/dx4S4ZIffiPztXjSaI3A+Ti0efhGOm1pHY+7eA2rrpSJobB6JiA\n" +
            "3FXh+9mhWV3m4RJhq/i4FS8lbQxp9niC0SIrr7r/Bf2Mc1OMTl66TxqD3diiWADd\n" +
            "iio/69FsejWpAgMBAAE=\n" +
            "-----END RSA PUBLIC KEY-----"

        // When
        val publicKey = pemKey.pemToPublicKey("RSA")

        // Then
        assertTrue(publicKey is PublicKey)
        assertEquals("RSA", publicKey?.algorithm)
    }

    @Test
    @Throws(Exception::class)
    fun pemToPublicKeyTestCase2() {
        // Given PKCS8 key
        val pemKey = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAppVU/nZZVewUCRVLz51X\n" +
            "iKcliziIOb5/ReqHH82ikgC517/7Qo/cBFK+/iOC+yDgULkJE3SMhG85JoCqeX7j\n" +
            "YzeILe5LLgqAxLCOjQFnkQDaHwP2WShU8WQifZ58UY5Th2GCKScFrsLxPr8HLWJH\n" +
            "cPC6qicuOmgvyT64SvWFh8l5nHWcx/RA7e5Z4eCRntqyVDv622/vYybNInFMvqB+\n" +
            "oEGOhEyh/qCYmIumEH3QH91eqCd05/Z9PtugH08TqRPDL6s5GunfTsBHYhJdxDTc\n" +
            "qh0etk+TnUqYON7jOXDAN7L8y5VI/UELVONBJy8MzcyER1pyPhrnCDMaKX6+LcpB\n" +
            "owIDAQAB\n" +
            "-----END PUBLIC KEY-----"

        // When
        val publicKey = pemKey.pemToPublicKey("RSA")

        // Then
        assertTrue(publicKey is PublicKey)
        assertEquals("RSA", publicKey?.algorithm)
    }

    @Test
    @Throws(Exception::class)
    fun pemToPublicKeyTestCase3() {
        // Given fake key
        val pemKey = "-----BEGIN FAKE PUBLIC KEY-----\n" +
            "MIIBigKCAYEAz2JhxKyAj7dSyZYyF9Ry9TDgV7kfujqfAyI4D7TeulJHQx3XfhNK\n" +
            "LQUUbUGc5uYYt3XmtaYTQK0oxDbOuhMFjaGQ34CHDdb7vRz72LCTEpOJXeFoN1nj\n" +
            "vpLzJowZbhJb2vmBC6Q7Qu7DMhnCARrF+D5D8KCa3Bc1+Z5wJ8ybp2ShGGJNDVyr\n" +
            "0nusFp94el97wnaKWEqYpYkvyXHKmSkLwdK38Vjb625Mt18KGcCvTc0EfactN2yu\n" +
            "NAA2VDtjtGldW1fX2dgMo4ksfgKC9TipbjZjRwQsybplEvDOoxgaCZ7MimIQTrfN\n" +
            "sLOPG1WNAAnGJWNlfRGcRHaXr+tsbPi1cR4MecQszdqKnEwRaqrFfFep6nk2/G+f\n" +
            "zhNukSBf+fU4/dx4S4ZIffiPztXjSaI3A+Ti0efhGOm1pHY+7eA2rrpSJobB6JiA\n" +
            "3FXh+9mhWV3m4RJhq/i4FS8lbQxp9niC0SIrr7r/Bf2Mc1OMTl66TxqD3diiWADd\n" +
            "iio/69FsejWpAgMBAAE=\n" +
            "-----END RSA PUBLIC KEY-----"

        // When
        val publicKey = pemKey.pemToPublicKey("RSA")

        // Then
        assertFalse(publicKey is PublicKey)
        assertNull(publicKey?.algorithm)
    }
}
