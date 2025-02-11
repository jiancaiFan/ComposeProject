package cn.fjc920.composetest.viewModel

import android.Manifest
import org.junit.Before
import org.junit.Test

class PdfDownloadViewModelTest {

    @Before
    fun setup(){

    }

    @Test
    fun testrequestPermission(permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>) {
        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}