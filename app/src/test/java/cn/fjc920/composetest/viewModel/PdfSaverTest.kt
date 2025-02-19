package cn.fjc920.composetest.viewModel

import android.os.Environment
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MediaStorePdfSaverTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var mediaStorePdfSaver: MediaStorePdfSaver

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        every { context.contentResolver } returns contentResolver
        mediaStorePdfSaver = MediaStorePdfSaver()
    }

    @Test
    fun testSavePdf_success() = runBlocking {
        val sourceFile = mockk<File>(relaxed = true)
        val fileName = "test.pdf"
        val uri = mockk<Uri>(relaxed = true)
        val outputStream = mockk<FileOutputStream>(relaxed = true)
        val inputStream = mockk<FileInputStream>(relaxed = true)

        mockkConstructor(FileInputStream::class)
        mockkConstructor(FileOutputStream::class)

        every { anyConstructed<FileInputStream>().copyTo(any()) } returns 0
        every { contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, any()) } returns uri
        every { contentResolver.openOutputStream(uri) } returns outputStream

        val result = mediaStorePdfSaver.savePdf(context, sourceFile, fileName)

        verify { contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, any()) }
        verify { contentResolver.openOutputStream(uri) }
        verify { anyConstructed<FileInputStream>().copyTo(outputStream) }

        assert(result is SaveResult.Success)
    }
}

class LegacyPdfSaverTest {

    private lateinit var context: Context
    private lateinit var legacyPdfSaver: LegacyPdfSaver

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        legacyPdfSaver = LegacyPdfSaver()
    }

    @Test
    fun testSavePdf_success() = runBlocking {
        val sourceFile = mockk<File>(relaxed = true)
        val fileName = "test.pdf"
        val destinationFile = mockk<File>(relaxed = true)
        val inputStream = mockk<FileInputStream>(relaxed = true)
        val outputStream = mockk<FileOutputStream>(relaxed = true)

        mockkConstructor(FileInputStream::class)
        mockkConstructor(FileOutputStream::class)

        every { anyConstructed<FileInputStream>().copyTo(any()) } returns 0
        mockkStatic(Environment::class)
        every { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) } returns destinationFile

        val result = legacyPdfSaver.savePdf(context, sourceFile, fileName)

        verify { FileInputStream(sourceFile) }
        verify { FileOutputStream(destinationFile) }
        verify { anyConstructed<FileInputStream>().copyTo(outputStream) }

        assert(result is SaveResult.Success)
    }
}