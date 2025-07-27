package com.devkaran.qrhub.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devkaran.qrhub.dataclass.QRCode
import com.devkaran.qrhub.roomdb.AppDatabase
import kotlinx.coroutines.launch

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).qrCodeDao()
    val allQRCodes = dao.getAll()

    fun insert(qrCode: QRCode) = viewModelScope.launch {
        dao.insert(qrCode)
    }
}
