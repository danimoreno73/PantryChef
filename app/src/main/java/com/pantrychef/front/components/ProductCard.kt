package com.pantrychef.front.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

enum class ProductAlertLevel {
    NONE,           // Sin alerta
    LOW_STOCK,      // Stock bajo
    EXPIRING_SOON,  // Próximo a vencer
    EXPIRED         // Vencido
}

@Composable
fun ProductCard(
    name: String,
    quantity: String,
    category: String,
    location: String,
    imageUrl: String?,
    alertLevel: ProductAlertLevel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = when (alertLevel) {
            ProductAlertLevel.EXPIRED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ProductAlertLevel.EXPIRING_SOON -> com.pantrychef.front.theme.BadgeWarning.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = when (category.lowercase()) {
                            "lácteos", "dairy" -> "🥛"
                            "proteínas", "proteins" -> "🍖"
                            "granos", "grains" -> "🌾"
                            "verduras", "vegetables" -> "🥬"
                            "frutas", "fruits" -> "🍎"
                            "condimentos", "condiments" -> "🧂"
                            else -> "📦"
                        },
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Alert badge
                if (alertLevel != ProductAlertLevel.NONE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Surface(
                            color = when (alertLevel) {
                                ProductAlertLevel.EXPIRED -> MaterialTheme.colorScheme.error
                                ProductAlertLevel.EXPIRING_SOON -> com.pantrychef.front.theme.BadgeWarning
                                ProductAlertLevel.LOW_STOCK -> com.pantrychef.front.theme.BadgeInfo
                                else -> MaterialTheme.colorScheme.surface
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            // Content section
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = quantity,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProductCardPreview() {
    PantryChefTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductCard(
                name = "Leche entera",
                quantity = "2.0 L",
                category = "Lácteos",
                location = "Refrigerador",
                imageUrl = null,
                alertLevel = ProductAlertLevel.LOW_STOCK,
                onClick = {},
                modifier = Modifier.weight(1f)
            )

            ProductCard(
                name = "Huevos",
                quantity = "3 uds",
                category = "Proteínas",
                location = "Refrigerador",
                imageUrl = null,
                alertLevel = ProductAlertLevel.EXPIRING_SOON,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}