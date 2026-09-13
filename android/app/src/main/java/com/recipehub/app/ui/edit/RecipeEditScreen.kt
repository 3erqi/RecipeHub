package com.recipehub.app.ui.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onSaved: () -> Unit,
    viewModel: RecipeEditViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()

    LaunchedEffect(form.saved) {
        if (form.saved) onSaved()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Edit recipe") }) }) { padding ->
        if (form.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(32.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = form.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.ingredientsText,
                onValueChange = viewModel::onIngredientsChange,
                label = { Text("Ingredients (one per line)") },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                minLines = 4,
            )
            OutlinedTextField(
                value = form.stepsText,
                onValueChange = viewModel::onStepsChange,
                label = { Text("Steps (one per line)") },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                minLines = 4,
            )
            Button(
                onClick = { viewModel.save() },
                enabled = !form.isSaving,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text(if (form.isSaving) "Saving…" else "Save")
            }
        }
    }
}
