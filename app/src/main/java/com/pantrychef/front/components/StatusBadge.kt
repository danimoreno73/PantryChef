package com.pantrychef.front.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantrychef.front.theme.BadgeInfo
import com.pantrychef.front.theme.BadgeSuccess
import com.pantrychef.front.theme.BadgeUrgent
import com.pantrychef.front.theme.BadgeWarning
import com.pantrychef.front.theme.PantryChefTheme

enum class BadgeSeverity {
    URGENT,    // Naranja - Urgente, Vence pronto
    WARNING,   // Amarillo - Bajo, Reponer
    SUCCESS,   // Verde - Cocinable, En despensa
    INFO,      // Azul - Información general
    LOW,
    CRITICAL //ROJO
}

@Composable
fun StatusBadge(
    text: String,
    severity: BadgeSeverity,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (severity) {
        BadgeSeverity.CRITICAL -> Color(0xFFD32F2F)
        BadgeSeverity.URGENT -> BadgeUrgent
        BadgeSeverity.WARNING -> BadgeWarning
        BadgeSeverity.SUCCESS -> BadgeSuccess
        BadgeSeverity.INFO -> BadgeInfo
        BadgeSeverity.LOW -> BadgeWarning
    }

    Text(
        text = text,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}

@Preview
@Composable
private fun StatusBadgePreview() {
    PantryChefTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            StatusBadge(text = "Urgente", severity = BadgeSeverity.URGENT)
            Spacer(modifier = Modifier.height(8.dp))
            StatusBadge(text = "Bajo", severity = BadgeSeverity.WARNING)
            Spacer(modifier = Modifier.height(8.dp))
            StatusBadge(text = "Cocinable", severity = BadgeSeverity.SUCCESS)
        }
    }
}