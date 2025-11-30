package com.pantrychef.front.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pantrychef.front.auth.AuthProvider
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight

@Composable
fun SocialAuthButton(
    provider: AuthProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = PrimaryGreenLight,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(56.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (provider) {
                    AuthProvider.APPLE -> "🍎"
                    AuthProvider.GOOGLE -> "G"
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = when (provider) {
                    AuthProvider.APPLE -> "Apple"
                    AuthProvider.GOOGLE -> "Google"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview
@Composable
private fun SocialAuthButtonPreview() {
    PantryChefTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SocialAuthButton(
                provider = AuthProvider.APPLE,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            SocialAuthButton(
                provider = AuthProvider.GOOGLE,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}