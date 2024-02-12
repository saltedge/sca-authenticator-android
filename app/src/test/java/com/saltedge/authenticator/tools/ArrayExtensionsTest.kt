/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.util.SparseArray
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArrayExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun getOrPutTestCase1() {
        val map = SparseArray<String>()
        map.put((0), "Fentury")

        assertThat(
            map.getOrPut((0), defaultValue = { "Salt Edge" }),
            equalTo("Fentury")
        )
    }

    @Test
    @Throws(Exception::class)
    fun getOrPutTestCase2() {
        val map = SparseArray<String>()

        assertThat(
            map.getOrPut((0), defaultValue = { "Salt Edge" }),
            equalTo("Salt Edge")
        )
    }
}
