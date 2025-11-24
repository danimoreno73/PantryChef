package com.pantrychef.front.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.TextSecondary

@Composable
fun ShoppingItemRow(
    name: String,
    quantity: Double,
    unit: String,
    isChecked: Boolean,
    source: String,
    sourceTag: String,
    sourceTagSeverity: BadgeSeverity,
    onCheckedChange: (Boolean) -> Unit,
    onQuantityIncrease: () -> Unit,
    onQuantityDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Item info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                color = if (isChecked)
                    TextSecondary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = source,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                StatusBadge(
                    text = sourceTag,
                    severity = sourceTagSeverity
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Quantity stepper
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onQuantityDecrease,
                modifier = Modifier.size(32.dp)
            ) {
                Text("-", style = MaterialTheme.typography.titleMedium)
            }

            Text(
                text = "${quantity.toInt()} $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.widthIn(min = 50.dp)
            )

            IconButton(
                onClick = onQuantityIncrease,
                modifier = Modifier.size(32.dp)
            ) {
                Text("+", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview
@Composable
private fun ShoppingItemRowPreview() {
    PantryChefTheme {
        Column {
            ShoppingItemRow(
                name = "Tortillas",
                quantity = 12.0,
                unit = "uds",
                isChecked = false,
                source = "Para Tacos de pollo",
                sourceTag = "Receta",
                sourceTagSeverity = BadgeSeverity.INFO,
                onCheckedChange = {},
                onQuantityIncrease = {},
                onQuantityDecrease = {}
            )

            ShoppingItemRow(
                name = "Leche",
                quantity = 2.0,
                unit = "L",
                isChecked = true,
                source = "Bajo stock",
                sourceTag = "Sugerido",
                sourceTagSeverity = BadgeSeverity.WARNING,
                onCheckedChange = {},
                onQuantityIncrease = {},
                onQuantityDecrease = {}
            )
        }
    }
}