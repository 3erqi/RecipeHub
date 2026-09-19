package com.recipehub.app.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipehub.app.R
import com.recipehub.app.data.model.Recipe
import com.recipehub.app.ui.components.CollectionPickerContent
import com.recipehub.app.ui.components.RecipeCard
import com.recipehub.app.ui.components.RouletteOverlay

private val Forest = Color(0xFF0A2318)
private val Cream = Color(0xFFF6F2E6)
private val Grass = Color(0xFF5CB944)
private val GrassDeep = Color(0xFF3F6B29)

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Forest)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 16.dp, bottom = 18.dp, start = 18.dp, end = 18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(width = 10.dp, height = 3.5.dp).rotate(-18f).background(Grass))
                    Box(modifier = Modifier.padding(start = 3.dp).size(width = 10.dp, height = 3.5.dp).rotate(-18f).background(Grass))
                    Text(
                        "RecipeHub",
                        color = Cream,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Cream.copy(alpha = 0.08f))
                            .border(1.dp, Cream.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                            .clickable { menuExpanded = true }
                            .padding(vertical = 11.dp, horizontal = 14.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                label,
                                color = Cream,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                            )
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Filter by list", tint = Grass)
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
                    IconButton(
                        onClick = { roulette = recipes to recipes.indices.random() },
                        enabled = recipes.isNotEmpty(),
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Grass),
                    ) {
                        Icon(painterResource(R.drawable.ic_shuffle), contentDescription = "Random recipe", tint = Forest)
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search recipes") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        TextButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Text("Clear", color = GrassDeep)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 14.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${recipes.size} recipe${if (recipes.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                )
                if (selectedIds.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearFilter() }) {
                        Text("Show all", color = GrassDeep, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                if (recipes.isEmpty()) {
                    val isSearching = searchQuery.isNotEmpty()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    ) {
                        Text(
                            if (isSearching) "Nothing matches that" else "No recipes here yet",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            if (isSearching) {
                                "Try a shorter word — search only looks at titles."
                            } else {
                                "Share a Reel or TikTok to RecipeHub and it lands here."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                        items(recipes, key = { it.localId }) { recipe ->
                            Box {
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe.localId) },
                                    onLongClick = { contextMenuId = recipe.localId },
                                    onOverflowClick = { contextMenuId = recipe.localId },
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
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.secondary) },
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
        val targetTitle = recipes.firstOrNull { it.localId == targetId }?.title.orEmpty()
        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            title = { Text("Delete this recipe?") },
            text = { Text("$targetTitle will be removed from every list. This can't be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecipe(targetId)
                        deleteTargetId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetId = null }) { Text("Keep it") }
            },
        )
    }

    roulette?.let { (candidates, targetIndex) ->
        RouletteOverlay(
            candidates = candidates,
            targetIndex = targetIndex,
            onCancel = { roulette = null },
            onFinished = { chosen ->
                roulette = null
                onRecipeClick(chosen.localId)
            },
        )
    }
}
