package cn.fjc920.composetest.ui.uiScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilteredTextFieldScreen() {

    val naviController = rememberNavController()

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
        FilteredTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun FilteredTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newText ->
            val filtered = newText.filterAndLimitInput()
            if (filtered == newText) {
                onValueChange(filtered)
            }
            // 非法字符自动过滤，不会出现在输入框
        },
        label = { Text("请输入内容") },
        modifier = modifier,
        singleLine = true
    )
}

private fun String.filterAndLimitInput(maxLength: Int = 50): String {
    val allowedRegex = Regex("[A-Za-z0-9 ,#%()*\\/;:=?&@+!$€\\-]")
    return this.filter { allowedRegex.matches(it.toString()) }.take(maxLength)
}
