package cn.fjc920.composetest.viewModel

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfDownloadViewModel : ViewModel() {
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted = _permissionGranted.asStateFlow()

    private val _saveResult = MutableStateFlow("")
    val saveResult = _saveResult.asStateFlow()

    // Request permission
    fun requestPermission(permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>) {
        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    // Handle permission request result
    fun onPermissionResult(isGranted: Boolean) {
        _permissionGranted.value = isGranted
    }

    // Check initial permission state
    fun checkInitialPermissionState(context: Context) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        _permissionGranted.value = granted
    }

    // Save PDF file to public directory (asynchronously)
    suspend fun savePdfToPublicDirectory(context: Context, sourceFile: File, fileName: String) =
        withContext(Dispatchers.IO) {
            if (!sourceFile.exists()) {
                _saveResult.value = "Save failed: file does not exist"
                return@withContext
            }

            val newPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savePdfUsingMediaStore(context, sourceFile, fileName)
            } else {
                savePdfToPublicDirectoryLegacy(sourceFile, fileName)
            }

            if (newPath != null) {
                _saveResult.value = "Save successful: $newPath"
            } else {
                _saveResult.value = "Save failed: unknown error"
            }
        }

    // Android 13 and above: save file using MediaStore
    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun savePdfUsingMediaStore(context: Context, sourceFile: File, fileName: String): String? =
        withContext(Dispatchers.IO) {
            val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$fileName"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            }

            val resolver = context.contentResolver
            val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it).use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }
                return@withContext it.toString()
            } ?: run {
                return@withContext null
            }
        }

    // Android 13 and below: save directly to public directory
    private suspend fun savePdfToPublicDirectoryLegacy(sourceFile: File, fileName: String): String? =
        withContext(Dispatchers.IO) {
            val destinationFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            try {
                FileInputStream(sourceFile).use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                return@withContext destinationFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
}