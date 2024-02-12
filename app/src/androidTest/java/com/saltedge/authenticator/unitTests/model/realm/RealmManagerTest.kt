/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.unitTests.model.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.instrumentationTestTools.TestTools
import com.saltedge.authenticator.models.realm.DB_SCHEMA_VERSION
import com.saltedge.authenticator.models.realm.RealmManager
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealmManagerTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        RealmManager.resetError()

        assertThat(DB_SCHEMA_VERSION, equalTo(5L))
        Assert.assertFalse(RealmManager.initialized)
        Assert.assertFalse(RealmManager.errorOccurred)

        RealmManager.initRealm(TestTools.applicationContext)

        Assert.assertTrue(RealmManager.initialized)
        Assert.assertFalse(RealmManager.errorOccurred)

        RealmManager.resetError()

        Assert.assertFalse(RealmManager.initialized)
        Assert.assertFalse(RealmManager.errorOccurred)
    }
}
