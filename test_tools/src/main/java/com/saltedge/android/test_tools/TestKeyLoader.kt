/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.android.test_tools

import android.util.Base64
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

fun loadTestPrivateKey(): PrivateKey {
    val privateKeyContent = PRIVATE_KEY_PEM.replace("\\n".toRegex(), "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")

    val kf = KeyFactory.getInstance("RSA")
    val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.decode(privateKeyContent, Base64.DEFAULT))
    return kf.generatePrivate(keySpecPKCS8)
}

fun loadTestPublicKey(keyString: String = PUBLIC_KEY_PEM): PublicKey {
    val publicKeyContent = keyString.replace("\\n".toRegex(), "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")

    val kf = KeyFactory.getInstance("RSA")
    val keySpecX509 = X509EncodedKeySpec(Base64.decode(publicKeyContent, Base64.DEFAULT))
    return kf.generatePublic(keySpecX509) as RSAPublicKey
}

private const val PRIVATE_KEY_PEM = "-----BEGIN PRIVATE KEY-----\n" +
    "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCmlVT+dllV7BQJ\n" +
    "FUvPnVeIpyWLOIg5vn9F6ocfzaKSALnXv/tCj9wEUr7+I4L7IOBQuQkTdIyEbzkm\n" +
    "gKp5fuNjN4gt7ksuCoDEsI6NAWeRANofA/ZZKFTxZCJ9nnxRjlOHYYIpJwWuwvE+\n" +
    "vwctYkdw8LqqJy46aC/JPrhK9YWHyXmcdZzH9EDt7lnh4JGe2rJUO/rbb+9jJs0i\n" +
    "cUy+oH6gQY6ETKH+oJiYi6YQfdAf3V6oJ3Tn9n0+26AfTxOpE8Mvqzka6d9OwEdi\n" +
    "El3ENNyqHR62T5OdSpg43uM5cMA3svzLlUj9QQtU40EnLwzNzIRHWnI+GucIMxop\n" +
    "fr4tykGjAgMBAAECggEBAIxuN0iM3KLiccF2goJzUeeRgMTs02pafVCjdWYKJJKQ\n" +
    "qhqHbXmfbQr39qqnpTOvc2qZkl0kU5ERckxTC2OA5JAvsE2IJTibAhLOBF46YOSM\n" +
    "o1SiMnrW1UTp9WmQwZy/Lwth2Ch5DQFWtaj/wT0xLKi5R6NJhF8YlbBBf3sxn2/n\n" +
    "vECcWwzpzxdGH3PsNkDgDLTmq/3nZHmtm6f5cngmOuAZGuSnITnUPC92dZjvBOIb\n" +
    "7f3j1UzMrRSYaInkA4vEbpgCGB9doPnAHSERoSalEDCJa4jMob6NuzUtJ4pYVwxZ\n" +
    "+9+tI2Edc0S1V1J8D9DeJAwfaScERP/3L3yGyRXkDYECgYEA2dVIPhLoUlFr8ve3\n" +
    "7FytOFsZJ0wI9nkWtgwINOmZKkFZUvGNkcKZXR1fdbW/ZdKZALV7s94PahvBGahz\n" +
    "8Xt1XRS8m+dQ4YTs4EkAnO5Dv7PCc7xNku4PCA/Gj4PnvUUfJZKloFWwNKXC/6Ay\n" +
    "/jPfna41JnbyNIPz5pc26d9cG5UCgYEAw8VIrEmF+4GlqSXcPF26WcYv8dyQjY6Q\n" +
    "/gC9CyWmo5hrjhmFSEdYm8UTjS24a46USami17jcNGtc93DK6LjQUfS/YyT3OD/Z\n" +
    "YEWO436A+/HcgKIdrny/MBBLRnpMlQ18JwZU/ev94DzptV6QP8TvZhGax+4X9eOI\n" +
    "TfKOrf4A2lcCgYEA2OVUPHKJHsXxzYg4e0HAPHgAsK810Wy6X2PVnBHorzlIXp0j\n" +
    "0DnKiPuhPExOmm06aYlK9cqq4IVoHUssvwqlzpvCPR3y28qGLcCiup9HmA6+FI0v\n" +
    "VJhzIRzMIcQ+L6gunjPJdL89Zv6SDgOOIqy2AIQgaf9PpViptzVjuTTsuo0CgYBT\n" +
    "5FLEIa2cnW4UEflSlQeqONeK2W3Ie3N+pWpdQIEsUcClYCJRWuGJvuYl9ZW14Z+C\n" +
    "AYOa1cjnXaq1Dkylda0vsaXiIpEeNZ49NxGIQ0marYZESTRW9XYobpMTXI08wk6V\n" +
    "mo0JUvuz/+ZWpnNCIG5GWvzs+AakhCCA5pRl0xcieQKBgEOGw+fLhsTn0EfMr0JK\n" +
    "OIkklonQVCOHwp37JwlE6fGlZseBLefrahwTCo+S9DWPsaHRPc1wry9e1vC5C7vf\n" +
    "4+O0rUl/N9qYe8gYQmu/I4YDKjVPgKHxeLmtFtDo1mo1oZinE/W7HzBLcMKJA7sf\n" +
    "LtfeaS9c4yK4FvEZUQ0TrwAr\n" +
    "-----END PRIVATE KEY-----\n"

private const val PUBLIC_KEY_PEM = "-----BEGIN PUBLIC KEY-----\n" +
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAppVU/nZZVewUCRVLz51X\n" +
    "iKcliziIOb5/ReqHH82ikgC517/7Qo/cBFK+/iOC+yDgULkJE3SMhG85JoCqeX7j\n" +
    "YzeILe5LLgqAxLCOjQFnkQDaHwP2WShU8WQifZ58UY5Th2GCKScFrsLxPr8HLWJH\n" +
    "cPC6qicuOmgvyT64SvWFh8l5nHWcx/RA7e5Z4eCRntqyVDv622/vYybNInFMvqB+\n" +
    "oEGOhEyh/qCYmIumEH3QH91eqCd05/Z9PtugH08TqRPDL6s5GunfTsBHYhJdxDTc\n" +
    "qh0etk+TnUqYON7jOXDAN7L8y5VI/UELVONBJy8MzcyER1pyPhrnCDMaKX6+LcpB\n" +
    "owIDAQAB\n" +
    "-----END PUBLIC KEY-----\n"
