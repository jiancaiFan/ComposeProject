package cn.fjc920.composetest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cn.fjc920.composetest.ui.theme.ComposeTestTheme
import cn.fjc920.composetest.uiScreen.DownloadPdfScreen
import cn.fjc920.composetest.uiScreen.ShimmerScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTestTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                MainContent(navController)
            }
            composable("download") {
                DownloadPdfScreen()
            }
            composable("shimmerScreen"){
                ShimmerScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent(navController: NavController) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("PDF下载") },
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { navController.navigate("download") },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Go to Download Screen")
                }

                Button(
                    onClick = { navController.navigate("shimmerScreen") },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Go to Shimmer Screen")
                }
            }
        }
    }
}