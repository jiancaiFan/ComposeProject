package cn.fjc920.composetest.viewModel

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.io.File
import java.io.FileOutputStream

@ExperimentalCoroutinesApi
class PdfDownloadViewModelTest {
    private lateinit var viewModel: PdfDownloadViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        viewModel = PdfDownloadViewModel()
        context = mock(Context::class.java)
    }

    @Test
    fun testOnPermissionResult() {
        viewModel.onPermissionResult(true)
        assertTrue(viewModel.permissionGranted.value)

        viewModel.onPermissionResult(false)
        assertFalse(viewModel.permissionGranted.value)
    }

    @Test
    fun testCheckInitialPermissionState() {
        `when`(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        viewModel.checkInitialPermissionState(context)
        assertTrue(viewModel.permissionGranted.value)

        `when`(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        viewModel.checkInitialPermissionState(context)
        assertFalse(viewModel.permissionGranted.value)
    }

    @Test
    fun testSavePdfToPublicDirectory_FileDoesNotExist() = runTest {
        val sourceFile = mock(File::class.java)
        `when`(sourceFile.exists()).thenReturn(false)

        viewModel.savePdfToPublicDirectory(context, sourceFile, "test.pdf")
        assertTrue(viewModel.saveResult.value is SaveResult.Failure)
        assertEquals("File does not exist", (viewModel.saveResult.value as SaveResult.Failure).reason)
    }

    @Test
    fun testSavePdfUsingMediaStore_Success() = runTest {
        val resolver = mock(ContentResolver::class.java)
        val uri = mock(Uri::class.java)
        val sourceFile = mock(File::class.java)
        `when`(context.contentResolver).thenReturn(resolver)
        `when`(resolver.insert(any(Uri::class.java), any(ContentValues::class.java))).thenReturn(uri)
        `when`(resolver.openOutputStream(uri)).thenReturn(mock(FileOutputStream::class.java))
        `when`(sourceFile.exists()).thenReturn(true)

        val result = viewModel.savePdfUsingMediaStore(context, sourceFile, "test.pdf")
        assertNotNull(result)
    }

    @Test
    fun testSavePdfToPublicDirectoryLegacy_Success() = runTest {
        val sourceFile = mock(File::class.java)
        val destinationFile = mock(File::class.java)
        `when`(sourceFile.exists()).thenReturn(true)
        `when`(destinationFile.absolutePath).thenReturn("path/to/file")
        val env = mockStatic(Environment::class.java)
        env.`when`<File> { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) }
            .thenReturn(destinationFile)

        val result = viewModel.savePdfToPublicDirectoryLegacy(sourceFile, "test.pdf")
        assertEquals("path/to/file", result)
    }
}