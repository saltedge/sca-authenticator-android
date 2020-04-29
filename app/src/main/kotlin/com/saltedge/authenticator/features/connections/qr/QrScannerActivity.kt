/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.features.connections.qr

import android.Manifest.permission
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CAMERA_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.features.connections.qr.di.QrScannerModule
import com.saltedge.authenticator.features.main.SnackbarAnchorContainer
import com.saltedge.authenticator.widget.security.LockableActivity
import com.saltedge.authenticator.widget.security.UnlockAppInputView
import com.saltedge.authenticator.tools.AppTools.getDisplayHeight
import com.saltedge.authenticator.tools.AppTools.getDisplayWidth
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.authenticatorApp
import com.saltedge.authenticator.tools.log
import kotlinx.android.synthetic.main.activity_qr_scanner.*
import java.io.IOException
import javax.inject.Inject

class QrScannerActivity : LockableActivity(), SnackbarAnchorContainer, View.OnClickListener {

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    @Inject lateinit var viewModelFactory: QrScannerViewModelFactory
    lateinit var viewModel: QrScannerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setContentView(R.layout.activity_qr_scanner)
        setupViewModel()
        setupViews()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE
            && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraSource()
        } else {
            showError(R.string.errors_permission_denied)
        }
    }

    override fun onDestroy() {
        barcodeDetector?.release()
        cameraSource?.release()
        super.onDestroy()
    }

    override fun getUnlockAppInputView(): UnlockAppInputView? = unlockAppInputView

    override fun getAppBarLayout(): View? = null

    override fun getSnackbarAnchorView(): View? = surfaceView

    override fun onClick(view: View?) {
        viewModel.onViewClick(view?.id ?: return)
    }

    private fun injectDependencies() {
        authenticatorApp?.appComponent?.addQrScannerModule(QrScannerModule())?.inject(
            this
        )
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(QrScannerViewModel::class.java)

        viewModel.closeActivity.observe(this, Observer<ViewModelEvent<Unit>> {
            finish()
        })
        viewModel.returnResultAndFinish.observe(this, Observer<String> { deeplink ->
            this.setResult(
                Activity.RESULT_OK,
                intent.putExtra(KEY_DEEP_LINK, deeplink)
            )
            this.finish()
        })
        viewModel.showError.observe(this, Observer { errorMessageResId ->
            AlertDialog.Builder(this)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(getString(errorMessageResId ?: R.string.errors_invalid_qr))
                .show()
        })
    }

    private fun setupViews() {
        closeImageView?.setOnClickListener(this)
        setupBarcodeDetector()
        setupCameraSource()
        setupSurface()
    }

    private fun setupCameraSource() {
        val height = getDisplayHeight(this)
        val width = getDisplayWidth(this)
        cameraSource = CameraSource.Builder(applicationContext, barcodeDetector)
            .setRequestedPreviewSize(height, width)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setAutoFocusEnabled(true)
            .build()
    }

    private fun setupSurface() {
        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                cameraSource?.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                startCameraSource()
            }
        })
    }

    private fun setupBarcodeDetector() {
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()
        barcodeDetector?.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                detections?.detectedItems?.let {
                    onReceivedCodes(it)
                }
            }
        })
    }

    private fun onReceivedCodes(codes: SparseArray<Barcode>) {
        viewModel.onReceivedCodes(codes)
    }

    private fun startCameraSource() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cameraSource?.start(surfaceView?.holder)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        } catch (e: IOException) {
            showError(R.string.errors_camera_init)
            e.log()
        }
    }

    private fun showError(@StringRes errorName: ResId?) {
        viewModel.showErrorMessage(errorName)
    }
}
