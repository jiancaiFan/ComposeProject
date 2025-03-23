package cn.fjc920.composetest.uiScreen

import android.content.Context
import android.widget.Toast
import cn.fjc920.composetest.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cn.fjc920.composetest.ui.theme.ComposeTestTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoreActionsScreen(onBackClick: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = { Text("我的应用栏") },
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            Text("MoreActions")
            CircleComponentsRow()
        }
    }
}

@Composable
internal fun CircleComponentsRow() {

    val context: Context = LocalContext.current
    val actionState = remember { false }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CircleComponent(
            iconResId = R.drawable.ic_launcher_foreground,
            name = "Component 1",
            modifier = Modifier.weight(1f),
            actionClick = {
                Toast.makeText(context, "Component 1 Clicked", Toast.LENGTH_SHORT).show()
            })
        CircleComponent(
            iconResId = R.drawable.ic_launcher_foreground,
            name = "Component 2",
            modifier = Modifier.weight(1f),
            actionClick = {
                Toast.makeText(context, "Component 1 Clicked", Toast.LENGTH_SHORT).show()
            })
        if (actionState) {
            CircleComponent(
                iconResId = R.drawable.ic_launcher_foreground,
                name = "Component 3",
                modifier = Modifier.weight(1f),
                actionClick = {
                    Toast.makeText(context, "Component 1 Clicked", Toast.LENGTH_SHORT).show()
                })
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
internal fun CircleComponent(
    iconResId: Int, name: String, modifier: Modifier = Modifier, actionClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(16.dp)
            .clickable(onClick = actionClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .background(Color.Gray, shape = CircleShape)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = name, fontSize = 20.sp, color = Color.Black)
    }
}

@Preview
@Composable
private fun PreviewMoreActionsScreen() {
    ComposeTestTheme {
        MoreActionsScreen {}
    }
}