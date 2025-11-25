package com.pantrychef.front.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

enum class MealStatus {
    PLAN,           // Plan - planificado
    RECIPE,         // Receta - de una receta
    COOKED,         // Cocinado - completado
    LEFTOVER        // Sobrante - sobras
}

@Composable
fun MealLogCard(
    mealName: String,
    mealType: String,
    time: String,
    calories: Int,
    imageUrl: String?,
    status: MealStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = mealName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = when (mealType) {
                            "Desayuno" -> "🥐"
                            "Almuerzo" -> "🍽️"
                            "Cena" -> "🍲"
                            "Comida" -> "🥗"
                            else -> "🍴"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$mealType: $mealName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "$time • $calories kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status badge
            Surface(
                color = when (status) {
                    MealStatus.PLAN -> MaterialTheme.colorScheme.primaryContainer
                    MealStatus.RECIPE -> MaterialTheme.colorScheme.secondaryContainer
                    MealStatus.COOKED -> MaterialTheme.colorScheme.tertiaryContainer
                    MealStatus.LEFTOVER -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (status) {
                        MealStatus.PLAN -> "Plan"
                        MealStatus.RECIPE -> "Receta"
                        MealStatus.COOKED -> "Cocinado"
                        MealStatus.LEFTOVER -> "Sobrante"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (status) {
                        MealStatus.PLAN -> MaterialTheme.colorScheme.onPrimaryContainer
                        MealStatus.RECIPE -> MaterialTheme.colorScheme.onSecondaryContainer
                        MealStatus.COOKED -> MaterialTheme.colorScheme.onTertiaryContainer
                        MealStatus.LEFTOVER -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun MealLogCardPreview() {
    PantryChefTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            MealLogCard(
                mealName = "Avena con fruta",
                mealType = "Desayuno",
                time = "07:45",
                calories = 320,
                imageUrl = null,
                status = MealStatus.PLAN,
                onClick = {}
            )

            MealLogCard(
                mealName = "Tacos de pollo",
                mealType = "Almuerzo",
                time = "13:20",
                calories = 540,
                imageUrl = "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=200",
                status = MealStatus.RECIPE,
                onClick = {}
            )

            MealLogCard(
                mealName = "Pasta con verduras",
                mealType = "Cena",
                time = "20:10",
                calories = 610,
                imageUrl = null,
                status = MealStatus.COOKED,
                onClick = {}
            )
        }
    }
}