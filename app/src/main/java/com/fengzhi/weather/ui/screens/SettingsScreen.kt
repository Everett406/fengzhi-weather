package com.fengzhi.weather.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("设置") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ListItem(
                headlineContent = { Text("深色模式") },
                supportingContent = { Text("跟随系统设置") },
                trailingContent = {
                    Switch(
                        checked = false,
                        onCheckedChange = { }
                    )
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("温度单位") },
                supportingContent = { Text("摄氏度") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("风速单位") },
                supportingContent = { Text("公里/小时") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("关于") },
                supportingContent = { Text("风止天气 v1.0.0") }
            )
        }
    }
}
