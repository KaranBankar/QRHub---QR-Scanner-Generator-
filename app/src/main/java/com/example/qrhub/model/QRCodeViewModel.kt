package com.example.qrhub.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrhub.dataclass.QRCode
import com.example.qrhub.roomdb.AppDatabase
import kotlinx.coroutines.launch

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).qrCodeDao()
    val allQRCodes = dao.getAll()

    fun insert(qrCode: QRCode) = viewModelScope.launch {
        dao.insert(qrCode)
    }
}
