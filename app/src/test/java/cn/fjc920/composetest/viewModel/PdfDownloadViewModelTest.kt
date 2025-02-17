package cn.fjc920.composetest.viewModel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PdfDownloadViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: TestScope

    private lateinit var viewModel: PdfDownloadViewModel

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var mockUri: Uri
    private lateinit var mockPublicDirectory: File

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)
        mockContext = mockk()
        mockContentResolver = mockk()
        mockUri = mockk()
        mockPublicDirectory = File("mock" + File.separator + "downloads")

        viewModel = PdfDownloadViewModel()
        every { mockContext.contentResolver } returns mockContentResolver

        // Mock Environment.getExternalStoragePublicDirectory method
        mockkStatic(Environment::class)
        every { Environment.getExternalStoragePublicDirectory(any()) } returns mockPublicDirectory
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
        unmockkAll() // Unmock all static mocks
    }

    @Test
    fun testOnPermissionResult() = testScope.runTest {
        viewModel.onPermissionResult(true)
        assertEquals(true, viewModel.permissionGranted.value)

        viewModel.onPermissionResult(false)
        assertEquals(false, viewModel.permissionGranted.value)
    }

    @Test
    fun testSavePdfToPublicDirectory_FileDoesNotExist() = testScope.runTest {
        val sourceFile = File("non_existent_file.pdf")
        viewModel.savePdfToPublicDirectory(mockContext, sourceFile, "test.pdf")
        assertTrue(viewModel.saveResult.first() is SaveResult.Failure)
        assertEquals("File does not exist", (viewModel.saveResult.first() as SaveResult.Failure).reason)
    }

    @Test
    fun testSavePdfToPublicDirectory_FileExists_Android10AndAbove() = testScope.runTest {
        val sourceFile = File.createTempFile("source", ".pdf")
        val fileName = "test.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            every { mockContentResolver.insert(any(), any()) } returns mockUri
            every { mockContentResolver.openOutputStream(mockUri) } returns FileOutputStream(File.createTempFile("destination", ".pdf"))

            viewModel.savePdfToPublicDirectory(mockContext, sourceFile, fileName)
            assertTrue(viewModel.saveResult.first() is SaveResult.Success)
        }
    }

    @Test
    fun testSavePdfToPublicDirectory_FileExists_BelowAndroid10() = testScope.runTest {
        val sourceFile = File.createTempFile("source", ".pdf")
        val fileName = "test.pdf"

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            viewModel.savePdfToPublicDirectory(mockContext, sourceFile, fileName)
            assertTrue(viewModel.saveResult.first() is SaveResult.Success)
        }
    }

    @Test
    fun testSavePdfToPublicDirectoryLegacy_Success() = testScope.runTest {
        val sourceFile = File.createTempFile("source", ".pdf")
        val fileName = "test.pdf"
        val result = viewModel.savePdfToPublicDirectoryLegacy(sourceFile, fileName)
        assertEquals(mockPublicDirectory.absolutePath + File.separator + "test.pdf", result)
    }

    @Test
    fun testSavePdfUsingMediaStore_InsertUriFailure() = testScope.runTest {
        val sourceFile = File.createTempFile("source", ".pdf")
        val fileName = "test.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            every { mockContentResolver.insert(any(), any()) } returns null

            val result = viewModel.savePdfUsingMediaStore(mockContext, sourceFile, fileName)
            assertEquals(null, result)
        }
    }

    @Test
    fun testSavePdfUsingMediaStore_OpenOutputStreamFailure() = testScope.runTest {
        val sourceFile = File.createTempFile("source", ".pdf")
        val fileName = "test.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            every { mockContentResolver.insert(any(), any()) } returns mockUri
            every { mockContentResolver.openOutputStream(mockUri) } returns null

            val result = viewModel.savePdfUsingMediaStore(mockContext, sourceFile, fileName)
            assertEquals(null, result)
        }
    }

    @Test
    fun testSavePdfUsingMediaStore_Success() = testScope.runTest {
        val sourceFile = File.createTempFile("source", ".pdf")
        val fileName = "test.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            every { mockContentResolver.insert(any(), any()) } returns mockUri
            every { mockContentResolver.openOutputStream(mockUri) } returns FileOutputStream(File.createTempFile("destination", ".pdf"))

            val result = viewModel.savePdfUsingMediaStore(mockContext, sourceFile, fileName)
            assertEquals(mockUri.toString(), result)
        }
    }

    @Test
    fun testSavePdfToPublicDirectoryLegacy_Failure() = testScope.runTest {
        val sourceFile = File("non_existent_source.pdf")
        val fileName = "test.pdf"
        val result = viewModel.savePdfToPublicDirectoryLegacy(sourceFile, fileName)
        assertEquals(null, result)
    }
}