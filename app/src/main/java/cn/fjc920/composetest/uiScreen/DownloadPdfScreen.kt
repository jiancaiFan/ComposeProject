package cn.fjc920.composetest.uiScreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cn.fjc920.composetest.viewModel.PdfDownloadViewModel
import cn.fjc920.composetest.viewModel.SaveResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPdfScreen() {
    val context = LocalContext.current
    val viewModel = PdfDownloadViewModel()
    checkInitialPermissionState(context, viewModel)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }
    val permissionGranted by viewModel.permissionGranted.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    var downloadPath by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var fileName by remember { mutableStateOf("") }
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("我的应用栏") },
                navigationIcon = {
                    IconButton(onClick = { /* 导航图标点击事件 */ }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || permissionGranted) {
                            handleDownloadButtonClick(
                                context = context,
                                coroutineScope = coroutineScope,
                                viewModel = viewModel,
                                downloadPath = downloadPath,
                                fileName = fileName.ifBlank { "downloaded-file" }
                            )
                        } else {
                            // Request permission
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.TwoTone.Share,
                            contentDescription = "Download"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    downloadPath = downloadPdf(context)
                }
            }, modifier = Modifier.padding(top = 16.dp)) {
                Text(text = "Download PDF")
            }

            downloadPath?.let {
                Text(text = "Downloaded to: $it", modifier = Modifier.padding(top = 16.dp))
            }

            LaunchedEffect(saveResult) {
                saveResult?.let { result ->
                    when (result) {
                        is SaveResult.Success -> {
                            snackBarHostState.showSnackbar(
                            message = "Saved: ${result.path}",
                            actionLabel = null,
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                            )
                        }
                        is SaveResult.Failure -> {
                            snackBarHostState.showSnackbar(
                            message = "Save failed: ${result.reason}",
                            actionLabel = null,
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
        }
    }
}

// Check initial permission state
fun checkInitialPermissionState(context: Context, viewModel: PdfDownloadViewModel) {
    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    viewModel.onPermissionResult(granted)
}

suspend fun downloadPdf(context: Context): String? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf") // 测试用的PDF文件URL
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "downloaded-file.pdf")
            val inputStream: InputStream = connection.inputStream
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }
            outputStream.close()
            inputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun handleDownloadButtonClick(
    context: Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    viewModel: PdfDownloadViewModel,
    downloadPath: String?,
    fileName: String
) {
    if (downloadPath == null) {
        Toast.makeText(context, "请先下载PDF文件", Toast.LENGTH_SHORT).show()
        return
    }
    coroutineScope.launch {
        viewModel.savePdfToPublicDirectory(context, File(downloadPath), "$fileName.pdf")
    }
}