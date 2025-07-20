package com.example.qrhub.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.qrhub.dataclass.QRCode
import kotlinx.coroutines.flow.Flow

@Dao
interface QRCodeDao {
    @Query("SELECT * FROM qr_codes ORDER BY timestamp DESC")
    fun getAll(): Flow<List<QRCode>>

    @Insert
    suspend fun insert(qrCode: QRCode)
}