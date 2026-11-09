package com.paan.safa.ui.screens.order

import android.content.Context
import com.paan.safa.R
import com.paan.safa.core.ui.UiText
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Opens Google's code scanner (no camera permission needed in our app).
 * Never logs the scanned text: it contains the pickup code.
 */
fun startPickupScan(
    context: Context,
    onResult: (String) -> Unit,
    onError: (UiText) -> Unit,
    onCancel: () -> Unit = {},
) {
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    try {
        GmsBarcodeScanning.getClient(context, options).startScan()
            .addOnSuccessListener { barcode ->
                val raw = barcode.rawValue
                if (raw.isNullOrBlank()) onError(UiText.Res(R.string.pickup_qr_unreadable)) else onResult(raw)
            }
            .addOnCanceledListener { onCancel() }
            .addOnFailureListener {
                onError(UiText.Res(R.string.pickup_scanner_unavailable))
            }
    } catch (_: Exception) {
        onError(UiText.Res(R.string.pickup_scanner_unsupported))
    }
}
