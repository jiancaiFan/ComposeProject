package cn.fjc920.composetest.ui.uiScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.fjc920.composetest.ui.component.LoadStatusDialog
import cn.fjc920.composetest.viewModel.FundListViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadMoreScreen(
    title: String,
    onBackClick: () -> Unit,
    viewModel: FundListViewModel,
) {

    val isShowDialog = remember { mutableStateOf(false) }
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasError by viewModel.hasError.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack, contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    LoadStatusDialog(
                        showDialog = isShowDialog.value,
                        onDismiss = {
                            isShowDialog.value = false
                        },
                        onLoadStateTypeOption = {
                            viewModel.setLoadType(it)
                        }
                    )
                    IconButton(
                        onClick = {
                            isShowDialog.value = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings, contentDescription = "选择测试模式"
                        )
                    }
                }
            )
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumnWithLoadMore(
                items = items,
                isLoading = isLoading,
                hasError = hasError,
                onLoadMore = { viewModel.loadMoreItems() },
                onRetry = { viewModel.retryLoadMoreItems() })
        }
    }
}

@Composable
fun LazyColumnWithLoadMore(
    items: List<String>,
    isLoading: Boolean,
    hasError: Boolean,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // LaunchedEffect to trigger onLoadMore when list is scrolled to the last item
    LaunchedEffect(listState) {
        coroutineScope.launch {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .map { visibleItems ->
                    // Calculate if the user is at the last item of the list
                    val totalItemCount = listState.layoutInfo.totalItemsCount
                    val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: 0

                    lastVisibleItemIndex >= (totalItemCount - 1) && !isLoading && !hasError
                }
                .distinctUntilChanged()
                .collect { shouldLoadMore ->
                    if (shouldLoadMore) {
                        onLoadMore()
                    }
                }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            ItemCard(text = item)
        }
        item {
            if (isLoading) {
                Box(modifier = Modifier.fillParentMaxWidth().height(40.dp)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            if (hasError) {
                Box(modifier = Modifier.fillParentMaxWidth()) {
                    Text(
                        text = "加载失败，上滑重新加载",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = text)
        }
    }
}