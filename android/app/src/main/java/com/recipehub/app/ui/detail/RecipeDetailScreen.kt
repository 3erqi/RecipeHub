package com.recipehub.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.MaterialTheme
import coil.compose.AsyncImage
import com.recipehub.app.data.model.Recipe
import com.recipehub.app.ui.components.CollectionPickerContent
import android.content.Intent
import android.net.Uri
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onEdit: () -> Unit,
    onBack: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val allCollections by viewModel.allCollections.collectAsStateWithLifecycle()
    val recipeCollectionIds by viewModel.recipeCollectionIds.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showListDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                },
            )
        },
    ) { padding ->
        recipe?.let { current ->
            RecipeDetailContent(
                recipe = current,
                listNames = allCollections.filter { it.id in recipeCollectionIds }.map { it.name },
                onEditLists = { showListDialog = true },
                onViewOriginal = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(current.sourceUrl)))
                },
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showListDialog) {
        AlertDialog(
            onDismissRequest = { showListDialog = false },
            title = { Text("Lists") },
            text = {
                CollectionPickerContent(
                    collections = allCollections,
                    selectedIds = recipeCollectionIds,
                    onToggle = viewModel::toggleCollection,
                    onCreateNew = viewModel::createCollection,
                )
            },
            confirmButton = {
                TextButton(onClick = { showListDialog = false }) { Text("Done") }
            },
        )
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    listNames: List<String>,
    onEditLists: () -> Unit,
    onViewOriginal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            AsyncImage(
                model = recipe.thumbnailPath?.let { File(it) },
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxWidth().height(240.dp),
            )
        }
        item {
            Text(
                recipe.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )
        }
        item {
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    if (listNames.isEmpty()) "No lists" else listNames.joinToString(", "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onEditLists) { Text("Edit lists") }
            }
        }
        if (recipe.ingredients.isEmpty() && recipe.steps.isEmpty()) {
            item {
                Text(
                    "No ingredients yet — tap Edit to add them",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        if (recipe.ingredients.isNotEmpty()) {
            item { Text("Ingredients", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)) }
            items(recipe.ingredients) { ingredient ->
                Text("• $ingredient", modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            }
        }
        if (recipe.steps.isNotEmpty()) {
            item { Text("Steps", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            itemsIndexed(recipe.steps) { index, step ->
                Text("${index + 1}. $step", modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            }
        }
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(onClick = onViewOriginal) {
                    Text("View original post")
                }
            }
        }
    }
}
