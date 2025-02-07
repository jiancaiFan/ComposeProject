package cn.fjc920.composetest.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.ContextCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = MainViewModel()
    }

    @Test
    fun testCheckInitialPermissionState_granted() {
        Mockito.`when`(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .thenReturn(PackageManager.PERMISSION_GRANTED)

        viewModel.checkInitialPermissionState(context)

        Assert.assertTrue(viewModel.permissionGranted.value)
    }

    @Test
    fun testCheckInitialPermissionState_denied() {
        Mockito.`when`(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .thenReturn(PackageManager.PERMISSION_DENIED)

        viewModel.checkInitialPermissionState(context)

        Assert.assertFalse(viewModel.permissionGranted.value)
    }

    @Test
    fun testRequestPermission() {
        viewModel.requestPermission(permissionLauncher)
        Mockito.verify(permissionLauncher).launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    @Test
    fun testOnPermissionResult_granted() {
        viewModel.onPermissionResult(true)
        Assert.assertTrue(viewModel.permissionGranted.value)
    }

    @Test
    fun testOnPermissionResult_denied() {
        viewModel.onPermissionResult(false)
        Assert.assertFalse(viewModel.permissionGranted.value)
    }

    @Test
    fun testSavePdfToPublicDirectory_fileNotExist() = runBlockingTest {
        val sourceFilePath = "non_existent_file.pdf"
        val result = viewModel.savePdfToPublicDirectory(context, sourceFilePath)
        Assert.assertFalse(result)
        Assert.assertEquals("保存失败：文件不存在", viewModel.saveResult.value)
    }
}