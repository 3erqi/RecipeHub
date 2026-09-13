package com.recipehub.app.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipehub.app.R
import com.recipehub.app.data.model.Recipe
import com.recipehub.app.ui.components.CollectionPickerContent
import com.recipehub.app.ui.components.RecipeCard
import com.recipehub.app.ui.components.RouletteOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onRecipeClick: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedCollectionIds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
    var contextMenuId by remember { mutableStateOf<Long?>(null) }
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }
    var roulette by remember { mutableStateOf<Pair<List<Recipe>, Int>?>(null) }

    val label = when {
        selectedIds.isEmpty() -> "All Recipes"
        selectedIds.size == 1 -> collections.firstOrNull { it.id in selectedIds }?.name ?: "All Recipes"
        else -> "${selectedIds.size} lists selected"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { menuExpanded = true },
                        ) {
                            Text(label)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Filter by list")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            CollectionPickerContent(
                                collections = collections,
                                selectedIds = selectedIds,
                                onToggle = viewModel::toggleCollection,
                                onCreateNew = viewModel::createCollection,
                                manageable = true,
                                onRename = viewModel::renameCollection,
                                onDelete = viewModel::deleteCollection,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { roulette = recipes to recipes.indices.random() },
                        enabled = recipes.isNotEmpty(),
                    ) {
                        Icon(painterResource(R.drawable.ic_shuffle), contentDescription = "Random recipe")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search recipes") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                if (recipes.isEmpty()) {
                    Text(
                        if (searchQuery.isNotEmpty()) {
                            "No recipes match \"$searchQuery\"."
                        } else {
                            "No recipes yet. Share a Reel or TikTok to save one here."
                        },
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    )
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                        items(recipes, key = { it.localId }) { recipe ->
                            Box {
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe.localId) },
                                    onLongClick = { contextMenuId = recipe.localId },
                                    modifier = Modifier.padding(8.dp),
                                )
                                DropdownMenu(
                                    expanded = contextMenuId == recipe.localId,
                                    onDismissRequest = { contextMenuId = null },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            contextMenuId = null
                                            onEditRecipe(recipe.localId)
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            contextMenuId = null
                                            deleteTargetId = recipe.localId
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val targetId = deleteTargetId
    if (targetId != null) {
        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            title = { Text("Delete this recipe?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecipe(targetId)
                    deleteTargetId = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetId = null }) { Text("Cancel") }
            },
        )
    }

    roulette?.let { (candidates, targetIndex) ->
        RouletteOverlay(
            candidates = candidates,
            targetIndex = targetIndex,
            onFinished = { chosen ->
                roulette = null
                onRecipeClick(chosen.localId)
            },
        )
    }
}
