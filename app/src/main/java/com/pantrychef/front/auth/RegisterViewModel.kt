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
    data class FullNameChanged(val fullName: String) : RegisterEvent
    data class EmailChanged(val email: String) : RegisterEvent
    data class PasswordChanged(val password: String) : RegisterEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterEvent
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
    // TODO: Inject AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<RegisterNavigation?>(null)
    val navigation: StateFlow<RegisterNavigation?> = _navigation.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged -> {
                _uiState.update { it.copy(
                    fullName = event.fullName,
                    fullNameError = null
                ) }
            }

            is RegisterEvent.EmailChanged -> {
                _uiState.update { it.copy(
                    email = event.email,
                    emailError = null
                ) }
            }

            is RegisterEvent.PasswordChanged -> {
                _uiState.update { it.copy(
                    password = event.password,
                    passwordError = null
                ) }
            }

            is RegisterEvent.ConfirmPasswordChanged -> {
                _uiState.update { it.copy(
                    confirmPassword = event.confirmPassword,
                    confirmPasswordError = null
                ) }
            }

            is RegisterEvent.TermsToggled -> {
                _uiState.update { it.copy(
                    acceptedTerms = event.accepted,
                    termsError = null
                ) }
            }

            RegisterEvent.RegisterClicked -> {
                performRegister()
            }

            is RegisterEvent.SocialRegisterClicked -> {
                performSocialRegister(event.provider)
            }

            RegisterEvent.LoginClicked -> {
                _navigation.value = RegisterNavigation.ToLogin
            }
        }
    }

    private fun performRegister() {
        // Validación local
        val fullNameError = validateFullName(_uiState.value.fullName)
        val emailError = validateEmail(_uiState.value.email)
        val passwordError = validatePassword(_uiState.value.password)
        val confirmPasswordError = validateConfirmPassword(
            _uiState.value.password,
            _uiState.value.confirmPassword
        )
        val termsError = validateTerms(_uiState.value.acceptedTerms)

        if (fullNameError != null || emailError != null ||
            passwordError != null || confirmPasswordError != null ||
            termsError != null) {
            _uiState.update { it.copy(
                fullNameError = fullNameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                termsError = termsError
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, registerError = null) }

            // TODO: Llamar a AuthRepository.register()
            kotlinx.coroutines.delay(1000)

            // Simulación: siempre éxito
            _uiState.update { it.copy(isLoading = false) }
            _navigation.value = RegisterNavigation.ToHome
        }
    }

    private fun performSocialRegister(provider: AuthProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // TODO: Implementar OAuth flow
            kotlinx.coroutines.delay(1000)

            _uiState.update { it.copy(isLoading = false) }
            _navigation.value = RegisterNavigation.ToHome
        }
    }

    private fun validateFullName(fullName: String): String? {
        return when {
            fullName.isBlank() -> "El nombre es requerido"
            fullName.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
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
            password.length < 8 -> "Mínimo 8 caracteres"
            else -> null
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    private fun validateTerms(accepted: Boolean): String? {
        return if (!accepted) "Debes aceptar los términos" else null
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}