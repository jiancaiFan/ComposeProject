package cn.fjc920.composetest.uiScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.fjc920.composetest.viewModel.FundListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadMoreScreen(
    title: String, viewModel: FundListViewModel, onBackClick: () -> Unit
) {

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

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            ItemCard(text = item)
        }
        item {
            if (isLoading) {
                Box(modifier = Modifier.fillParentMaxWidth()) {
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

    LaunchedEffect(listState) {
        var lastFirstVisibleItemIndex = 0
        snapshotFlow { listState.firstVisibleItemIndex to listState.layoutInfo.totalItemsCount }
            .collect { (firstVisibleItemIndex, totalItemsCount) ->
                if (!isLoading && !hasError && firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size >= totalItemsCount) {
                    onLoadMore()
                } else if (hasError && firstVisibleItemIndex < lastFirstVisibleItemIndex) {
                    onRetry()
                }
                lastFirstVisibleItemIndex = firstVisibleItemIndex
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LoadMoreScreen("LoaMore", viewModel()) {

    }
}