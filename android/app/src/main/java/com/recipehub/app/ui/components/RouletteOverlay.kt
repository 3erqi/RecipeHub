package com.recipehub.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.recipehub.app.data.model.Recipe
import java.io.File

// A tall, single-slot window: the next card slides fully into view as the previous one slides
// out, same visual language as the grid's RecipeCard (thumbnail on top, title below).
private val ITEM_WIDTH = 240.dp
private val ITEM_HEIGHT = 300.dp
private const val SPIN_CYCLES = 4
private const val SPIN_DURATION_MS = 5000
private const val TOTAL_VIRTUAL_ITEMS = 2000
private val SPIN_EASING = CubicBezierEasing(0.12f, 0.7f, 0.15f, 1f)

private val Forest = Color(0xFF0A2318)
private val ForestDeep = Color(0xFF052116)
private val Cream = Color(0xFFF6F2E6)
private val Grass = Color(0xFF5CB944)

private fun platformLabel(sourcePlatform: String): String = when (sourcePlatform) {
    "tiktok" -> "TikTok"
    "instagram" -> "Instagram"
    else -> sourcePlatform.replaceFirstChar { it.uppercase() }
}

@Composable
fun RouletteOverlay(
    candidates: List<Recipe>,
    targetIndex: Int,
    onCancel: () -> Unit,
    onFinished: (Recipe) -> Unit,
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { ITEM_HEIGHT.toPx() }
    val itemCount = candidates.size

    val listState = rememberLazyListState()
    var settled by remember { mutableStateOf(false) }
    var currentTargetIndex by remember(candidates) { mutableIntStateOf(targetIndex) }
    var absoluteIndex by remember(candidates) { mutableLongStateOf(0L) }
    var spinGeneration by remember { mutableIntStateOf(0) }

    LaunchedEffect(candidates, spinGeneration) {
        if (candidates.isEmpty()) return@LaunchedEffect
        settled = false
        val base = absoluteIndex + SPIN_CYCLES * itemCount
        val remainder = (base % itemCount).toInt()
        val adjustment = (currentTargetIndex - remainder + itemCount) % itemCount
        val nextAbsolute = base + adjustment
        val deltaPx = (nextAbsolute - absoluteIndex) * itemHeightPx
        listState.animateScrollBy(
            value = deltaPx,
            animationSpec = tween(durationMillis = SPIN_DURATION_MS, easing = SPIN_EASING),
        )
        absoluteIndex = nextAbsolute
        settled = true
    }

    val settledRecipe = candidates.getOrNull(currentTargetIndex)

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(ForestDeep.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (settled) "Tonight, then" else "Picking a recipe",
                    color = Cream,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 24.sp),
                )
                Text(
                    if (settled && settledRecipe != null) {
                        val author = settledRecipe.authorUsername?.takeIf { it.isNotBlank() }
                        if (author != null) "${platformLabel(settledRecipe.sourcePlatform)} · $author" else platformLabel(settledRecipe.sourcePlatform)
                    } else {
                        "From the ${candidates.size} on screen"
                    },
                    color = Cream.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp, lineHeight = 17.sp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                )
                Box(
                    modifier = Modifier
                        .width(ITEM_WIDTH)
                        .height(ITEM_HEIGHT)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Cream)
                        .border(2.dp, Grass, RoundedCornerShape(18.dp)),
                ) {
                    LazyColumn(state = listState, userScrollEnabled = false) {
                        items(TOTAL_VIRTUAL_ITEMS) { index ->
                            ReelCard(candidates[index % itemCount], modifier = Modifier.height(ITEM_HEIGHT))
                        }
                    }
                }
                if (!settled) {
                    TextButton(onClick = onCancel, modifier = Modifier.padding(top = 22.dp)) {
                        Text("Cancel", color = Cream.copy(alpha = 0.5f))
                    }
                }
                AnimatedVisibility(visible = settled, enter = fadeIn(tween(250))) {
                    Row(modifier = Modifier.padding(top = 22.dp)) {
                        OutlinedButton(
                            onClick = {
                                currentTargetIndex = candidates.indices.random()
                                spinGeneration++
                            },
                            border = BorderStroke(1.5.dp, Cream.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Spin again", color = Cream)
                        }
                        Button(
                            onClick = { settledRecipe?.let(onFinished) },
                            colors = ButtonDefaults.buttonColors(containerColor = Grass, contentColor = Forest),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(start = 12.dp),
                        ) {
                            Text("Cook this")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReelCard(recipe: Recipe, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            model = recipe.thumbnailPath?.let { File(it) },
            contentDescription = recipe.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        )
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.titleSmall,
            color = Forest,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(12.dp),
        )
    }
}
