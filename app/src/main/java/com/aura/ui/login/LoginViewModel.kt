package com.aura.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            // Simulation d’un appel réseau
            delay(1000)

            if (username == "admin" && password == "admin") {
                _uiState.value = LoginUiState(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = LoginUiState(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Identifiant ou mot de passe incorrect"
                )
            }
        }
    }
}