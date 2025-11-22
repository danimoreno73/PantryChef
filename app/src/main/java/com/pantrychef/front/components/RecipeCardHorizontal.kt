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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.TextSecondary

@Composable
fun RecipeCardHorizontal(
    title: String,
    prepTime: String,
    servings: String,
    imageUrl: String?,
    badge: String?,
    badgeSeverity: BadgeSeverity = BadgeSeverity.SUCCESS,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
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
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = "$prepTime • $servings",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Badge
        if (badge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(
                text = badge,
                severity = badgeSeverity
            )
        }
    }
}

@Preview
@Composable
private fun RecipeCardHorizontalPreview() {
    PantryChefTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RecipeCardHorizontal(
                title = "Pasta con verduras",
                prepTime = "25 min",
                servings = "2 porciones",
                imageUrl = null,
                badge = "Todo listo",
                badgeSeverity = BadgeSeverity.SUCCESS,
                onClick = {}
            )
            RecipeCardHorizontal(
                title = "Tacos de pollo",
                prepTime = "30 min",
                servings = "3 porciones",
                imageUrl = "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=200",
                badge = "2/4",
                badgeSeverity = BadgeSeverity.WARNING,
                onClick = {}
            )
        }
    }
}