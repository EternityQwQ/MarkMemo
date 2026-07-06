package com.mdnote.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mdnote.app.data.model.NoteCategory
import com.mdnote.app.viewmodel.NoteEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    viewModel: NoteEditViewModel,
    noteId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val category by viewModel.category.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            viewModel.loadNote(noteId)
        } else {
            viewModel.createNewNote()
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (noteId != null && noteId > 0) "编辑笔记" else "新建笔记",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // Category selector
                    IconButton(onClick = { showCategoryMenu = true }) {
                        Icon(Icons.Default.Folder, contentDescription = "分类")
                    }
                    // Preview
                    if (content.isNotEmpty()) {
                        IconButton(onClick = onNavigateToPreview) {
                            Icon(Icons.Default.Visibility, contentDescription = "预览")
                        }
                    }
                    // Save
                    IconButton(onClick = { viewModel.saveNote() }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
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
            // Category chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                val currentCategory = try {
                    NoteCategory.valueOf(category)
                } catch (_: Exception) {
                    NoteCategory.OTHER
                }
                InputChip(
                    selected = true,
                    onClick = { showCategoryMenu = true },
                    label = { Text(currentCategory.displayName) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.setTitle(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("标题") },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // Content
            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.setContent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("开始书写 Markdown…") },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // Bottom bar with word count
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${content.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size} 字 | ${content.length} 字符",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Category selector
    DropdownMenu(
        expanded = showCategoryMenu,
        onDismissRequest = { showCategoryMenu = false }
    ) {
        NoteCategory.entries.forEach { cat ->
            DropdownMenuItem(
                text = { Text(cat.displayName) },
                onClick = {
                    viewModel.setCategory(cat.name)
                    showCategoryMenu = false
                },
                leadingIcon = {
                    if (category == cat.name) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
        }
    }
}