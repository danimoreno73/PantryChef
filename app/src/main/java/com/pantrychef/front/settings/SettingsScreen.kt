package com.pantrychef.front.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pantrychef.front.components.SettingsItem
import com.pantrychef.front.components.SettingsItemWithToggle
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight
import com.pantrychef.front.theme.TextSecondary

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigation by viewModel.navigation.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (navigation) {
            SettingsNavigation.ToProfile -> {
                // TODO: Navigate to profile edit
                viewModel.clearNavigation()
            }
            SettingsNavigation.ToLanguage -> {
                // TODO: Navigate to language selection
                viewModel.clearNavigation()
            }
            SettingsNavigation.ToLogin -> {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    SettingsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // User profile section
        Surface(
            onClick = { onEvent(SettingsEvent.ProfileClicked) },
            color = PrimaryGreenLight,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.userName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preferencias section
        SectionHeader("Preferencias")

        SettingsItem(
            icon = Icons.Filled.Language,
            title = "Idioma",
            subtitle = uiState.language,
            onClick = { onEvent(SettingsEvent.LanguageClicked) }
        )

        Divider(modifier = Modifier.padding(start = 56.dp))

        SettingsItemWithToggle(
            icon = Icons.Filled.DarkMode,
            title = "Modo oscuro",
            subtitle = "Tema de la aplicación",
            checked = uiState.darkModeEnabled,
            onCheckedChange = { onEvent(SettingsEvent.DarkModeToggled(it)) }
        )

        Divider(modifier = Modifier.padding(start = 56.dp))

        SettingsItemWithToggle(
            icon = Icons.Filled.Notifications,
            title = "Notificaciones",
            subtitle = "Alertas de productos y recetas",
            checked = uiState.notificationsEnabled,
            onCheckedChange = { onEvent(SettingsEvent.NotificationsToggled(it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Datos section
        SectionHeader("Datos")

        SettingsItem(
            icon = Icons.Filled.Upload,
            title = "Exportar datos",
            subtitle = "Descargar copia de seguridad",
            onClick = { onEvent(SettingsEvent.ExportDataClicked) }
        )

        Divider(modifier = Modifier.padding(start = 56.dp))

        SettingsItem(
            icon = Icons.Filled.Download,
            title = "Importar datos",
            subtitle = "Restaurar desde archivo",
            onClick = { onEvent(SettingsEvent.ImportDataClicked) }
        )

        Divider(modifier = Modifier.padding(start = 56.dp))

        SettingsItem(
            icon = Icons.Filled.Delete,
            title = "Limpiar datos",
            subtitle = "Eliminar toda la información",
            onClick = { onEvent(SettingsEvent.ClearDataClicked) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Acerca de section
        SectionHeader("Acerca de")

        SettingsItem(
            icon = Icons.Filled.Info,
            title = "Versión",
            subtitle = uiState.appVersion,
            showChevron = false,
            onClick = { onEvent(SettingsEvent.AboutClicked) }
        )

        Divider(modifier = Modifier.padding(start = 56.dp))

        SettingsItem(
            icon = Icons.Filled.Security,
            title = "Política de privacidad",
            onClick = { onEvent(SettingsEvent.PrivacyPolicyClicked) }
        )

        Divider(modifier = Modifier.padding(start = 56.dp))

        SettingsItem(
            icon = Icons.Filled.Description,
            title = "Términos y condiciones",
            onClick = { onEvent(SettingsEvent.TermsClicked) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Logout button
        Surface(
            onClick = { onEvent(SettingsEvent.LogoutClicked) },
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun SettingsScreenPreview() {
    PantryChefTheme {
        SettingsContent(
            uiState = SettingsUiState(
                userName = "Usuario Mock",
                userEmail = "usuario@pantrychef.com"
            ),
            onEvent = {}
        )
    }
}