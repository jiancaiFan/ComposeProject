package cn.fjc920.composetest.viewModel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class PdfDownloadViewModel(
    private val mediaStorePdfSaver: PdfSaver,
    private val legacyPdfSaver: PdfSaver
) : ViewModel() {
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted = _permissionGranted.asStateFlow()

    private val _saveResult = MutableStateFlow<SaveResult?>(null)
    val saveResult = _saveResult.asStateFlow()

    fun onPermissionResult(isGranted: Boolean) {
        _permissionGranted.update { isGranted }
    }

    suspend fun savePdfToPublicDirectory(context: Context, sourceFile: File, fileName: String) {
        if (!sourceFile.exists()) {
            _saveResult.update { SaveResult.Failure("File does not exist") }
            return
        }

        val newPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStorePdfSaver.savePdf(context, sourceFile, fileName)
        } else {
            legacyPdfSaver.savePdf(context, sourceFile, fileName)
        }

        _saveResult.update { newPath }
    }
}