package cn.fjc920.composetest.viewModel

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class PdfDownloadViewModelTest{

    private lateinit var mediaStorePdfSaver: PdfSaver
    private lateinit var legacyPdfSaver: PdfSaver
    private lateinit var viewModel: PdfDownloadViewModel

    @Before
    fun setup(){
        mediaStorePdfSaver = mockk()
        legacyPdfSaver = mockk()
        viewModel = PdfDownloadViewModel(mediaStorePdfSaver, legacyPdfSaver)
    }

    @Test
    fun `test onPermissionResult updates permissionGranted` (){

        viewModel.onPermissionResult(true)
        assertEquals(true, viewModel.permissionGranted.value)

        viewModel.onPermissionResult(false)
        assertEquals(false, viewModel.permissionGranted.value)
    }

    @Test
    suspend fun `test savePdfToPublicDirectory  with non-existent file`(){

        val context = mockk<Context>()
        val sourceFile = mockk<File>()
        val fileName = "testFile.pdf"

        every { sourceFile.exists() } returns false

        viewModel.savePdfToPublicDirectory(context, sourceFile, fileName)
    }
}