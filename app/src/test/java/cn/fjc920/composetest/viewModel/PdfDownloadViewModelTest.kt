package cn.fjc920.composetest.viewModel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class PdfDownloadViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var viewModel: PdfDownloadViewModel
    private lateinit var mediaStorePdfSaver: PdfSaver
    private lateinit var legacyPdfSaver: PdfSaver
    private lateinit var context: Context
    private lateinit var sourceFile: File

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mediaStorePdfSaver = mockk()
        legacyPdfSaver = mockk()
        context = mockk()
        sourceFile = mockk()
        viewModel = PdfDownloadViewModel(mediaStorePdfSaver, legacyPdfSaver)

        coEvery { sourceFile.exists() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `savePdfToPublicDirectory should update saveResult with success on API below 29`() = testScope.runTest {
        // Given
        val fileName = "destination.pdf"
        val expectedPath = SaveResult.Success("path/to/saved/file.pdf")
        coEvery { legacyPdfSaver.savePdf(context, sourceFile, fileName) } returns expectedPath

        // When
        viewModel.savePdfToPublicDirectory(context, sourceFile, fileName)
        //testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedPath, viewModel.saveResult.first())
    }
}