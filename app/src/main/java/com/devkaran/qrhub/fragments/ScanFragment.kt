package com.devkaran.qrhub.fragments

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devkaran.qrhub.databinding.FragmentScanBinding
import com.devkaran.qrhub.dataclass.QRCode
import com.devkaran.qrhub.model.QRCodeViewModel
import com.devkaran.qrhub.model.QRCodeViewModelFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory

class ScanFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: QRCodeViewModel
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    // Light sensor
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var torchEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startScan()
                } else {
                    _binding?.scanResultText?.text = "Camera permission denied"
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)

        val factory = QRCodeViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[QRCodeViewModel::class.java]

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        checkCameraPermission()

        // Scanner animation
        binding.barcodeScanner.post {
            val scannerHeight = binding.barcodeScanner.height
            val animator = ObjectAnimator.ofFloat(
                binding.scannerLine,
                "translationY",
                0f,
                scannerHeight.toFloat()
            ).apply {
                duration = 1200
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
            animator.start()
        }

        return binding.root
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startScan()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                binding.scanResultText.text = "Camera permission is required to scan QR codes"
            }

            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startScan() {
        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128, BarcodeFormat.CODE_39)
        binding.barcodeScanner.decoderFactory = DefaultDecoderFactory(formats)
        binding.barcodeScanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                val content = result.text
                val format = result.barcodeFormat.name

                saveToHistory(content, format)
                handleScannedContent(content)
                binding.barcodeScanner.pause()
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        })
        binding.barcodeScanner.resume()
    }

    private fun handleScannedContent(content: String) {
        if (Patterns.WEB_URL.matcher(content).matches()) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(content))
            startActivity(browserIntent)
        } else {
            binding.scanResultText.text = content
        }
    }

    fun saveToHistory(content: String, type: String) {
        val qrCode = QRCode(content = content, type = type, timestamp = System.currentTimeMillis())
        viewModel.insert(qrCode)
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.barcodeScanner.resume()
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Light sensor callback
    override fun onSensorChanged(event: android.hardware.SensorEvent?) {
        event?.let {
            val lightLevel = it.values[0]

            // Threshold for darkness (in lux)
            if (lightLevel < 10 && !torchEnabled) {
                binding.barcodeScanner.setTorch(true)
                torchEnabled = true
            } else if (lightLevel >= 10 && torchEnabled) {
                binding.barcodeScanner.setTorch(false)
                torchEnabled = false
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
