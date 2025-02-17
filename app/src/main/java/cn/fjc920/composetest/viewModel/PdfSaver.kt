package cn.fjc920.composetest.viewModel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

interface PdfSaver {
    suspend fun savePdf(context: Context, sourceFile: File, fileName: String): SaveResult
}

class MediaStorePdfSaver : PdfSaver {
    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun savePdf(context: Context, sourceFile: File, fileName: String): SaveResult =
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
                return@withContext SaveResult.Success(it.toString())
            } ?: run {
                return@withContext SaveResult.Failure("Failed to insert URI")
            }
        }
}

class LegacyPdfSaver : PdfSaver {
    override suspend fun savePdf(context: Context, sourceFile: File, fileName: String): SaveResult =
        withContext(Dispatchers.IO) {
            val destinationFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            return@withContext try {
                FileInputStream(sourceFile).use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                SaveResult.Success(destinationFile.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
                SaveResult.Failure("Failed to save file")
            }
        }
}