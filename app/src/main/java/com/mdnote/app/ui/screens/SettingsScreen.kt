package com.mdnote.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mdnote.app.data.repository.SortOrder
import com.mdnote.app.ui.theme.ThemeMode
import com.mdnote.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val sortOrder by viewModel.sortOrder.collectAsState(initial = SortOrder.UPDATED_DESC)
    val dynamicColor by viewModel.dynamicColor.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Theme section
            Text(
                text = "外观",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            ListItem(
                headlineContent = { Text("主题") },
                supportingContent = {
                    Text(
                        when (themeMode) {
                            ThemeMode.LIGHT -> "浅色模式"
                            ThemeMode.DARK -> "深色模式"
                            ThemeMode.SYSTEM -> "跟随系统"
                        }
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Palette, contentDescription = null)
                },
                modifier = Modifier.clickable { showThemeDialog = true }
            )

            ListItem(
                headlineContent = { Text("动态取色") },
                supportingContent = { Text("使用壁纸颜色生成主题配色") },
                leadingContent = {
                    Icon(Icons.Default.ColorLens, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = { enabled ->
                            scope.launch { viewModel.setDynamicColor(enabled) }
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Sort section
            Text(
                text = "显示",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            ListItem(
                headlineContent = { Text("默认排序") },
                supportingContent = {
                    Text(
                        when (sortOrder) {
                            SortOrder.UPDATED_DESC -> "最近修改"
                            SortOrder.CREATED_DESC -> "最近创建"
                            SortOrder.TITLE_ASC -> "按标题"
                        }
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Sort, contentDescription = null)
                },
                modifier = Modifier.clickable { showSortDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Data section
            Text(
                text = "数据",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            ListItem(
                headlineContent = { Text("导出诊断日志") },
                supportingContent = { Text("导出应用日志方便排查问题") },
                leadingContent = {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                },
                modifier = Modifier.clickable {
                    val uri = viewModel.exportLogs()
                    if (uri != null) {
                        viewModel.shareLogs(uri)
                    } else {
                        Toast.makeText(context, "日志导出失败，请稍后重试", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // About section
            Text(
                text = "关于",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            ListItem(
                headlineContent = { Text("关于 MdNote") },
                supportingContent = { Text("版本 1.0.0") },
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
                modifier = Modifier.clickable { onNavigateToAbout() }
            )

            ListItem(
                headlineContent = { Text("AI 驱动开发") },
                supportingContent = { Text("本项目 100% 由 AI 驱动开发完成") },
                leadingContent = {
                    Icon(Icons.Default.SmartToy, contentDescription = null)
                }
            )
        }
    }

    // Theme dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("选择主题") },
            text = {
                Column {
                    ThemeOption(
                        label = "浅色模式",
                        icon = Icons.Default.LightMode,
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = {
                            scope.launch { viewModel.setThemeMode(ThemeMode.LIGHT) }
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        label = "深色模式",
                        icon = Icons.Default.DarkMode,
                        selected = themeMode == ThemeMode.DARK,
                        onClick = {
                            scope.launch { viewModel.setThemeMode(ThemeMode.DARK) }
                            showThemeDialog = false
                        }
                    )
                    ThemeOption(
                        label = "跟随系统",
                        icon = Icons.Default.SettingsBrightness,
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = {
                            scope.launch { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Sort dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("默认排序方式") },
            text = {
                Column {
                    SortOption(
                        label = "最近修改",
                        selected = sortOrder == SortOrder.UPDATED_DESC,
                        onClick = {
                            scope.launch { viewModel.setSortOrder(SortOrder.UPDATED_DESC) }
                            showSortDialog = false
                        }
                    )
                    SortOption(
                        label = "最近创建",
                        selected = sortOrder == SortOrder.CREATED_DESC,
                        onClick = {
                            scope.launch { viewModel.setSortOrder(SortOrder.CREATED_DESC) }
                            showSortDialog = false
                        }
                    )
                    SortOption(
                        label = "按标题",
                        selected = sortOrder == SortOrder.TITLE_ASC,
                        onClick = {
                            scope.launch { viewModel.setSortOrder(SortOrder.TITLE_ASC) }
                            showSortDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SortOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}