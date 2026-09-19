package com.recipehub.app.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val Forest = Color(0xFF0A2318)
private val Cream = Color(0xFFF6F2E6)
private val Grass = Color(0xFF5CB944)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onBack: () -> Unit = {},
    onSaved: () -> Unit,
    viewModel: RecipeEditViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()

    LaunchedEffect(form.saved) {
        if (form.saved) onSaved()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Edit recipe", color = Cream, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Cream)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Grass, RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = !form.isSaving,
                                onClick = { viewModel.save() },
                            )
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                    ) {
                        Text(if (form.isSaving) "Saving…" else "Save", color = Forest)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Forest),
            )
        },
    ) { padding ->
        if (form.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(padding).padding(32.dp), color = Grass)
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 18.dp, top = 20.dp, bottom = 34.dp),
        ) {
            EditField(label = "TITLE", value = form.title, onValueChange = viewModel::onTitleChange, singleLine = true)
            EditField(
                label = "INGREDIENTS",
                value = form.ingredientsText,
                onValueChange = viewModel::onIngredientsChange,
                hint = "one per line",
                minLines = 6,
                modifier = Modifier.padding(top = 18.dp),
            )
            EditField(
                label = "STEPS",
                value = form.stepsText,
                onValueChange = viewModel::onStepsChange,
                hint = "one per line",
                minLines = 6,
                modifier = Modifier.padding(top = 18.dp),
            )
            Button(
                onClick = { viewModel.save() },
                enabled = !form.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Grass, contentColor = Forest),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Text(if (form.isSaving) "Saving…" else "Save")
            }
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
    minLines: Int = 1,
    hint: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            if (hint != null) {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(13.dp),
            textStyle = if (singleLine) MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp) else MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
    }
}
