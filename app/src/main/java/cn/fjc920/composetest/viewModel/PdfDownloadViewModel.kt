package cn.fjc920.composetest.viewModel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Appendable

/**
 * Represents the result of a save operation.
 */
sealed class SaveResult {
    data class Success(val path: String) : SaveResult()
    data class Failure(val reason: String) : SaveResult()
}

/**
 * ViewModel for handling PDF download and save operations.
 */
class PdfDownloadViewModel : ViewModel(){
    // StateFlow for permission granted status
    private val _permissionGranted = MutableStateFlow(false)
    var permissionGranted = _permissionGranted.asStateFlow()

    // StateFlow for save result
    private val _saveResult = MutableStateFlow<SaveResult?>(null)
    var saveResult = _saveResult.asStateFlow()

    /**
     * Update the permission granted status.
     * @param isGranted Whether the permission is granted.
     */
    fun onPermissionResult(isGranted: Boolean) {
        _permissionGranted.value = isGranted
    }

    /**
     * Save a PDF file to the public directory asynchronously.
     * @param context The context to use for accessing resources.
     * @param sourceFile The source file to save.
     * @param fileName The name of the file to save.
     */
    suspend fun savePdfToPublicDirectory(context: Context, sourceFile: File, fileName: String) =
        withContext(Dispatchers.IO) {
            if (!sourceFile.exists()) {
                _saveResult.value = SaveResult.Failure("File does not exist")
                return@withContext
            }

            val newPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savePdfUsingMediaStore(context, sourceFile, fileName)
            } else {
                savePdfToPublicDirectoryLegacy(sourceFile, fileName)
            }

            if (newPath != null) {
                _saveResult.value = SaveResult.Success(newPath)
            } else {
                _saveResult.value = SaveResult.Failure("Unknown error")
            }
        }

    /**
     * Save a PDF file using MediaStore for Android 13 and above.
     * @param context The context to use for accessing resources.
     * @param sourceFile The source file to save.
     * @param fileName The name of the file to save.
     * @return The path of the saved file or null if saving failed.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun savePdfUsingMediaStore(context: Context, sourceFile: File, fileName: String): String? =
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

    /**
     * Save a PDF file directly to the public directory for Android 13 and below.
     * @param sourceFile The source file to save.
     * @param fileName The name of the file to save.
     * @return The path of the saved file or null if saving failed.
     */
    suspend fun savePdfToPublicDirectoryLegacy(sourceFile: File, fileName: String): String? =
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