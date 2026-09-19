package com.recipehub.app.ui.saving

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.recipehub.app.data.model.Collection
import java.io.File

private val Forest = Color(0xFF0A2318)
private val Cream = Color(0xFFF6F2E6)
private val Grass = Color(0xFF5CB944)
private val Danger = Color(0xFFB5471B)

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

@Composable
fun SavingScreen(
    url: String,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: SavingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedCollectionIds.collectAsStateWithLifecycle()

    LaunchedEffect(url) { viewModel.submit(url) }

    Box(modifier = Modifier.fillMaxSize().background(Forest.copy(alpha = 0.5f))) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 44.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)),
                )
                Spacer(modifier = Modifier.height(18.dp))

                when (val current = state) {
                    is SavingState.Loading -> LoadingContent()
                    is SavingState.Review -> ReviewContent(
                        review = current,
                        collections = collections,
                        selectedIds = selectedIds,
                        onTitleChange = viewModel::onTitleChange,
                        onToggleCollection = viewModel::toggleCollection,
                        onCreateCollection = viewModel::createCollection,
                        onDiscard = { viewModel.discard(onCancel) },
                        onSave = viewModel::saveRecipe,
                    )
                    is SavingState.Error -> ErrorContent(
                        onRetry = viewModel::retry,
                        onSaveAnyway = viewModel::saveAnyway,
                        onDiscard = { viewModel.discard(onCancel) },
                    )
                    is SavingState.Saved -> SavedContent(saved = current, onDone = onSaved)
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        val transition = rememberInfiniteTransition(label = "spinner")
        val angle by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
            label = "angle",
        )
        val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        Canvas(modifier = Modifier.size(42.dp).rotate(angle)) {
            drawCircle(color = trackColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
            drawArc(
                color = Grass,
                startAngle = -90f,
                sweepAngle = 100f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text(
            "Reading the post",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            modifier = Modifier.padding(top = 18.dp),
        )
        Text(
            "Grabbing the thumbnail and caption",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ReviewContent(
    review: SavingState.Review,
    collections: List<Collection>,
    selectedIds: Set<Long>,
    onTitleChange: (String) -> Unit,
    onToggleCollection: (Long) -> Unit,
    onCreateCollection: (String) -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Text("Save this one?", style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 24.sp))
    Text(
        "We named it from the caption — change anything you like.",
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
    )

    Row(verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = review.thumbnailPath?.let { File(it) },
            contentDescription = review.title,
            modifier = Modifier.size(78.dp).clip(RoundedCornerShape(13.dp)).aspectRatio(1f),
        )
        Column(modifier = Modifier.padding(start = 13.dp).weight(1f)) {
            val author = review.authorUsername?.takeIf { it.isNotBlank() }
            Text(
                (platformLabel(review.sourcePlatform) + (author?.let { " · @$it" } ?: "")).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            )
            OutlinedTextField(
                value = review.title,
                onValueChange = onTitleChange,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }
    }

    Text(
        "ADD TO LISTS",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        collections.forEach { collection ->
            val selected = collection.id in selectedIds
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .let {
                        if (selected) it.background(Grass) else it.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                    }
                    .clickableNoRipple { onToggleCollection(collection.id) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text(
                    collection.name,
                    color = if (selected) Forest else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                .clickableNoRipple { showCreateDialog = true }
                .padding(horizontal = 14.dp, vertical = 9.dp),
        ) {
            Text(
                "+ New list",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
            )
        }
    }

    Row(modifier = Modifier.padding(top = 22.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onDiscard) { Text("Discard", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
        Spacer(modifier = Modifier.weight(1f))
    }
    Button(
        onClick = onSave,
        enabled = review.title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = Grass, contentColor = Forest),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Save recipe")
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New list") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true) },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        onCreateCollection(name.trim())
                        showCreateDialog = false
                    },
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit, onSaveAnyway: () -> Unit, onDiscard: () -> Unit) {
    Text("Couldn't read this post", style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp))
    Text(
        "Check your connection and try again.",
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
    )
    Button(
        onClick = onRetry,
        colors = ButtonDefaults.buttonColors(containerColor = Grass, contentColor = Forest),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Retry") }
    OutlinedButton(onClick = onSaveAnyway, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Text("Save anyway")
    }
    TextButton(onClick = onDiscard, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text("Discard", color = Danger)
    }
}

@Composable
private fun SavedContent(saved: SavingState.Saved, onDone: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(Grass),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(22.dp)) {
                val strokeWidth = 2.6.dp.toPx()
                drawLine(Forest, Offset(size.width * 0.15f, size.height * 0.55f), Offset(size.width * 0.4f, size.height * 0.8f), strokeWidth, StrokeCap.Round)
                drawLine(Forest, Offset(size.width * 0.4f, size.height * 0.8f), Offset(size.width * 0.9f, size.height * 0.2f), strokeWidth, StrokeCap.Round)
            }
        }
        Text(
            "Saved",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 19.sp),
            modifier = Modifier.padding(top = 14.dp),
        )
        val body = if (saved.listNames.isEmpty()) {
            "${saved.title} is in your library, not filed in a list yet."
        } else {
            "${saved.title} is in ${saved.listNames.joinToString(" and ")}."
        }
        Text(
            body,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = Forest, contentColor = Cream),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Done") }
    }
}
