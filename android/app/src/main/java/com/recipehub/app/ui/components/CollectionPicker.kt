package com.recipehub.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.recipehub.app.data.model.Collection

private val Forest = Color(0xFF0A2318)
private val Grass = Color(0xFF5CB944)
private val GrassDeep = Color(0xFF3F6B29)
private val Danger = Color(0xFFB5471B)

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
        Text(
            "SHOW LISTS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 6.dp),
        )
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
                    .padding(vertical = 11.dp, horizontal = 18.dp),
            ) {
                Checkbox(
                    checked = collection.id in selectedIds,
                    onCheckedChange = { onToggle(collection.id) },
                    colors = CheckboxDefaults.colors(checkedColor = Grass, checkmarkColor = Forest),
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    collection.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 10.dp).weight(1f),
                )
                if (manageable) {
                    TextButton(onClick = { manageTargetId = collection.id }) {
                        Text("Edit", color = GrassDeep, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { showCreateDialog = true })
                .padding(vertical = 14.dp, horizontal = 18.dp),
        ) {
            Box(
                modifier = Modifier.size(22.dp).clip(RoundedCornerShape(7.dp)).background(Grass),
                contentAlignment = Alignment.Center,
            ) {
                Text("+", color = Forest, style = MaterialTheme.typography.labelLarge)
            }
            Text(
                "New list",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }

    if (showCreateDialog) {
        NamePromptDialog(
            title = "New list",
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
                title = "Rename list",
                initialValue = current.name,
                confirmLabel = "Save",
                onConfirm = { name ->
                    onRename(targetId, name)
                    manageTargetId = null
                },
                onDismiss = { manageTargetId = null },
                onDelete = {
                    onDelete(targetId)
                    manageTargetId = null
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
    onDelete: (() -> Unit)? = null,
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Grass, contentColor = Forest),
                shape = RoundedCornerShape(13.dp),
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete list", color = Danger) }
                    Spacer(modifier = Modifier.weight(1f))
                }
                TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
            }
        },
    )
}
