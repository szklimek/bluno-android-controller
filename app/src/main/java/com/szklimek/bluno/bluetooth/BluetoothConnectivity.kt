package com.szklimek.bluno.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * Specific [ActivityResultContract] to prompt user to enable bluetooth connectivity
 */
class EnableBluetoothContract : ActivityResultContract<Unit, Boolean?>() {
    override fun createIntent(context: Context, input: Unit?) =
        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

    override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == Activity.RESULT_OK
}

fun getScanFailureReasonMessage(errorCode: Int) = when (errorCode) {
    ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "SCAN_FAILED_ALREADY_STARTED. Fails to start scan as BLE scan with the same settings is already started by the app"
    ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED. Fails to start scan as app cannot be registered"
    ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "SCAN_FAILED_INTERNAL_ERROR. Fails to start scan due an internal error"
    ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "SCAN_FAILED_FEATURE_UNSUPPORTED. Fails to start power optimized scan as this feature is not supported"
    5 /* SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES */ -> "SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES. Fails to start scan as it is out of hardware resources"
    6 /* SCAN_FAILED_SCANNING_TOO_FREQUENTLY */ -> "SCAN_FAILED_SCANNING_TOO_FREQUENTLY. Fails to start scan as application tries to scan too frequently."
    else -> "SCAN_FAILED_UNKNOWN_ERROR. Scan failed due to unknown error"
}
