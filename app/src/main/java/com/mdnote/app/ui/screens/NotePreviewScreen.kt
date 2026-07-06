package com.mdnote.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mdnote.app.data.export.ExportHelper
import com.mdnote.app.data.model.NoteCategory
import com.mdnote.app.ui.components.MarkdownRenderer
import com.mdnote.app.viewmodel.NoteEditViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePreviewScreen(
    viewModel: NoteEditViewModel,
    noteId: Long,
    onNavigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val category by viewModel.category.collectAsState()
    val exportedUri by viewModel.exportedUri.collectAsState()
    val exportType by viewModel.exportType.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    // Handle export result
    LaunchedEffect(exportedUri, exportType) {
        if (exportedUri != null && exportType != null) {
            val helper = ExportHelper(context)
            val formatName = if (exportType == "markdown") "Markdown" else "PDF"
            val result = snackbarHostState.showSnackbar(
                message = "$formatName 导出成功",
                actionLabel = "分享",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                helper.shareFile(
                    exportedUri!!,
                    if (exportType == "markdown") "text/markdown" else "application/pdf"
                )
            }
            viewModel.clearExportState()
        }
    }

    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    val currentCategory = try {
        NoteCategory.valueOf(category)
    } catch (_: Exception) {
        NoteCategory.OTHER
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title.ifEmpty { "无标题" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Default.Share, contentDescription = "导出")
                    }
                    IconButton(onClick = { onEdit(noteId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
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
            // Metadata bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InputChip(
                        selected = true,
                        onClick = {},
                        label = { Text(currentCategory.displayName) }
                    )
                    Text(
                        text = "${content.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size} 字",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Markdown content
            MarkdownRenderer(
                markdown = content,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            )
        }
    }

    // Export menu
    DropdownMenu(
        expanded = showExportMenu,
        onDismissRequest = { showExportMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("导出 Markdown") },
            onClick = {
                showExportMenu = false
                scope.launch {
                    val note = viewModel.getCurrentNote()
                    if (note != null) {
                        val helper = ExportHelper(context)
                        val uri = helper.exportMarkdown(note)
                        viewModel.setExportedUri(uri, "markdown")
                    } else {
                        Toast.makeText(context, "导出失败：无法获取笔记数据", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            leadingIcon = {
                Icon(Icons.Default.Code, contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("导出 PDF") },
            onClick = {
                showExportMenu = false
                scope.launch {
                    val note = viewModel.getCurrentNote()
                    if (note != null) {
                        val helper = ExportHelper(context)
                        val uri = helper.exportPdf(note)
                        viewModel.setExportedUri(uri, "pdf")
                    } else {
                        Toast.makeText(context, "导出失败：无法获取笔记数据", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            leadingIcon = {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            }
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这篇笔记吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(noteId)
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}