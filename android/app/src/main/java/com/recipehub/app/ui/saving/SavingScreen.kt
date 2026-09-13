package com.recipehub.app.ui.saving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipehub.app.ui.components.CollectionPickerContent

@Composable
fun SavingScreen(
    url: String,
    onSaved: (localId: Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: SavingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(url) { viewModel.submit(url) }

    LaunchedEffect(state) {
        (state as? SavingState.Done)?.let { onSaved(it.localId) }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (state) {
            is SavingState.Loading -> {
                CircularProgressIndicator()
                Text("Saving your recipe…", modifier = Modifier.padding(top = 16.dp))
            }
            is SavingState.Error -> {
                Text("Couldn't fetch this post. Check your connection and try again.")
                Button(onClick = { viewModel.retry() }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retry")
                }
                OutlinedButton(onClick = { viewModel.saveAnyway() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Save anyway")
                }
                OutlinedButton(
                    onClick = { viewModel.discard(onDiscarded = onCancel) },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text("Cancel")
                }
            }
            is SavingState.NeedsName -> {
                var name by remember { mutableStateOf("") }
                Text("We couldn't read a name from this post — what should we call it?")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Recipe name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
                Button(
                    onClick = { viewModel.confirmName(name) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = { viewModel.discard(onDiscarded = onCancel) },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text("Cancel")
                }
            }
            is SavingState.PickingLists -> {
                val collections by viewModel.collections.collectAsStateWithLifecycle()
                val selectedIds by viewModel.selectedCollectionIds.collectAsStateWithLifecycle()
                Text("Add this recipe to a list?")
                CollectionPickerContent(
                    collections = collections,
                    selectedIds = selectedIds,
                    onToggle = viewModel::toggleCollection,
                    onCreateNew = viewModel::createCollection,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Button(onClick = { viewModel.finalizeLists() }, modifier = Modifier.padding(top = 16.dp)) {
                    Text(if (selectedIds.isEmpty()) "Skip" else "Save")
                }
            }
            is SavingState.Done -> {
                // Navigation is handled by the LaunchedEffect above; nothing to render.
            }
        }
    }
}
