package com.aura.ui.login

import com.aura.R
import com.aura.data.repository.LoginRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel associé à l'écran de connexion.
 * Il gère la logique de connexion et expose l'état de l'UI via un StateFlow.
 * Inclut la gestion des erreurs réseau.
 */
class LoginViewModel(private val repository: LoginRepository = LoginRepository()) : ViewModel() {

    // ---------------------------------------------------------------------------------------------
    // Propriétés et StateFlows Internes
    // ---------------------------------------------------------------------------------------------

    /** L'état modifiable (interne) de l'interface utilisateur de connexion. */
    private val _uiState = MutableStateFlow(LoginUiState())

    /** Champ identifier saisi par l'utilisateur */
    private val identifier: MutableStateFlow<String> = MutableStateFlow("")

    /** Champ password saisi par l'utilisateur */
    private val password: MutableStateFlow<String> = MutableStateFlow("")

    // ---------------------------------------------------------------------------------------------
    // Propriétés Exposées (Observables)
    // ---------------------------------------------------------------------------------------------

    /** L'état exposé (observable) de l'interface utilisateur de connexion. */
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Flow qui indique si le bouton login doit être activé.
     */
    val isLoginEnabled: StateFlow<Boolean> = createLoginEnabledFlow()

    // ---------------------------------------------------------------------------------------------
    // Fonctions d'Entrée Utilisateur (Intentions)
    // ---------------------------------------------------------------------------------------------

    fun setIdentifier(newIdentifier: String) {
        identifier.value = newIdentifier
        resetUiStateOnError()
    }

    fun setPassword(newPassword: String) {
        password.value = newPassword
        resetUiStateOnError()
    }

    /**
     * Lance la connexion via le repository.
     */
    fun login() {
        startLoginAttempt()

        viewModelScope.launch {
            try {
                // Appel de la logique de connexion dans une fonction séparée
                performLoginRequest()
            } catch (e: IOException) {
                handleNetworkError(e)
            } catch (e: Exception) {
                handleGenericError(e)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Fonctions de Logique Interne (Découpage de 'login')
    // ---------------------------------------------------------------------------------------------

    /**
     * Crée le StateFlow pour l'activation du bouton.
     */
    private fun createLoginEnabledFlow(): StateFlow<Boolean> =
        combine(identifier, password, _uiState) { id, pwd, state ->
            id.isNotBlank() && pwd.isNotBlank() && !state.isLoading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Ajout d'un timeout pour plus de robustesse
            initialValue = false
        )

    /**
     * Réinitialise l'état de succès/erreur lorsque l'utilisateur modifie les champs.
     */
    private fun resetUiStateOnError() {
        _uiState.update { it.copy(isSuccess = null, error = null) }
    }

    /**
     * Initialise l'état au début du processus de connexion.
     */
    private fun startLoginAttempt() {
        _uiState.update { it.copy(isLoading = true, isSuccess = null, error = null, userId = null) }
    }

    /**
     * Effectue l'appel réel au repository et gère la réponse du serveur.
     */
    private suspend fun performLoginRequest() {
        val response = repository.login(identifier.value, password.value)

        if (response.granted) {
            // Connexion réussie (du point de vue du serveur)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = true,
                    userId = identifier.value
                )
            }
        } else {
            // Connexion échouée (identifiants incorrects)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = R.string.error_bad_id // Message d'erreur spécifique
                )
            }
        }
    }

    /**
     * Gère spécifiquement les erreurs de réseau (IOException).
     */
    private fun handleNetworkError(e: IOException) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSuccess = false,
                error = R.string.error_network
            )
        }
    }

    /**
     * Gère toutes les autres exceptions non prévues.
     */
    private fun handleGenericError(e: Exception) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSuccess = false,
                error = R.string.error_generic
            )
        }
    }
}