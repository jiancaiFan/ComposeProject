package cn.fjc920.composetest.viewModel

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE)
class PdfDownloadViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PdfDownloadViewModel
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = PdfDownloadViewModel()
        context = ApplicationProvider.getApplicationContext()
        contentResolver = context.contentResolver
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testOnPermissionResult() = runBlocking {
        viewModel.onPermissionResult(true)
        Assert.assertEquals(true, viewModel.permissionGranted.first())
    }

    @Test
    fun testSavePdfToPublicDirectory_FileDoesNotExist() = runBlocking {
        val file = File("nonexistentFile.pdf")
        viewModel.savePdfToPublicDirectory(context, file, "test.pdf")
        Assert.assertEquals(SaveResult.Failure("File does not exist"), viewModel.saveResult.first())
    }

    @Test
    fun testSavePdfToPublicDirectory_Success_Legacy() = runBlocking<Unit> {
        val file = File.createTempFile("test", ".pdf")
        file.writeText("Test content")

        val destinationFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "test.pdf"
        )

        viewModel.savePdfToPublicDirectory(context, file, "test.pdf")
        Assert.assertEquals(SaveResult.Success(destinationFile.absolutePath), viewModel.saveResult.first())

        // Clean up
        file.delete()
        destinationFile.delete()
    }

    @Test
    fun testSavePdfToPublicDirectory_Success_MediaStore() = runBlocking {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val file = File.createTempFile("test", ".pdf")
            file.writeText("Test content")

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "test.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                contentResolver.openOutputStream(it).use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }
            }

            viewModel.savePdfToPublicDirectory(context, file, "test.pdf")
            Assert.assertEquals(SaveResult.Success(uri.toString()), viewModel.saveResult.first())

            // Clean up
            uri?.let { contentResolver.delete(it, null, null) }
            file.delete()
        }
    }
}