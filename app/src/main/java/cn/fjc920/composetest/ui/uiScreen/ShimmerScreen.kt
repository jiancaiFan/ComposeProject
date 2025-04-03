package cn.fjc920.composetest.ui.uiScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.placeholder.*
import com.google.accompanist.placeholder.material.*

/**
 *      time: 2025-02-18
 *      role: 樊建财
 *      contentDescription: 测试Android中的骨架屏
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShimmerScreen() {

    val naviController = rememberNavController()
    var isLoading by remember { mutableStateOf(true) }

    // Simulate a network request
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        isLoading = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("我的应用栏") },
                navigationIcon = {
                    IconButton(onClick = { naviController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
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

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .placeholder(
                                    visible = isLoading,
                                    color = Color.Gray,
                                    highlight = PlaceholderHighlight.shimmer(),
                                    shape = RectangleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .placeholder(
                                    visible = isLoading,
                                    color = Color.Gray,
                                    highlight = PlaceholderHighlight.shimmer(),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewShimmer() {
    ShimmerScreen()
}