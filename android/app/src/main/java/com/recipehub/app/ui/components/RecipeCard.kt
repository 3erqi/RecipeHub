package com.recipehub.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.recipehub.app.data.model.Recipe
import java.io.File

// Fixed dark scrim tones for badges that sit on top of a photo thumbnail — intentionally not
// theme-dependent, since they need to stay readable against arbitrary image content either way.
private val ScrimStrong = Color(0xD30A2318)
private val ScrimSoft = Color(0x8C0A2318)
private val Cream = Color(0xFFF6F2E6)

private fun platformLabel(sourcePlatform: String): String = when (sourcePlatform) {
    "tiktok" -> "TikTok"
    "instagram" -> "Instagram"
    else -> sourcePlatform.replaceFirstChar { it.uppercase() }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onOverflowClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val meta = recipe.authorUsername?.takeIf { it.isNotBlank() }
        ?.let { "${platformLabel(recipe.sourcePlatform)} · $it" }
        ?: platformLabel(recipe.sourcePlatform)

    Card(
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Column {
            Box {
                AsyncImage(
                    model = recipe.thumbnailPath?.let { File(it) },
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
                Text(
                    platformLabel(recipe.sourcePlatform),
                    color = Cream,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ScrimStrong)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
                IconButton(
                    onClick = onOverflowClick,
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(ScrimSoft),
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = Cream)
                }
            }
            Column(modifier = Modifier.padding(start = 11.dp, end = 11.dp, top = 10.dp, bottom = 12.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
