package com.recipehub.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.recipehub.app.data.model.Collection

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionPickerContent(
    collections: List<Collection>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onCreateNew: (String) -> Unit,
    manageable: Boolean = false,
    onRename: (Long, String) -> Unit = { _, _ -> },
    onDelete: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var manageTargetId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = modifier) {
        collections.forEach { collection ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onToggle(collection.id) },
                        onLongClick = if (manageable) {
                            { manageTargetId = collection.id }
                        } else null,
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Checkbox(checked = collection.id in selectedIds, onCheckedChange = { onToggle(collection.id) })
                Text(collection.name, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { showCreateDialog = true })
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.width(24.dp))
            Text("Create new collection", modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (showCreateDialog) {
        NamePromptDialog(
            title = "New collection",
            initialValue = "",
            confirmLabel = "Create",
            onConfirm = { name ->
                onCreateNew(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }

    val targetId = manageTargetId
    if (targetId != null) {
        val current = collections.firstOrNull { it.id == targetId }
        if (current != null) {
            NamePromptDialog(
                title = "Edit collection",
                initialValue = current.name,
                confirmLabel = "Rename",
                onConfirm = { name ->
                    onRename(targetId, name)
                    manageTargetId = null
                },
                onDismiss = { manageTargetId = null },
                extraAction = {
                    TextButton(onClick = {
                        onDelete(targetId)
                        manageTargetId = null
                    }) { Text("Delete") }
                },
            )
        } else {
            manageTargetId = null
        }
    }
}

@Composable
private fun NamePromptDialog(
    title: String,
    initialValue: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    extraAction: (@Composable () -> Unit)? = null,
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true)
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }, enabled = text.isNotBlank()) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            Row {
                extraAction?.invoke()
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
