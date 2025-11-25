package com.pantrychef.front.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "Español",
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false
)

sealed interface SettingsEvent {
    object ProfileClicked : SettingsEvent
    object LanguageClicked : SettingsEvent
    data class NotificationsToggled(val enabled: Boolean) : SettingsEvent
    data class DarkModeToggled(val enabled: Boolean) : SettingsEvent
    object ExportDataClicked : SettingsEvent
    object ImportDataClicked : SettingsEvent
    object ClearDataClicked : SettingsEvent
    object PrivacyPolicyClicked : SettingsEvent
    object TermsClicked : SettingsEvent
    object AboutClicked : SettingsEvent
    object LogoutClicked : SettingsEvent
}

sealed interface SettingsNavigation {
    object ToProfile : SettingsNavigation
    object ToLanguage : SettingsNavigation
    object ToLogin : SettingsNavigation
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: Inject UserRepository, PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _navigation = MutableStateFlow<SettingsNavigation?>(null)
    val navigation: StateFlow<SettingsNavigation?> = _navigation.asStateFlow()

    init {
        loadUserSettings()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.ProfileClicked -> {
                _navigation.value = SettingsNavigation.ToProfile
            }

            SettingsEvent.LanguageClicked -> {
                _navigation.value = SettingsNavigation.ToLanguage
            }

            is SettingsEvent.NotificationsToggled -> {
                _uiState.update { it.copy(notificationsEnabled = event.enabled) }
                saveNotificationPreference(event.enabled)
            }

            is SettingsEvent.DarkModeToggled -> {
                _uiState.update { it.copy(darkModeEnabled = event.enabled) }
                saveDarkModePreference(event.enabled)
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

            SettingsEvent.PrivacyPolicyClicked -> {
                // TODO: Open privacy policy URL
            }

            SettingsEvent.TermsClicked -> {
                // TODO: Open terms URL
            }

            SettingsEvent.AboutClicked -> {
                // TODO: Show about dialog
            }

            SettingsEvent.LogoutClicked -> {
                logout()
            }
        }
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // TODO: Load from repository
            kotlinx.coroutines.delay(300)

            _uiState.update { it.copy(
                userName = "Usuario Mock",
                userEmail = "usuario@pantrychef.com",
                notificationsEnabled = true,
                darkModeEnabled = false,
                language = "Español",
                isLoading = false
            )}
        }
    }

    private fun saveNotificationPreference(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Save to repository
            kotlinx.coroutines.delay(100)
        }
    }

    private fun saveDarkModePreference(enabled: Boolean) {
        viewModelScope.launch {
            // TODO: Save to repository
            kotlinx.coroutines.delay(100)
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            // TODO: Export data to file
            kotlinx.coroutines.delay(500)
        }
    }

    private fun importData() {
        viewModelScope.launch {
            // TODO: Import data from file
            kotlinx.coroutines.delay(500)
        }
    }

    private fun clearData() {
        viewModelScope.launch {
            // TODO: Clear all user data
            kotlinx.coroutines.delay(500)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            // TODO: Call auth repository logout
            kotlinx.coroutines.delay(500)
            _navigation.value = SettingsNavigation.ToLogin
        }
    }

    fun clearNavigation() {
        _navigation.value = null
    }
}