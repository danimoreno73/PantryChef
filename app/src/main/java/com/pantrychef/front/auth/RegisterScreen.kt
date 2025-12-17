package com.pantrychef.front.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigation by viewModel.navigation.collectAsState()

    // Handle navigation
    LaunchedEffect(navigation) {
        when (navigation) {
            RegisterNavigation.ToHome -> {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
                viewModel.clearNavigation()
            }
            RegisterNavigation.ToLogin -> {
                navController.popBackStack()
                viewModel.clearNavigation()
            }
            null -> { /* No navigation */ }
        }
    }

    RegisterContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        // Top bar centrado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crear cuenta",
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
            Spacer(modifier = Modifier.height(16.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = PrimaryGreenLight,
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👨‍🍳",
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PantryChef",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Gestiona tu despensa y planifica sin desperdicios",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Field
            InputField(
                value = uiState.fullName,
                onValueChange = { onEvent(RegisterEvent.FullNameChanged(it)) },
                label = "Nombre completo",
                placeholder = "Ej. Ana Pérez",
                leadingIcon = Icons.Filled.Person,
                isError = uiState.fullNameError != null,
                errorMessage = uiState.fullNameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            InputField(
                value = uiState.email,
                onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
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
                onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
                label = "Contraseña",
                placeholder = "Mínimo 8 caracteres",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            InputField(
                value = uiState.confirmPassword,
                onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                label = "Confirmar contraseña",
                placeholder = "Repite tu contraseña",
                leadingIcon = Icons.Filled.Lock,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible)
                                "Ocultar contraseña"
                            else
                                "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Terms Checkbox
            Surface(
                color = PrimaryGreenLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.acceptedTerms,
                        onCheckedChange = { onEvent(RegisterEvent.TermsToggled(it)) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Column {
                        Text(
                            text = "Acepto los Términos y la Política",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Ver",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { /* TODO: Ver términos */ }
                        )
                    }
                }
            }

            if (uiState.termsError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.termsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Crear cuenta button
            PrimaryButton(
                text = "Crear cuenta",
                onClick = { onEvent(RegisterEvent.RegisterClicked) },
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading
            )

            if (uiState.registerError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.registerError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Separator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "o regístrate con",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Register Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialAuthButton(
                    provider = AuthProvider.APPLE,
                    onClick = {
                        onEvent(RegisterEvent.SocialRegisterClicked(AuthProvider.APPLE))
                    },
                    modifier = Modifier.weight(1f)
                )

                SocialAuthButton(
                    provider = AuthProvider.GOOGLE,
                    onClick = {
                        onEvent(RegisterEvent.SocialRegisterClicked(AuthProvider.GOOGLE))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ya tienes cuenta
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿Ya tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Inicia sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onEvent(RegisterEvent.LoginClicked)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun RegisterScreenPreview() {
    PantryChefTheme {
        RegisterContent(
            uiState = RegisterUiState(),
            onEvent = {}
        )
    }
}