package com.recipehub.app.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.recipehub.app.data.model.Recipe
import com.recipehub.app.ui.components.CollectionPickerContent
import java.io.File

private val Forest = Color(0xFF0A2318)
private val Cream = Color(0xFFF6F2E6)
private val Grass = Color(0xFF5CB944)
private val GrassDeep = Color(0xFF3F6B29)

private fun platformLabel(sourcePlatform: String): String = when (sourcePlatform) {
    "tiktok" -> "TikTok"
    "instagram" -> "Instagram"
    else -> sourcePlatform.replaceFirstChar { it.uppercase() }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )
}

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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        recipe?.let { current ->
            RecipeDetailContent(
                recipe = current,
                listNames = allCollections.filter { it.id in recipeCollectionIds }.map { it.name },
                onBack = onBack,
                onEdit = onEdit,
                onEditLists = { showListDialog = true },
                onViewOriginal = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(current.sourceUrl)))
                },
            )
        }
    }

    if (showListDialog) {
        AlertDialog(
            onDismissRequest = { showListDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Lists", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                CollectionPickerContent(
                    collections = allCollections,
                    selectedIds = recipeCollectionIds,
                    onToggle = viewModel::toggleCollection,
                    onCreateNew = viewModel::createCollection,
                )
            },
            confirmButton = {
                TextButton(onClick = { showListDialog = false }) { Text("Done", color = GrassDeep) }
            },
        )
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    listNames: List<String>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onEditLists: () -> Unit,
    onViewOriginal: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                AsyncImage(
                    model = recipe.thumbnailPath?.let { File(it) },
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(14.dp),
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Forest.copy(alpha = 0.75f)),
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Cream)
                    }
                    Box(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Forest.copy(alpha = 0.75f))
                            .clickableNoRipple(onEdit)
                            .padding(horizontal = 15.dp, vertical = 9.dp),
                    ) {
                        Text("Edit", color = Cream, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-22).dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .padding(top = 22.dp, start = 22.dp, end = 20.dp, bottom = 34.dp),
            ) {
                val author = recipe.authorUsername?.takeIf { it.isNotBlank() }
                Text(
                    (platformLabel(recipe.sourcePlatform) + (author?.let { " · $it" } ?: "")).uppercase(),
                    color = GrassDeep,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Row(modifier = Modifier.padding(top = 15.dp, bottom = 4.dp)) {
                    listNames.forEach { name ->
                        Box(
                            modifier = Modifier
                                .padding(end = 7.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                name,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                            .clickableNoRipple(onEditLists)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text("Edit lists", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f), style = MaterialTheme.typography.labelLarge)
                    }
                }

                if (recipe.ingredients.isEmpty() && recipe.steps.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 11.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.045f))
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                    ) {
                        Text(
                            "No ingredients yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            "Watch the clip and jot them down — they stay with the recipe.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        )
                        Button(
                            onClick = onEdit,
                            colors = ButtonDefaults.buttonColors(containerColor = Grass, contentColor = Forest),
                            shape = RoundedCornerShape(12.dp),
                        ) { Text("Add notes") }
                    }
                }
            }
        }
        if (recipe.ingredients.isNotEmpty()) {
            item {
                Text(
                    "INGREDIENTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 22.dp, top = 11.dp),
                )
            }
            items(recipe.ingredients) { ingredient ->
                Row(modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
                    Box(modifier = Modifier.padding(top = 7.dp, end = 10.dp).size(7.dp).clip(CircleShape).background(Grass))
                    Text(ingredient, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        if (recipe.steps.isNotEmpty()) {
            item {
                Text(
                    "STEPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 22.dp, top = 14.dp, bottom = 4.dp),
                )
            }
            itemsIndexed(recipe.steps) { index, step ->
                Row(modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)) {
                    Box(
                        modifier = Modifier.size(26.dp).clip(RoundedCornerShape(9.dp)).background(Forest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", color = Cream, style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp))
                    }
                    Text(
                        step,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(22.dp)) {
                Button(
                    onClick = onViewOriginal,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest, contentColor = Cream),
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("View original post")
                    Text(" ↗", color = Grass)
                }
            }
        }
    }
}
