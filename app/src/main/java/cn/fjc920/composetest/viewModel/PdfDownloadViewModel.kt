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

    private val _saveResult = MutableStateFlow<String?>(null)
    val saveResult = _saveResult.asStateFlow()

    // 请求权限的方法
    fun requestPermission(permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>) {
        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    // 处理权限请求结果的方法
    fun onPermissionResult(isGranted: Boolean) {
        _permissionGranted.value = isGranted
    }

    // 检查初始权限状态
    fun checkInitialPermissionState(context: Context) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        _permissionGranted.value = granted
    }

    // 保存 PDF 文件到公共目录的方法（异步）
    suspend fun savePdfToPublicDirectory(context: Context, sourceFile: File, fileName: String): String =
        withContext(Dispatchers.IO) {
            if (!sourceFile.exists()) {
                return@withContext "保存失败：文件不存在"
            }

            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savePdfUsingMediaStore(context, sourceFile, fileName)
            } else {
                savePdfToPublicDirectoryLegacy(sourceFile, fileName)
            }

            if (success) {
                "保存成功：${sourceFile.absolutePath}"
            } else {
                "保存失败：未知错误"
            }
        }

    // Android 13 及以上版本：使用 MediaStore 保存文件
    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun savePdfUsingMediaStore(context: Context, sourceFile: File, fileName: String): Boolean =
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
                return@withContext true
            } ?: run {
                return@withContext false
            }
        }

    // Android 13 以下版本：直接保存到公共目录
    private suspend fun savePdfToPublicDirectoryLegacy(sourceFile: File, fileName: String): Boolean =
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
                return@withContext true
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
}