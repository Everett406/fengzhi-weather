package com.fengzhi.weather.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fengzhi.weather.data.local.SettingsPreferences
import com.fengzhi.weather.data.model.City
import com.fengzhi.weather.ui.screens.settings.components.AboutSection
import com.fengzhi.weather.ui.screens.settings.components.AddCityDialog
import com.fengzhi.weather.ui.screens.settings.components.CityManagementItem

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 状态
    val cityList by viewModel.cityList.collectAsState()
    val currentCityId by viewModel.currentCityId.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val refreshInterval by viewModel.refreshInterval.collectAsState()
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    // 对话框状态
    var showAddCityDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showRefreshIntervalDialog by remember { mutableStateOf(false) }
    var showTemperatureUnitDialog by remember { mutableStateOf(false) }
    var showWindUnitDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("设置") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 城市管理部分
            item {
                SettingsSectionTitle(title = "城市管理")
            }

            // 城市列表
            if (cityList.isEmpty()) {
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "暂无保存的城市",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "点击右侧按钮添加城市",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Filled.LocationCity,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { showAddCityDialog = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "添加城市"
                                )
                            }
                        }
                    )
                }
            } else {
                items(cityList, key = { it.id }) { city ->
                    CityManagementItem(
                        city = city,
                        isCurrentCity = city.id == currentCityId,
                        onSelect = { viewModel.setCurrentCity(city.id) },
                        onDelete = { viewModel.deleteCity(city.id) }
                    )
                }

                // 添加城市按钮
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "添加城市",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable { showAddCityDialog = true }
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 显示设置部分
            item {
                SettingsSectionTitle(title = "显示设置")
            }

            // 主题设置
            item {
                ListItem(
                    headlineContent = { Text("主题模式") },
                    supportingContent = {
                        Text(viewModel.getThemeModeDisplayName(themeMode))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = if (themeMode == SettingsPreferences.THEME_DARK) {
                                Icons.Filled.DarkMode
                            } else {
                                Icons.Filled.LightMode
                            },
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { showThemeDialog = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // 刷新间隔设置
            item {
                ListItem(
                    headlineContent = { Text("自动刷新间隔") },
                    supportingContent = {
                        Text(viewModel.getRefreshIntervalDisplayName(refreshInterval))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { showRefreshIntervalDialog = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 单位设置部分
            item {
                SettingsSectionTitle(title = "单位设置")
            }

            // 温度单位
            item {
                ListItem(
                    headlineContent = { Text("温度单位") },
                    supportingContent = {
                        Text(viewModel.getTemperatureUnitDisplayName(temperatureUnit))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Thermostat,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { showTemperatureUnitDialog = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // 风速单位
            item {
                ListItem(
                    headlineContent = { Text("风速单位") },
                    supportingContent = {
                        Text(viewModel.getWindUnitDisplayName(windUnit))
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.WindPower,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { showWindUnitDialog = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 关于部分
            item {
                AboutSection()
            }
        }
    }

    // 添加城市对话框
    if (showAddCityDialog) {
        AddCityDialog(
            onDismiss = {
                showAddCityDialog = false
                viewModel.clearSearch()
            },
            onAddCity = { location ->
                viewModel.addCity(location)
                showAddCityDialog = false
            },
            searchState = searchState,
            searchResults = searchResults,
            onSearch = { query -> viewModel.searchCity(query) }
        )
    }

    // 主题选择对话框
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                viewModel.setThemeMode(mode)
                showThemeDialog = false
            }
        )
    }

    // 刷新间隔选择对话框
    if (showRefreshIntervalDialog) {
        RefreshIntervalDialog(
            currentInterval = refreshInterval,
            onDismiss = { showRefreshIntervalDialog = false },
            onSelect = { interval ->
                viewModel.setRefreshInterval(interval)
                showRefreshIntervalDialog = false
            }
        )
    }

    // 温度单位选择对话框
    if (showTemperatureUnitDialog) {
        TemperatureUnitDialog(
            currentUnit = temperatureUnit,
            onDismiss = { showTemperatureUnitDialog = false },
            onSelect = { unit ->
                viewModel.setTemperatureUnit(unit)
                showTemperatureUnitDialog = false
            }
        )
    }

    // 风速单位选择对话框
    if (showWindUnitDialog) {
        WindUnitDialog(
            currentUnit = windUnit,
            onDismiss = { showWindUnitDialog = false },
            onSelect = { unit ->
                viewModel.setWindUnit(unit)
                showWindUnitDialog = false
            }
        )
    }
}

/**
 * 设置部分标题
 */
@Composable
private fun SettingsSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * 主题选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        SettingsPreferences.THEME_SYSTEM to "跟随系统",
        SettingsPreferences.THEME_LIGHT to "浅色模式",
        SettingsPreferences.THEME_DARK to "深色模式"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("主题模式") },
        text = {
            Column {
                options.forEach { (value, label) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            RadioButton(
                                selected = currentTheme == value,
                                onClick = { onSelect(value) }
                            )
                        },
                        modifier = Modifier.clickable { onSelect(value) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 刷新间隔选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshIntervalDialog(
    currentInterval: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        SettingsPreferences.REFRESH_10_MINUTES to "10 分钟",
        SettingsPreferences.REFRESH_30_MINUTES to "30 分钟",
        SettingsPreferences.REFRESH_1_HOUR to "1 小时",
        SettingsPreferences.REFRESH_MANUAL to "手动刷新"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自动刷新间隔") },
        text = {
            Column {
                options.forEach { (value, label) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            RadioButton(
                                selected = currentInterval == value,
                                onClick = { onSelect(value) }
                            )
                        },
                        modifier = Modifier.clickable { onSelect(value) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 温度单位选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemperatureUnitDialog(
    currentUnit: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        SettingsPreferences.UNIT_CELSIUS to "摄氏度 (°C)",
        SettingsPreferences.UNIT_FAHRENHEIT to "华氏度 (°F)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("温度单位") },
        text = {
            Column {
                options.forEach { (value, label) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            RadioButton(
                                selected = currentUnit == value,
                                onClick = { onSelect(value) }
                            )
                        },
                        modifier = Modifier.clickable { onSelect(value) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 风速单位选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WindUnitDialog(
    currentUnit: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        SettingsPreferences.UNIT_KMH to "公里/小时 (km/h)",
        SettingsPreferences.UNIT_MS to "米/秒 (m/s)",
        SettingsPreferences.UNIT_MPH to "英里/小时 (mph)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("风速单位") },
        text = {
            Column {
                options.forEach { (value, label) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            RadioButton(
                                selected = currentUnit == value,
                                onClick = { onSelect(value) }
                            )
                        },
                        modifier = Modifier.clickable { onSelect(value) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
