/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.connections.delete

import android.content.DialogInterface
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_ALL_REQUEST_CODE
import com.saltedge.authenticator.core.model.GUID
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeleteConnectionPresenterTest : DeleteConnectionContract.View {

    private var dismissViewCallback: Boolean = false
    private var resultGuidCallback: String? = null

    override fun dismissView() {
        this.dismissViewCallback = true
    }

    override fun returnSuccessResult(guid: GUID) {
        this.resultGuidCallback = guid
    }

    @Before
    fun setUp() {
        dismissViewCallback = false
        resultGuidCallback = null
    }

    @Test
    @Throws(Exception::class)
    fun getViewContractTest() {
        val presenter = DeleteConnectionPresenter(viewContract = this)

        assertNull(presenter.guid)
        assertNotNull(presenter.viewContract)
    }

    @Test
    @Throws(Exception::class)
    fun onActionViewClickTestCase1() {
        val presenter = DeleteConnectionPresenter(viewContract = this)

        presenter.onActionViewClick(DialogInterface.BUTTON_NEGATIVE)

        assertTrue(dismissViewCallback)
        assertNull(resultGuidCallback)
    }

    @Test
    @Throws(Exception::class)
    fun onActionViewClickTestCase2() {
        val presenter = DeleteConnectionPresenter(viewContract = this)
        presenter.guid = "test"

        presenter.onActionViewClick(DialogInterface.BUTTON_POSITIVE)

        assertTrue(dismissViewCallback)
        assertThat(resultGuidCallback!!, equalTo("test"))
    }

    @Test
    @Throws(Exception::class)
    fun viewTitleTest() {
        val presenter = DeleteConnectionPresenter(viewContract = this)
        presenter.guid = null

        assertThat(
            presenter.viewTitle(DELETE_ALL_REQUEST_CODE),
            equalTo(R.string.delete_connections_title)
        )

        presenter.guid = "guid1"

        assertThat(presenter.viewTitle(0), equalTo(R.string.delete_connection_title))
    }

    @Test
    @Throws(Exception::class)
    fun viewMessageTest() {
        val presenter = DeleteConnectionPresenter(viewContract = this)
        presenter.guid = null

        assertThat(
            presenter.viewMessage(DELETE_ALL_REQUEST_CODE),
            equalTo(R.string.delete_connections_message)
        )

        presenter.guid = "guid1"

        assertThat(presenter.viewMessage(0), equalTo(R.string.delete_connection_message))
    }
}
