/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.instrumentationTestTools

import androidx.test.platform.app.InstrumentationRegistry
import com.saltedge.authenticator.models.realm.RealmManager
import io.realm.Realm
import org.junit.After
import org.junit.Before

open class DatabaseTestCase {

    init {
        RealmManager.initRealm(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    lateinit var testRealm: Realm

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        TestTools.setLocale("en")
        testRealm = Realm.getDefaultInstance()
        testRealm.executeTransaction { it.deleteAll() }
    }

    @After
    @Throws(Exception::class)
    open fun tearDown() {
        testRealm.close()
    }
}
