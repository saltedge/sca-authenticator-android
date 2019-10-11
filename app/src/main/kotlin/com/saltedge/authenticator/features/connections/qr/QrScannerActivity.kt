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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.CAMERA_PERMISSION_REQUEST_CODE
import com.saltedge.authenticator.app.KEY_DEEP_LINK
import com.saltedge.authenticator.sdk.tools.isValidDeeplink
import com.saltedge.authenticator.tool.AppTools.getDisplayHeight
import com.saltedge.authenticator.tool.AppTools.getDisplayWidth
import com.saltedge.authenticator.tool.log
import kotlinx.android.synthetic.main.activity_qr_scanner.*
import java.io.IOException

class QrScannerActivity : AppCompatActivity() {

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)
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
            showError(getString(R.string.errors_permission_denied))
        }
    }

    override fun onDestroy() {
        barcodeDetector?.release()
        cameraSource?.release()
        super.onDestroy()
    }

    private fun setupViews() {
        setSupportActionBar(toolbarView)
        toolbarView?.navigationIcon = getDrawable(R.drawable.ic_close_white_24dp)
        toolbarView?.setNavigationOnClickListener { finish() }

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
        val deeplinks = mutableListOf<String>()
        codes.forEach { _, value ->
            if (value.displayValue.isValidDeeplink()) { deeplinks.add(value.displayValue) }
        }
        deeplinks.firstOrNull()?.let {
            returnResultAndFinish(deeplink = it)
        }
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
            showError(getString(R.string.errors_camera_init))
            e.log()
        }
    }

    private fun returnResultAndFinish(deeplink: String) {
        this.setResult(
            Activity.RESULT_OK,
            intent.putExtra(KEY_DEEP_LINK, deeplink)
        )
        this.finish()
    }

    private fun showError(reason: String?) {
        AlertDialog.Builder(this)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(reason ?: getString(R.string.errors_invalid_qr))
            .show()
    }
}
