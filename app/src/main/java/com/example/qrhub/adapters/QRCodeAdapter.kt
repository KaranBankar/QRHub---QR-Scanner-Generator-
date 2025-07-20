package com.example.qrhub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.qrhub.databinding.ItemQrcodeBinding
import com.example.qrhub.dataclass.QRCode

class QRCodeAdapter : ListAdapter<QRCode, QRCodeAdapter.QRCodeViewHolder>(QRCodeDiffCallback()) {

    class QRCodeViewHolder(private val binding: ItemQrcodeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(qrCode: QRCode) {
            binding.contentText.text = qrCode.content
            binding.typeText.text = qrCode.type
            binding.timestampText.text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", qrCode.timestamp)
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