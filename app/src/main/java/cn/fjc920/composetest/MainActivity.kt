package cn.fjc920.composetest

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import cn.fjc920.composetest.ui.theme.ComposeTestTheme
import cn.fjc920.composetest.viewModel.PdfDownloadViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTestTheme {
                // 创建 ViewModel 实例
                val viewModel = PdfDownloadViewModel()

                // 在应用启动时检查权限状态
                viewModel.checkInitialPermissionState(this)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("我的应用栏") },
                            navigationIcon = {
                                IconButton(onClick = { /* 导航图标点击事件 */ }) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Filled.Close,
                                        contentDescription = "Menu"
                                    )
                                }
                            },
                            actions = {
                                val context = LocalContext.current
                                val coroutineScope = rememberCoroutineScope()  // 使用协程作用域
                                val permissionLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.RequestPermission()
                                ) { isGranted ->
                                    viewModel.onPermissionResult(isGranted)  // 将结果传递给 ViewModel
                                }

                                val permissionGranted by viewModel.permissionGranted.collectAsState()
                                val saveResult by viewModel.saveResult.collectAsState()

                                IconButton(onClick = {
                                    handleDownloadButtonClick(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        viewModel = viewModel,
                                        permissionGranted = permissionGranted,
                                        saveResult = saveResult,
                                        permissionLauncher = permissionLauncher
                                    )
                                }) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                                        contentDescription = "Download PDF"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

fun handleDownloadButtonClick(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    viewModel: PdfDownloadViewModel,
    permissionGranted: Boolean,
    saveResult: String?,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // 如果权限已授予，异步保存 PDF 文件
        coroutineScope.launch {
            viewModel.savePdfToPublicDirectory(context, "测试.pdf")  // 确保路径正确
            Toast.makeText(context, saveResult, Toast.LENGTH_SHORT).show()
        }
    } else {
        if (permissionGranted) {
            // 如果权限已授予，异步保存 PDF 文件
            coroutineScope.launch {
                viewModel.savePdfToPublicDirectory(context, "测试.pdf")  // 确保路径正确
                Toast.makeText(context, saveResult, Toast.LENGTH_SHORT).show()
            }
        } else {
            // 如果权限未授予，请求权限
            viewModel.requestPermission(permissionLauncher)
        }
    }
}