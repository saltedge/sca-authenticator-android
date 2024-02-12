/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.editName

import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.features.connections.edit.EditConnectionNameDialog
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditConnectionNameDialogTest {

    @Test
    @Throws(Exception::class)
    fun dataBundleTest() {
        val arguments = EditConnectionNameDialog.dataBundle(guid = "guid1", name = "Demobank")

        assertThat(arguments.getString(KEY_GUID), equalTo("guid1"))
        assertThat(arguments.getString(KEY_NAME), equalTo("Demobank"))
    }
}
