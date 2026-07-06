package com.mdnote.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mdnote.app.data.model.Note
import com.mdnote.app.data.model.NoteCategory
import com.mdnote.app.data.repository.SortOrder
import com.mdnote.app.ui.components.NoteCard
import com.mdnote.app.ui.components.SearchBar
import com.mdnote.app.viewmodel.NoteListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel,
    onCreateNote: () -> Unit,
    onEditNote: (Long) -> Unit,
    onPreviewNote: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var deleteConfirmNote by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("笔记") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = if (selectedCategory != null) Icons.Default.FilterList else Icons.Default.FilterList,
                            contentDescription = "筛选",
                            tint = if (selectedCategory != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "排序")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建笔记")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onClear = { viewModel.clearSearch() }
            )

            // Category filter chips
            if (selectedCategory != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.setCategory(null) },
                        label = {
                            Text(
                                NoteCategory.valueOf(selectedCategory ?: "OTHER").displayName
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "清除筛选",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Notes list
            if (notes.isEmpty()) {
                EmptyNotesView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = {
                                if (note.content.isEmpty()) {
                                    onEditNote(note.id)
                                } else {
                                    onPreviewNote(note.id)
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    // Sort menu
    DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("最近修改") },
            onClick = {
                viewModel.setSortOrder(SortOrder.UPDATED_DESC)
                showSortMenu = false
            },
            leadingIcon = {
                if (sortOrder == SortOrder.UPDATED_DESC) {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }
        )
        DropdownMenuItem(
            text = { Text("最近创建") },
            onClick = {
                viewModel.setSortOrder(SortOrder.CREATED_DESC)
                showSortMenu = false
            },
            leadingIcon = {
                if (sortOrder == SortOrder.CREATED_DESC) {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }
        )
        DropdownMenuItem(
            text = { Text("按标题") },
            onClick = {
                viewModel.setSortOrder(SortOrder.TITLE_ASC)
                showSortMenu = false
            },
            leadingIcon = {
                if (sortOrder == SortOrder.TITLE_ASC) {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }
        )
    }

    // Filter menu
    DropdownMenu(
        expanded = showFilterMenu,
        onDismissRequest = { showFilterMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("全部") },
            onClick = {
                viewModel.setCategory(null)
                showFilterMenu = false
            },
            leadingIcon = {
                if (selectedCategory == null) {
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }
        )
        NoteCategory.entries.forEach { category ->
            DropdownMenuItem(
                text = { Text(category.displayName) },
                onClick = {
                    viewModel.setCategory(category.name)
                    showFilterMenu = false
                },
                leadingIcon = {
                    if (selectedCategory == category.name) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
        }
    }

    // Delete confirmation
    deleteConfirmNote?.let { note ->
        AlertDialog(
            onDismissRequest = { deleteConfirmNote = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${note.title.ifEmpty { "无标题" }}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(note)
                        deleteConfirmNote = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmNote = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EmptyNotesView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NoteAdd,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无笔记",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角 + 创建你的第一篇笔记",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}