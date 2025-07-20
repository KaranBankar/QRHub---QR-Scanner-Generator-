package com.example.qrhub.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.qrhub.databinding.FragmentGenerateBinding
import com.example.qrhub.dataclass.QRCode
import com.example.qrhub.model.QRCodeViewModel
import com.example.qrhub.model.QRCodeViewModelFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream

class GenerateFragment : Fragment() {
    private var _binding: FragmentGenerateBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: QRCodeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenerateBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, QRCodeViewModelFactory(requireActivity().application))
            .get(QRCodeViewModel::class.java)

        binding.generateButton.setOnClickListener {
            val content = binding.inputText.text.toString()
            if (content.isNotEmpty()) {
                generateQRCode(content)
            }
        }

        return binding.root
    }

    private fun generateQRCode(content: String) {
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)

            binding.qrImage.setImageBitmap(bitmap)

            // Save to history
            viewModel.insert(QRCode(content = content, type = "Generated", timestamp = System.currentTimeMillis()))

            // Share option
            binding.shareButton.visibility = View.VISIBLE
            binding.shareButton.setOnClickListener {
                shareQRCode(bitmap, content)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareQRCode(bitmap: Bitmap, content: String) {
        val cachePath = File(requireContext().cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "qr_code.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, content)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}