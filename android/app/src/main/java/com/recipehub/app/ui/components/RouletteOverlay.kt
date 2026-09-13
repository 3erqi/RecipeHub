package com.recipehub.app.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.recipehub.app.data.model.Recipe
import kotlinx.coroutines.delay
import java.io.File

// A tall, single-slot window: the next card slides fully into view as the previous one slides
// out, same visual language as the grid's RecipeCard (thumbnail on top, title below).
private val ITEM_WIDTH = 240.dp
private val ITEM_HEIGHT = 300.dp
private const val SPIN_CYCLES = 4
private const val SPIN_DURATION_MS = 5000
private val SPIN_EASING = CubicBezierEasing(0.12f, 0.7f, 0.15f, 1f)

@Composable
fun RouletteOverlay(
    candidates: List<Recipe>,
    targetIndex: Int,
    onFinished: (Recipe) -> Unit,
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { ITEM_HEIGHT.toPx() }
    val itemCount = candidates.size

    val virtualList = remember(candidates) {
        if (candidates.isEmpty()) emptyList() else List((SPIN_CYCLES + 1) * itemCount) { candidates[it % itemCount] }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(candidates, targetIndex) {
        if (candidates.isEmpty()) return@LaunchedEffect
        val targetItemIndex = SPIN_CYCLES * itemCount + targetIndex
        val distancePx = targetItemIndex * itemHeightPx
        listState.animateScrollBy(
            value = distancePx,
            animationSpec = tween(durationMillis = SPIN_DURATION_MS, easing = SPIN_EASING),
        )
        delay(400)
        onFinished(candidates[targetIndex])
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 6.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    "Picking a recipe…",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Box(
                    modifier = Modifier
                        .width(ITEM_WIDTH)
                        .height(ITEM_HEIGHT)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                ) {
                    LazyColumn(state = listState, userScrollEnabled = false) {
                        items(virtualList) { recipe ->
                            ReelCard(recipe, modifier = Modifier.height(ITEM_HEIGHT))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReelCard(recipe: Recipe, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(0.dp), modifier = modifier.fillMaxWidth()) {
        Column {
            AsyncImage(
                model = recipe.thumbnailPath?.let { File(it) },
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
            Text(
                text = recipe.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}
