package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraX live preview with real-time ZXing barcode/QR code frame analyzer.
 * Binds directly to the LifecycleOwner and renders into AndroidView PreviewView.
 */
@Composable
fun CameraQrScannerView(
    isTorchOn: Boolean,
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val isScanningActive = remember { AtomicBoolean(true) }
    var currentCamera = remember { mutableListOf<Camera>() }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(isTorchOn) {
        val cam = currentCamera.firstOrNull()
        if (cam != null && cam.cameraInfo.hasFlashUnit()) {
            try {
                cam.cameraControl.enableTorch(isTorchOn)
            } catch (e: Exception) {
                Log.e("CameraQrScannerView", "Error toggling torch", e)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor, QrImageAnalyzer { resultText ->
                            if (isScanningActive.compareAndSet(true, false)) {
                                ContextCompat.getMainExecutor(context).execute {
                                    onQrCodeDetected(resultText)
                                }
                            }
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

                currentCamera.clear()
                currentCamera.add(boundCamera)

                if (boundCamera.cameraInfo.hasFlashUnit()) {
                    boundCamera.cameraControl.enableTorch(isTorchOn)
                }
            } catch (exc: Exception) {
                Log.e("CameraQrScannerView", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraQrScannerView", "Failed unbinding camera", e)
            }
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}

/**
 * ImageAnalysis Analyzer that extracts Y-luminance bytes from the camera frame
 * and passes them to ZXing MultiFormatReader to detect and decode QR codes.
 */
private class QrImageAnalyzer(
    private val onQrFound: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    override fun analyze(imageProxy: ImageProxy) {
        val plane = imageProxy.planes.firstOrNull()
        if (plane == null) {
            imageProxy.close()
            return
        }

        val buffer = plane.buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val width = imageProxy.width
        val height = imageProxy.height

        try {
            val source = PlanarYUVLuminanceSource(
                data,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(bitmap)
            if (result != null && !result.text.isNullOrBlank()) {
                onQrFound(result.text)
            }
        } catch (_: Exception) {
            // ZXing throws NotFoundException when no QR is present in the frame; ignore and continue next frame
        } finally {
            reader.reset()
            imageProxy.close()
        }
    }
}
