package com.pantrychef.front.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight

enum class AuthProvider {
    APPLE,
    GOOGLE
}

@Composable
fun SocialAuthButton(
    provider: AuthProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text = when (provider) {
        AuthProvider.APPLE -> "Apple"
        AuthProvider.GOOGLE -> "Google"
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreenLight,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: Agregar iconos reales cuando tengamos material-icons-extended
            Text(
                text = "🍎", // Emoji temporal para Apple
                modifier = Modifier.width(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Preview
@Composable
private fun SocialAuthButtonPreview() {
    PantryChefTheme {
        SocialAuthButton(
            provider = AuthProvider.GOOGLE,
            onClick = {}
        )
    }
}