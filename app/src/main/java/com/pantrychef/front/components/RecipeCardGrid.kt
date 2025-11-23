package com.pantrychef.front.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.TextSecondary

@Composable
fun RecipeCardGrid(
    title: String,
    prepTime: String,
    servings: String,
    imageUrl: String?,
    badge: String?,
    badgeSeverity: BadgeSeverity = BadgeSeverity.SUCCESS,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        // Image section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "🍽️",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Badge in top-right corner
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    StatusBadge(
                        text = badge,
                        severity = badgeSeverity
                    )
                }
            }
        }

        // Content section
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$prepTime • $servings",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Preview
@Composable
private fun RecipeCardGridPreview() {
    PantryChefTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecipeCardGrid(
                title = "Pasta con verduras",
                prepTime = "25 min",
                servings = "2 porciones",
                imageUrl = null,
                badge = "Todo listo",
                badgeSeverity = BadgeSeverity.SUCCESS,
                onClick = {},
                modifier = Modifier.weight(1f)
            )

            RecipeCardGrid(
                title = "Tacos de pollo",
                prepTime = "30 min",
                servings = "3 porciones",
                imageUrl = "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=300",
                badge = "2/4",
                badgeSeverity = BadgeSeverity.WARNING,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}