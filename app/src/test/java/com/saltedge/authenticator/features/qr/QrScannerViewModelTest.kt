/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.qr

import android.content.pm.PackageManager
import android.util.SparseArray
import com.google.android.gms.vision.barcode.Barcode
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrScannerViewModelTest : ViewModelTest() {

    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private lateinit var viewModel: QrScannerViewModel

    @Before
    fun setUp() {
        viewModel = QrScannerViewModel(connectionsRepository = mockConnectionsRepository)
    }

    /**
     * Test onViewClick when click on closeImageView
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase1() {
        //given
        val viewId = R.id.closeImageView

        //when
        viewModel.onViewClick(viewId = viewId)

        //then
        assertNotNull(viewModel.onCloseEvent.value)
    }

    /**
     * Test onViewClick when click on unknown id
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase2() {
        //given
        val viewId = -1

        //when
        viewModel.onViewClick(viewId = viewId)

        //then
        assertNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws
    fun onReceivedCodesTestCase1() {
        //given
        val codes = SparseArray<Barcode>()

        //when
        viewModel.onReceivedCodes(codes)

        //then
        assertNull(viewModel.setActivityResult.value)
        assertNull(viewModel.onCloseEvent.value)
    }

    @Test
    @Throws
    fun onReceivedCodesTestCase2() {
        //given
        val validDeepLink = "authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890"
        val barcode = Barcode()
        barcode.displayValue = validDeepLink
        val codes = SparseArray<Barcode>()
        codes.put((0), barcode)

        //when
        viewModel.onReceivedCodes(codes)

        //then
        assertThat(viewModel.setActivityResult.value, equalTo(validDeepLink))
        assertThat(viewModel.onCloseEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    @Test
    @Throws
    fun onCameraInitExceptionTest() {
        //when
        viewModel.onCameraInitException()

        //then
        assertThat(viewModel.errorMessageResId.value, equalTo(R.string.errors_camera_init))
    }

    @Test
    @Throws
    fun onSetupNotificationExceptionTest() {
        //when
        viewModel.onSetupNotificationException()

        //then
        assertThat(viewModel.errorMessageResId.value, equalTo(R.string.errors_notifications_setup))
    }

    @Test
    @Throws
    fun showErrorMessageTest() {
        //when
        viewModel.showErrorMessage(messageId = R.string.errors_permission_denied)

        //then
        assertThat(viewModel.errorMessageResId.value, equalTo(R.string.errors_permission_denied))
    }

    @Test
    @Throws
    fun onErrorConfirmedTest() {
        //when
        viewModel.onErrorConfirmed()

        //then
        assertThat(viewModel.onCloseEvent.value, equalTo(ViewModelEvent(Unit)))
    }
}
