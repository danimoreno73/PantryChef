package com.pantrychef.front.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.repository.AuthRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<LoginNavigation?>(null)
    val navigation: StateFlow<LoginNavigation?> = _navigation.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email, emailError = null) }
            }

            is LoginEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.password, passwordError = null) }
            }

            is LoginEvent.RememberMeToggled -> {
                _uiState.update { it.copy(rememberMe = event.checked) }
            }

            LoginEvent.LoginClicked -> {
                login()
            }

            is LoginEvent.SocialLoginClicked -> {
                // TODO: Implement social login
                _navigation.value = LoginNavigation.ToHome
            }

            LoginEvent.ForgotPasswordClicked -> {
                _navigation.value = LoginNavigation.ToForgotPassword
            }

            LoginEvent.RegisterClicked -> {
                _navigation.value = LoginNavigation.ToRegister
            }
        }
    }

    private fun login() {
        val emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            "Email inválido"
        } else null

        val passwordError = if (_uiState.value.password.length < 6) {
            "La contraseña debe tener al menos 6 caracteres"
        } else null

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(
                emailError = emailError,
                passwordError = passwordError
            )}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loginError = null) }

            val result = authRepository.login(
                email = _uiState.value.email,
                password = _uiState.value.password
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigation.value = LoginNavigation.ToHome
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        loginError = error.message ?: "Error al iniciar sesión"
                    )}
                }
            )
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}