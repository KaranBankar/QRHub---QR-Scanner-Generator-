package com.devkaran.qrhub.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_codes")
data class QRCode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val type: String,
    val timestamp: Long
)