package cn.fjc920.composetest.uiScreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import cn.fjc920.composetest.ui.uiScreen.DownloadPdfScreen
import cn.fjc920.composetest.viewModel.PdfDownloadViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class DownloadPdfScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = PdfDownloadViewModel()

    @Test
    fun testDownloadPdfButton() {
        mockViewModel.permissionGranted = MutableStateFlow(true)
        mockViewModel.saveResult = MutableStateFlow(null)

        composeTestRule.setContent {
            DownloadPdfScreen()
        }

        composeTestRule.onNodeWithText("Download PDF").assertExists().performClick()
    }

    @Test
    fun testSnackBarOnDownloadSuccess() {
        val saveResultFlow = MutableStateFlow<`SaveResult.kt`?>(`SaveResult.kt`.Success("/path/to/downloaded-file.pdf"))
        mockViewModel.permissionGranted = MutableStateFlow(true)
        mockViewModel.saveResult = saveResultFlow

        composeTestRule.setContent {
            DownloadPdfScreen()
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Saved: /path/to/downloaded-file.pdf").assertExists()
    }

    @Test
    fun testSnackBarOnDownloadFailure() {
        val saveResultFlow = MutableStateFlow<`SaveResult.kt`?>(`SaveResult.kt`.Failure("File does not exist"))
        mockViewModel.permissionGranted = MutableStateFlow(true)
        mockViewModel.saveResult = saveResultFlow

        composeTestRule.setContent {
            DownloadPdfScreen()
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Save failed: File does not exist").assertExists()
    }

    @Test
    fun testPermissionRequest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        mockViewModel.permissionGranted = MutableStateFlow(false)

        composeTestRule.setContent {
            DownloadPdfScreen()
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            composeTestRule.onNodeWithContentDescription("Download").performClick()
            assert(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        }
    }
}