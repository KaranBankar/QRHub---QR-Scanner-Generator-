package com.devkaran.qrhub.fragments

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devkaran.qrhub.R
import com.devkaran.qrhub.databinding.FragmentGenerateBinding
import com.devkaran.qrhub.dataclass.QRCode
import com.devkaran.qrhub.model.QRCodeViewModel
import com.devkaran.qrhub.model.QRCodeViewModelFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GenerateFragment : Fragment() {
    private var _binding: FragmentGenerateBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: QRCodeViewModel
    private var generatedBitmap: Bitmap? = null

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
            } else {
                Toast.makeText(requireContext(), "Please enter content to generate QR code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.downloadButton.setOnClickListener {
            generatedBitmap?.let {
                saveImageToGallery(it, "QR_${System.currentTimeMillis()}")
            } ?: run {
                Toast.makeText(requireContext(), "No QR code generated", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareButton.setOnClickListener {
            generatedBitmap?.let {
                shareQRCode(it, binding.inputText.text.toString())
            } ?: run {
                Toast.makeText(requireContext(), "No QR code generated", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun generateQRCode(content: String) {
        try {
            val selectedColorId = binding.colorPicker.checkedRadioButtonId
            val selectedColor = when (selectedColorId) {
                R.id.colorRed -> Color.RED
                R.id.colorGreen -> Color.GREEN
                R.id.colorBlue -> Color.BLUE
                R.id.colorBlack -> Color.BLACK
                else -> Color.BLACK
            }

            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) selectedColor else Color.WHITE)
                }
            }

            binding.qrImage.setImageBitmap(bitmap)
            generatedBitmap = bitmap
            viewModel.insert(QRCode(content = content, type = "Generated", timestamp = System.currentTimeMillis()))

            binding.shareButton.visibility = View.VISIBLE
            binding.downloadButton.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error generating QR code: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap, filename: String) {
        try {
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRHub")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri == null) {
                Toast.makeText(requireContext(), "Failed to create media entry", Toast.LENGTH_SHORT).show()
                return
            }

            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
            } ?: run {
                Toast.makeText(requireContext(), "Failed to open output stream", Toast.LENGTH_SHORT).show()
                return
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            // Notify gallery of new file
            requireContext().sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))

            Toast.makeText(requireContext(), "QR code saved to Gallery", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error saving QR code: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun shareQRCode(bitmap: Bitmap, content: String) {
        try {
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
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error sharing QR code: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}