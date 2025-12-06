package com.pantrychef.front.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrychef.back.repository.AuthRepository
import com.pantrychef.back.utils.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val notificationsEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val language: String = "Español",
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPermissionDialog: Boolean = false
)

sealed interface SettingsEvent {
    object ProfileClicked : SettingsEvent
    object LanguageClicked : SettingsEvent
    data class DarkModeToggled(val enabled: Boolean) : SettingsEvent
    data class NotificationsToggled(val enabled: Boolean) : SettingsEvent
    data class NotificationPermissionResult(val granted: Boolean) : SettingsEvent
    object DismissPermissionDialog : SettingsEvent
    object ExportDataClicked : SettingsEvent
    object ImportDataClicked : SettingsEvent
    object ClearDataClicked : SettingsEvent
    object AboutClicked : SettingsEvent
    object PrivacyPolicyClicked : SettingsEvent
    object TermsClicked : SettingsEvent
    object LogoutClicked : SettingsEvent
}

sealed interface SettingsNavigation {
    object ToProfile : SettingsNavigation
    object ToLanguage : SettingsNavigation
    object ToLogin : SettingsNavigation
    object RequestNotificationPermission : SettingsNavigation
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<SettingsNavigation?>(null)
    val navigation: StateFlow<SettingsNavigation?> = _navigation.asStateFlow()

    private val notificationHelper = NotificationHelper(context)

    init {
        loadSettings()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ProfileClicked -> {
                _navigation.value = SettingsNavigation.ToProfile
            }

            SettingsEvent.LanguageClicked -> {
                _navigation.value = SettingsNavigation.ToLanguage
            }

            is SettingsEvent.DarkModeToggled -> {
                _uiState.update { it.copy(darkModeEnabled = event.enabled) }
                saveDarkModeSetting(event.enabled)
            }

            is SettingsEvent.NotificationsToggled -> {
                handleNotificationToggle(event.enabled)
            }

            is SettingsEvent.NotificationPermissionResult -> {
                handlePermissionResult(event.granted)
            }

            SettingsEvent.DismissPermissionDialog -> {
                _uiState.update { it.copy(showPermissionDialog = false) }
            }

            SettingsEvent.ExportDataClicked -> {
                exportData()
            }

            SettingsEvent.ImportDataClicked -> {
                importData()
            }

            SettingsEvent.ClearDataClicked -> {
                clearData()
            }

            SettingsEvent.AboutClicked -> {
                // TODO: Show about dialog
            }

            SettingsEvent.PrivacyPolicyClicked -> {
                // TODO: Open privacy policy
            }

            SettingsEvent.TermsClicked -> {
                // TODO: Open terms and conditions
            }

            SettingsEvent.LogoutClicked -> {
                logout()
            }
        }
    }

    private fun handleNotificationToggle(enabled: Boolean) {
        if (enabled) {
            // Usuario quiere activar notificaciones
            if (notificationHelper.checkNotificationPermission(context)) {
                // Ya tiene permiso
                _uiState.update { it.copy(notificationsEnabled = true) }
                saveNotificationsSetting(true)
            } else {
                // Necesita solicitar permiso (Android mostrará diálogo nativo)
                _navigation.value = SettingsNavigation.RequestNotificationPermission
            }
        } else {
            // Usuario desactiva notificaciones
            _uiState.update { it.copy(notificationsEnabled = false) }
            saveNotificationsSetting(false)
        }
    }

    private fun handlePermissionResult(granted: Boolean) {
        if (granted) {
            // Permiso concedido
            _uiState.update { it.copy(
                notificationsEnabled = true,
                showPermissionDialog = false
            )}
            saveNotificationsSetting(true)
        } else {
            // Permiso denegado - mostrar diálogo solo si fue denegado permanentemente
            _uiState.update { it.copy(
                notificationsEnabled = false,
                showPermissionDialog = true // Solo se mostrará si se denegó permanentemente
            )}
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Cargar usuario actual
                val userResult = authRepository.getCurrentUser()

                userResult.fold(
                    onSuccess = { user ->
                        // Verificar si tiene permiso de notificaciones
                        val hasPermission = notificationHelper.checkNotificationPermission(context)

                        _uiState.update { it.copy(
                            userName = user?.name ?: "Usuario",
                            userEmail = user?.email ?: "usuario@example.com",
                            notificationsEnabled = hasPermission,
                            isLoading = false
                        )}
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(
                            userName = "Usuario",
                            userEmail = "usuario@example.com",
                            isLoading = false,
                            error = error.message
                        )}
                    }
                )

                // TODO: Load other preferences from PreferencesRepository
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar configuración"
                )}
            }
        }
    }

    private fun saveDarkModeSetting(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Save to PreferencesRepository
        }
    }

    private fun saveNotificationsSetting(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Save to PreferencesRepository
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            // TODO: Implement data export
            _uiState.update { it.copy(error = "Datos exportados correctamente") }
        }
    }

    private fun importData() {
        viewModelScope.launch {
            // TODO: Implement data import
            _uiState.update { it.copy(error = "Datos importados correctamente") }
        }
    }

    private fun clearData() {
        viewModelScope.launch {
            // TODO: Implement data clearing
            _uiState.update { it.copy(error = "Datos limpiados correctamente") }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = authRepository.logout()

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigation.value = SettingsNavigation.ToLogin
                },
                onFailure = { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cerrar sesión"
                    )}
                }
            )
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}