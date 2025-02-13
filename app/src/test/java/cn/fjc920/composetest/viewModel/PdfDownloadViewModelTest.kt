package cn.fjc920.composetest.viewModel

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.*

@OptIn(ExperimentalCoroutinesApi::class)
class PdfDownloadViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PdfDownloadViewModel
    private val context: Context = mockk(relaxed = true)
    private val sourceFile: File = mockk(relaxed = true)
    private val fileName = "test.pdf"

    @Before
    fun setUp() {
        viewModel = PdfDownloadViewModel()
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test permission result updates state`() {
        viewModel.onPermissionResult(true)
        assert(viewModel.permissionGranted.value)

        viewModel.onPermissionResult(false)
        assert(!viewModel.permissionGranted.value)
    }

    @Test
    fun `test savePdfToPublicDirectory success`() = runTest {
        every { sourceFile.exists() } returns true
        mockkStatic(Environment::class)
        val destinationFile: File = mockk(relaxed = true)
        every { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) } returns destinationFile

        mockkConstructor(FileInputStream::class)
        mockkConstructor(FileOutputStream::class)

        every { anyConstructed<FileInputStream>().use(any<((FileInputStream) -> Unit)>()) } answers {
            val block = this.invocation.args[0] as (FileInputStream) -> Unit
            block(mockk(relaxed = true))
        }

        every { anyConstructed<FileOutputStream>().use(any<((FileOutputStream) -> Unit)>()) } answers {
            val block = this.invocation.args[0] as (FileOutputStream) -> Unit
            block(mockk(relaxed = true))
        }

        viewModel.savePdfToPublicDirectory(context, sourceFile, fileName)

        verify { sourceFile.exists() }
        verify { anyConstructed<FileInputStream>().use(any<((FileInputStream) -> Unit)>()) }
        verify { anyConstructed<FileOutputStream>().use(any<((FileOutputStream) -> Unit)>()) }
        assert(viewModel.saveResult.value is SaveResult.Success)
    }

    @Test
    fun `test savePdfToPublicDirectory failure`() = runTest {
        every { sourceFile.exists() } returns false

        viewModel.savePdfToPublicDirectory(context, sourceFile, fileName)

        coVerify { sourceFile.exists() }
        assert(viewModel.saveResult.value is SaveResult.Failure)
    }

    @Test
    fun `test savePdfToPublicDirectory with MediaStore success`() = runTest {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            every { sourceFile.exists() } returns true
            val contentResolver: ContentResolver = mockk(relaxed = true)
            val uri: Uri = mockk(relaxed = true)
            val outputStream: OutputStream = mockk(relaxed = true)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$fileName")
            }

            every { context.contentResolver } returns contentResolver
            every { contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) } returns uri
            every { contentResolver.openOutputStream(uri) } returns outputStream

            mockkConstructor(FileInputStream::class)

            coEvery { anyConstructed<FileInputStream>().use(any<((FileInputStream) -> Unit)>()) } answers {
                val block = this.invocation.args[0] as (FileInputStream) -> Unit
                block(mockk(relaxed = true))
            }

            coEvery { outputStream.use(any<((OutputStream) -> Unit)>()) } answers {
                val block = this.invocation.args[0] as (OutputStream) -> Unit
                block(mockk(relaxed = true))
            }

            viewModel.savePdfToPublicDirectory(context, sourceFile, fileName)

            coVerify { sourceFile.exists() }
            coVerify { anyConstructed<FileInputStream>().use(any<((FileInputStream) -> Unit)>()) }
            coVerify { outputStream.use(any<((OutputStream) -> Unit)>()) }
            assert(viewModel.saveResult.value is SaveResult.Success)
        }
    }

    @Test
    fun `test savePdfToPublicDirectory with MediaStore failure`() = runTest {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            every { sourceFile.exists() } returns true
            val contentResolver: ContentResolver = mockk(relaxed = true)

            every { context.contentResolver } returns contentResolver
            every { contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, any()) } returns null

            viewModel.savePdfToPublicDirectory(context, sourceFile, fileName)

            coVerify { sourceFile.exists() }
            assert(viewModel.saveResult.value is SaveResult.Failure)
        }
    }
}