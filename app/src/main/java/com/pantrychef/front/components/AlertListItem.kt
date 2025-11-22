package com.pantrychef.front.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.TextSecondary

@Composable
fun AlertListItem(
    productName: String,
    message: String,
    categoryInfo: String,
    severity: BadgeSeverity,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon + Content
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = when (severity) {
                    BadgeSeverity.URGENT -> com.pantrychef.front.theme.BadgeUrgent
                    BadgeSeverity.WARNING -> com.pantrychef.front.theme.BadgeWarning
                    BadgeSeverity.LOW -> com.pantrychef.front.theme.BadgeWarning
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$message • $categoryInfo",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Badge
        StatusBadge(
            text = actionLabel,
            severity = severity
        )
    }
}

@Preview
@Composable
private fun AlertListItemPreview() {
    PantryChefTheme {
        Column {
            AlertListItem(
                productName = "Leche",
                message = "Baja en 2 días",
                categoryInfo = "Lácteos",
                severity = BadgeSeverity.WARNING,
                actionLabel = "Reponer",
                onClick = {}
            )
            AlertListItem(
                productName = "Huevos",
                message = "Quedan 3",
                categoryInfo = "Proteínas",
                severity = BadgeSeverity.LOW,
                actionLabel = "Bajo",
                onClick = {}
            )
        }
    }
}