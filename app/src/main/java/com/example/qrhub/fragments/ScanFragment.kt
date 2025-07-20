package com.example.qrhub.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.qrhub.databinding.FragmentScanBinding
import com.example.qrhub.dataclass.QRCode
import com.example.qrhub.model.QRCodeViewModel
import com.example.qrhub.model.QRCodeViewModelFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory

class ScanFragment : Fragment() {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: QRCodeViewModel

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startScan()
        } else {
            binding.scanResultText.text = "Camera permission denied"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)

        val factory = QRCodeViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(QRCodeViewModel::class.java)

        binding.scanButton.setOnClickListener {
            checkCameraPermission()
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
            // It's a URL, open in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(content))
            startActivity(browserIntent)
        } else {
            // It's plain text, show on screen
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
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeScanner.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
