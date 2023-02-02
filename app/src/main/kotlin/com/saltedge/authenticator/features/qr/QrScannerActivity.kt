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
package com.saltedge.authenticator.features.qr

import android.Manifest
import android.Manifest.permission
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.*
import com.saltedge.authenticator.features.main.SnackbarAnchorContainer
import com.saltedge.authenticator.features.main.showWarningSnack
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.realm.initRealmDatabase
import com.saltedge.authenticator.tools.getScreenHeight
import com.saltedge.authenticator.tools.getScreenWidth
import com.saltedge.authenticator.widget.security.LockableActivity
import com.saltedge.authenticator.widget.security.UnlockAppInputView
import kotlinx.android.synthetic.main.activity_qr_scanner.*
import timber.log.Timber
import javax.inject.Inject

class QrScannerActivity : LockableActivity(), SnackbarAnchorContainer {

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    @Inject lateinit var viewModelFactory: ViewModelsFactory
    lateinit var viewModel: QrScannerViewModel
    private var errorDialog: AlertDialog? = null
    private val requestMultiplePermissions = (this as ComponentActivity).registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { actionMap ->
            when (actionMap.key) {
                Manifest.permission.CAMERA -> {
                    if (actionMap.value) {
                        startCameraSource()
                    } else {
                        viewModel.showErrorMessage(R.string.errors_camera_permission_denied)
                    }
                }
                Manifest.permission.POST_NOTIFICATIONS -> {
                    if (actionMap.value) {
                        setupNotificationsPermissions()
                    } else {
                        viewModel.showErrorMessage(R.string.errors_notifications_permission_denied)
                    }
                }
                else -> viewModel.showErrorMessage(R.string.errors_permission_denied)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initRealmDatabase()
        authenticatorApp?.appComponent?.inject(this)
        setContentView(R.layout.activity_qr_scanner)
        setupViewModel()
        setupViews()
    }

    override fun onLockActivity() {
        errorDialog?.let { if (it.isShowing) it.dismiss() }
    }

    override fun onDestroy() {
        barcodeDetector?.release()
        cameraSource?.release()
        super.onDestroy()
    }

    override fun getUnlockAppInputView(): UnlockAppInputView? = unlockAppInputView

    override fun getSnackbarAnchorView(): View? = surfaceView

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(QrScannerViewModel::class.java)

        viewModel.onCloseEvent.observe(this, Observer<ViewModelEvent<Unit>> {
            it.getContentIfNotHandled()?.let { finish() }
        })
        viewModel.setActivityResult.observe(this, Observer<String> { deepLink ->
            this.setResult(Activity.RESULT_OK, intent.putExtra(KEY_DEEP_LINK, deepLink))
        })
        viewModel.errorMessageResId.observe(this, Observer { errorMessageResId ->
            errorDialog = AlertDialog.Builder(this)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(getString(errorMessageResId ?: R.string.errors_invalid_qr))
                .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.onErrorConfirmed() }
                .show()
        })
    }

    override fun onClearAppDataEvent() {
        //empty
    }

    private fun setupViews() {
        closeImageView?.setOnClickListener { view -> viewModel.onViewClick(view.id) }
        descriptionView?.setText(viewModel.descriptionRes)
        setupBarcodeDetector()
        setupCameraSource()
        setupSurface()
    }

    private fun setupCameraSource() {
        val height = this.getScreenHeight()
        val width = this.getScreenWidth()
        cameraSource = CameraSource.Builder(applicationContext, barcodeDetector)
            .setRequestedPreviewSize(height, width)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setAutoFocusEnabled(true)
            .build()
    }

    private fun setupSurface() {
        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource?.stop()
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
                setupNotificationsPermissions()
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

    private fun setupNotificationsPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startCameraSource()
                } else {
                    runCatching {
                        requestMultiplePermissions.launch(arrayOf(permission.POST_NOTIFICATIONS))
                    }.onFailure {
                        Timber.e(it)
                    }
                }
            } else {
                startCameraSource()
            }
        } catch (e: Exception) {
            viewModel.onSetupNotificationException()
            Timber.e(e)
        }
    }

    private fun startCameraSource() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val holder = surfaceView?.holder
                if (holder != null) cameraSource?.start(holder)
                else this@QrScannerActivity.showWarningSnack(textResId = R.string.errors_failed_to_start_camera)
            } else {
                runCatching {
                    requestMultiplePermissions.launch(arrayOf(permission.CAMERA))
                }.onFailure {
                    viewModel.onCameraInitException()
                    Timber.e(it)
                }
            }
        } catch (e: Exception) {
            viewModel.onCameraInitException()
            Timber.e(e)
        }
    }
}
