/*
 * Copyright (c) 2020 Salt Edge Inc.
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
    "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDTYrQHUXv3hzgG\n" +
    "WwIqEQ+x3ylfNqi+emse1UkhJzx7dsycxVpw1FtOzXBaiBneyCq1no9CDw1MAsJd\n" +
    "W8WPFl8PbtTfbwtuDnjImndS9DVP5crwSBqB2ke9RYlipf3yi8O9JQ50kaFzXhIs\n" +
    "BoRuJnpgEG79czWJ6ZkY+j9lKXbaULBEHix0O/4BGDszTu/PFnolZmYkmJaAzCyq\n" +
    "sh7TVVAOD4lNlt/TXoKwt62vM5yUHEaPZywNshZuoFzk73ADUqMHjmmV/++y6+9Q\n" +
    "Xal9cfyAovvg0EjFsJnVBRZ1/S/Hbb3XImJqOtB9qrFgUlv+p4xlE6pRxSzc8jUL\n" +
    "2pjNCsx5AgMBAAECggEADVSrL872t2yCnvv8TfTXfYsqS8Phrml/+ejPfNTBnrOm\n" +
    "Qbi8dGMgAv83t63fm8oZz2xZcnwKhaGi6kpQUi7B/fIqYJDbMqyc92GuZr/Z4NkI\n" +
    "DFEvwsKmRYxuFpeLRVGFHZeJqtZITUNAeCBNF7k7fHVs2nT6imD987JMby/HPAs/\n" +
    "QQiT+8GBIHpB5RzTiAiFG6eerIrt4PNpHNJ4jtkIA/2mFGuEBZ9IQsrj44b/zESZ\n" +
    "VRkXOpXFqBnHG+UifF/NpUT8GXKO5t50Uxu0oUZ9wHPJZQQM938owkFDlal9Sbuc\n" +
    "ne0wiVYop8E1yO9/+Qj43Po2GCrBWWWFvgECRVZEtQKBgQD1D662pPu13xRk/XJB\n" +
    "cZiSWqwbL3eFGmyp9AjBZUfb1cwJuNk09zTgx3vEpM04+ufuc2t63fXMif6Q7KoE\n" +
    "KYpFbbtsje1cybNykHMGlHao2j5bL6Pg9l9UgG0SmO4oNhVF/yuO8xmZCrkScKXg\n" +
    "+jthuf3fhDBJLxq7PXxIKlvkQwKBgQDc0jVMCn93491pHDxmtXGxxNw3QBoK51Cz\n" +
    "8RjlbnMbVXVbJ9/kpFXdfkDioohfzbSV8dZiO4EO/t4s/SY92/6fz4ypwbq4e4CD\n" +
    "GD3mjN5mHJqmN46FMpmp1Y1u5reCwOwM9oNHQu7MLGbnFn4nnLzRyW5ssSkE3qij\n" +
    "5J7XBl++kwKBgQCEHcRPa3rYfj/8gjcK2TcsD/4hWeHRvIFAzvO5b+Thu82YoDNZ\n" +
    "vryfQmyTg1WwlnRbtZYIU4mkj/DSKQUv4UVOUO5ug8Wn9IXuCbo5neiq954OgwyS\n" +
    "x8B1SheDozciLqYhaoQNOTpfq8xDCMAlHkLNWPxpeFaf3elHcMqEDXIlCQKBgCNa\n" +
    "8LedEq2iaSfUIoObV3nL+CpMdB78zDoTRxYs6fcdZq3So5FbEnhlJ4Rh243jRJ+h\n" +
    "Wd7eghphijcPzRioaYsy0uW3I+s3surKbdbFBTK6L7SfPo1q/ZlonL196THhK6oj\n" +
    "qf8140ADd/JV8prrCHxaUPjMILIr6Dpdd2UcGyZLAoGBAK3ps1/O4z0Ng0NtmxYY\n" +
    "0pzgMIrVgMZEwdCGyvjYz3E8VIwgdkTYBTftSTDo6cAryzdgRcmjn898Xf5dI4At\n" +
    "gvL1lEauYIVGuY8hQorDvcTRSbEu6JqwEV7kiCXTxNDff0Hja+FBsQ0yjdCzkeZs\n" +
    "SHq9f/edLnB9JAJymXMbPT6I\n" +
    "-----END PRIVATE KEY-----\n"

private const val PUBLIC_KEY_PEM = "-----BEGIN PUBLIC KEY-----\n" +
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA02K0B1F794c4BlsCKhEP\n" +
    "sd8pXzaovnprHtVJISc8e3bMnMVacNRbTs1wWogZ3sgqtZ6PQg8NTALCXVvFjxZf\n" +
    "D27U328Lbg54yJp3UvQ1T+XK8EgagdpHvUWJYqX98ovDvSUOdJGhc14SLAaEbiZ6\n" +
    "YBBu/XM1iemZGPo/ZSl22lCwRB4sdDv+ARg7M07vzxZ6JWZmJJiWgMwsqrIe01VQ\n" +
    "Dg+JTZbf016CsLetrzOclBxGj2csDbIWbqBc5O9wA1KjB45plf/vsuvvUF2pfXH8\n" +
    "gKL74NBIxbCZ1QUWdf0vx2291yJiajrQfaqxYFJb/qeMZROqUcUs3PI1C9qYzQrM\n" +
    "eQIDAQAB\n" +
    "-----END PUBLIC KEY-----\n"
