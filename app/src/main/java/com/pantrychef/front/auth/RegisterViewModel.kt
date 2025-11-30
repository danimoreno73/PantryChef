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

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
    val isLoading: Boolean = false,
    val registerError: String? = null
)

sealed interface RegisterEvent {
    data class FullNameChanged(val name: String) : RegisterEvent
    data class EmailChanged(val email: String) : RegisterEvent
    data class PasswordChanged(val password: String) : RegisterEvent
    data class ConfirmPasswordChanged(val password: String) : RegisterEvent
    data class TermsToggled(val accepted: Boolean) : RegisterEvent
    object RegisterClicked : RegisterEvent
    data class SocialRegisterClicked(val provider: AuthProvider) : RegisterEvent
    object LoginClicked : RegisterEvent
}

sealed interface RegisterNavigation {
    object ToHome : RegisterNavigation
    object ToLogin : RegisterNavigation
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<RegisterNavigation?>(null)
    val navigation: StateFlow<RegisterNavigation?> = _navigation.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged -> {
                _uiState.update { it.copy(fullName = event.name, fullNameError = null) }
            }

            is RegisterEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email, emailError = null) }
            }

            is RegisterEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.password, passwordError = null) }
            }

            is RegisterEvent.ConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPassword = event.password, confirmPasswordError = null) }
            }

            is RegisterEvent.TermsToggled -> {
                _uiState.update { it.copy(acceptedTerms = event.accepted, termsError = null) }
            }

            RegisterEvent.RegisterClicked -> {
                register()
            }

            is RegisterEvent.SocialRegisterClicked -> {
                // TODO: Implement social register
                _navigation.value = RegisterNavigation.ToHome
            }

            RegisterEvent.LoginClicked -> {
                _navigation.value = RegisterNavigation.ToLogin
            }
        }
    }

    private fun register() {
        val fullNameError = if (_uiState.value.fullName.length < 2) {
            "El nombre debe tener al menos 2 caracteres"
        } else null

        val emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            "Email inválido"
        } else null

        val passwordError = if (_uiState.value.password.length < 8) {
            "La contraseña debe tener al menos 8 caracteres"
        } else null

        val confirmPasswordError = if (_uiState.value.password != _uiState.value.confirmPassword) {
            "Las contraseñas no coinciden"
        } else null

        val termsError = if (!_uiState.value.acceptedTerms) {
            "Debes aceptar los términos y condiciones"
        } else null

        if (fullNameError != null || emailError != null || passwordError != null ||
            confirmPasswordError != null || termsError != null) {
            _uiState.update { it.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                termsError = termsError
            )}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, registerError = null) }

            val result = authRepository.register(
                name = _uiState.value.fullName,
                email = _uiState.value.email,
                password = _uiState.value.password
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigation.value = RegisterNavigation.ToHome
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        registerError = error.message ?: "Error al registrar"
                    )}
                }
            )
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}