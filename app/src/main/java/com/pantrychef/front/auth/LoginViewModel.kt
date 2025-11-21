package com.pantrychef.front.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.front.components.AuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null
)

sealed interface LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent
    data class PasswordChanged(val password: String) : LoginEvent
    data class RememberMeToggled(val checked: Boolean) : LoginEvent
    object LoginClicked : LoginEvent
    data class SocialLoginClicked(val provider: AuthProvider) : LoginEvent
    object ForgotPasswordClicked : LoginEvent
    object RegisterClicked : LoginEvent
}

sealed interface LoginNavigation {
    object ToHome : LoginNavigation
    object ToRegister : LoginNavigation
    object ToForgotPassword : LoginNavigation
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    // TODO: Inject AuthRepository cuando esté listo
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<LoginNavigation?>(null)
    val navigation: StateFlow<LoginNavigation?> = _navigation.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _uiState.update { it.copy(
                    email = event.email,
                    emailError = null
                ) }
            }

            is LoginEvent.PasswordChanged -> {
                _uiState.update { it.copy(
                    password = event.password,
                    passwordError = null
                ) }
            }

            is LoginEvent.RememberMeToggled -> {
                _uiState.update { it.copy(rememberMe = event.checked) }
            }

            LoginEvent.LoginClicked -> {
                performLogin()
            }

            is LoginEvent.SocialLoginClicked -> {
                performSocialLogin(event.provider)
            }

            LoginEvent.ForgotPasswordClicked -> {
                _navigation.value = LoginNavigation.ToForgotPassword
            }

            LoginEvent.RegisterClicked -> {
                _navigation.value = LoginNavigation.ToRegister
            }
        }
    }

    private fun performLogin() {
        // Validación local
        val emailError = validateEmail(_uiState.value.email)
        val passwordError = validatePassword(_uiState.value.password)

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(
                emailError = emailError,
                passwordError = passwordError
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }

            // TODO: Llamar a AuthRepository.login()
            // Por ahora, simulamos éxito después de 1 segundo
            kotlinx.coroutines.delay(1000)

            // Simulación: siempre éxito
            _uiState.update { it.copy(isLoading = false) }
            _navigation.value = LoginNavigation.ToHome
        }
    }

    private fun performSocialLogin(provider: AuthProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // TODO: Implementar OAuth flow
            kotlinx.coroutines.delay(1000)

            _uiState.update { it.copy(isLoading = false) }
            _navigation.value = LoginNavigation.ToHome
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo es requerido"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Correo electrónico inválido"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}