package com.pantrychef.front.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pantrychef.front.components.*
import com.pantrychef.front.navigation.Routes
import com.pantrychef.front.theme.PantryChefTheme
import com.pantrychef.front.theme.PrimaryGreenLight
import com.pantrychef.front.theme.TextSecondary

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigation by viewModel.navigation.collectAsState()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (navigation) {
            LoginNavigation.ToHome -> {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
                viewModel.clearNavigation()
            }
            LoginNavigation.ToRegister -> {
                navController.navigate(Routes.REGISTER)
                viewModel.clearNavigation()
            }
            LoginNavigation.ToForgotPassword -> {
                // TODO: Implementar pantalla de recuperación
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    LoginContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar centrado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = PrimaryGreenLight,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👨‍🍳",
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "PantryChef",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = "Organiza tu despensa, descubre recetas y evita el desperdicio",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email Field
            InputField(
                value = uiState.email,
                onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                label = "Correo electrónico",
                placeholder = "tu@correo.com",
                leadingIcon = Icons.Filled.Email,
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            InputField(
                value = uiState.password,
                onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                label = "Contraseña",
                leadingIcon = Icons.Filled.Lock,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Ocultar contraseña"
                            else
                                "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recordarme y Olvidaste contraseña
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.rememberMe,
                        onCheckedChange = { onEvent(LoginEvent.RememberMeToggled(it)) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Recordarme",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        onEvent(LoginEvent.ForgotPasswordClicked)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Entrar
            PrimaryButton(
                text = "Entrar",
                onClick = { onEvent(LoginEvent.LoginClicked) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading
            )

            // Error general
            if (uiState.loginError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.loginError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Separador
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "o continúa con",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Login Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialAuthButton(
                    provider = AuthProvider.APPLE,
                    onClick = {
                        onEvent(LoginEvent.SocialLoginClicked(AuthProvider.APPLE))
                    },
                    modifier = Modifier.weight(1f)
                )

                SocialAuthButton(
                    provider = AuthProvider.GOOGLE,
                    onClick = {
                        onEvent(LoginEvent.SocialLoginClicked(AuthProvider.GOOGLE))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Crear cuenta
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onEvent(LoginEvent.RegisterClicked)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    PantryChefTheme {
        LoginContent(
            uiState = LoginUiState(),
            onEvent = {}
        )
    }
}