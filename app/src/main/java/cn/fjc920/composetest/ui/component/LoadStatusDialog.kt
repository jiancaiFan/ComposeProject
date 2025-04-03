package cn.fjc920.composetest.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cn.fjc920.composetest.viewModel.LoadType

@Composable
fun LoadStatusDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onLoadStateTypeOption: ( LoadType ) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = "测试加载状态") },
            text = { Text(text = "选择一个选项进行测试：") },
            confirmButton = {},
            dismissButton = {
                Column (modifier = Modifier.padding(16.dp).fillMaxWidth()){
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        onLoadStateTypeOption(LoadType.SUCCESS)
                        onDismiss()
                    }) {
                        Text("测试加载成功")
                    }
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        onLoadStateTypeOption(LoadType.FAILURE)
                        onDismiss()
                    }) {
                        Text("测试加载失败")
                    }
                }
            },
            properties = DialogProperties(dismissOnClickOutside = false)
        )
    }
}

@Preview
@Composable
private fun PreViewLoadStatusDialog(){
    LoadStatusDialog(true, {

    }, {

    })
}