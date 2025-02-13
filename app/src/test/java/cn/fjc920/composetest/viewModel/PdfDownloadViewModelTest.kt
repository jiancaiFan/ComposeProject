package cn.fjc920.composetest.viewModel

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PdfDownloadViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PdfDownloadViewModel
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var sourceFile: File
    private lateinit var observer: Observer<SaveResult?>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PdfDownloadViewModel()
        context = mockk()
        contentResolver = mockk()
        sourceFile = mockk()
        observer = mockk(relaxed = true)

        every { context.contentResolver } returns contentResolver
        every { sourceFile.exists() } returns true
        viewModel.saveResult.observeForever(observer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        viewModel.saveResult.removeObserver(observer)
    }

    @Test
    fun `test onPermissionResult updates permissionGranted state`() {
        viewModel.onPermissionResult(true)
        assertEquals(true, viewModel.permissionGranted.value)

        viewModel.onPermissionResult(false)
        assertEquals(false, viewModel.permissionGranted.value)
    }

    @Test
    fun `test savePdfToPublicDirectory with non-existent file`() = runTest {
        every { sourceFile.exists() } returns false

        viewModel.savePdfToPublicDirectory(context, sourceFile, "test.pdf")

        coVerify { observer.onChanged(SaveResult.Failure("File does not exist")) }
    }

    @Test
    fun `test savePdfToPublicDirectory saves successfully on Android TIRAMISU and above`() = runTest {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val uri = mockk<Uri>()
            val outputStream = mockk<FileOutputStream>(relaxed = true)
            val inputStream = mockk<FileInputStream>(relaxed = true)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "test.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            every { contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) } returns uri
            every { contentResolver.openOutputStream(uri) } returns outputStream
            every { FileInputStream(sourceFile) } returns inputStream

            viewModel.savePdfToPublicDirectory(context, sourceFile, "test.pdf")

            coVerify { observer.onChanged(SaveResult.Success(uri.toString())) }
        }
    }

    @Test
    fun `test savePdfToPublicDirectoryLegacy saves successfully`() = runTest {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val destinationFile = mockk<File>()
            val outputStream = mockk<FileOutputStream>(relaxed = true)
            val inputStream = mockk<FileInputStream>(relaxed = true)

            mockkStatic(Environment::class)
            every { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) } returns destinationFile
            every { File(destinationFile, "test.pdf") } returns destinationFile
            every { FileInputStream(sourceFile) } returns inputStream
            every { FileOutputStream(destinationFile) } returns outputStream
            every { destinationFile.absolutePath } returns "/path/to/test.pdf"

            viewModel.savePdfToPublicDirectory(context, sourceFile, "test.pdf")

            coVerify { observer.onChanged(SaveResult.Success("/path/to/test.pdf")) }
        }
    }
}