package com.devkaran.qrhub.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.devkaran.qrhub.R
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devkaran.qrhub.databinding.ItemQrcodeBinding
import com.devkaran.qrhub.dataclass.QRCode

class QRCodeAdapter : ListAdapter<QRCode, QRCodeAdapter.QRCodeViewHolder>(QRCodeDiffCallback()) {

    class QRCodeViewHolder(private val binding: ItemQrcodeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(qrCode: QRCode) {
            val originalContent = qrCode.content.trim()

            // Limit to 100 characters if needed
            val displayContent = if (originalContent.length > 100) {
                originalContent.substring(0, 50) + "..."
            } else {
                originalContent
            }

            // Set content
            binding.contentText.text = displayContent

            // Set type and timestamp
            binding.typeText.text = qrCode.type
            binding.timestampText.text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", qrCode.timestamp)

            // Set type color and icon
            if (qrCode.type.equals("Generated", ignoreCase = true)) {
                binding.typeText.setTextColor(Color.parseColor("#2196F3")) // Blue
                binding.image.setImageResource(R.drawable.add)
            } else {
                binding.typeText.setTextColor(Color.parseColor("#4CAF50")) // Green
                binding.image.setImageResource(R.drawable.scan)
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRCodeViewHolder {
        val binding = ItemQrcodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QRCodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QRCodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class QRCodeDiffCallback : DiffUtil.ItemCallback<QRCode>() {
    override fun areItemsTheSame(oldItem: QRCode, newItem: QRCode): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: QRCode, newItem: QRCode): Boolean {
        return oldItem == newItem
    }
}