package com.aura.ui.login

import com.aura.ui.Logger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.R
import com.aura.ui.repository.LoginRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel associé à l'écran de connexion.
 * Il gère la logique de connexion et expose l'état de l'UI via un StateFlow.
 * Inclut la gestion des erreurs réseau.
 */
class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    // ---------------------------------------------------------------------------------------------
    // GESTION DU CONTENEUR D'ÉTAT (LoginUiState)
    // ---------------------------------------------------------------------------------------------

    /** L'état modifiable (interne) de l'interface utilisateur de connexion. */
    private val _uiState = MutableStateFlow(LoginUiState())
    /** L'état exposé (observable) de l'interface utilisateur de connexion. */
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Champ identifier saisi par l'utilisateur */
    private val identifier : MutableStateFlow<String> = MutableStateFlow("")

    fun setIdentifier(newIdentifier: String){
        identifier.value = newIdentifier
        // Optionnel : Réinitialiser l'état de succès/erreur lorsque l'utilisateur modifie les champs
        _uiState.update { it.copy(isSuccess = null, error = null) }
    }

    /** Champ password saisi par l'utilisateur */
    private val password : MutableStateFlow<String> = MutableStateFlow("")

    fun setPassword(newPassword: String){
        password.value = newPassword
        // Optionnel : Réinitialiser l'état de succès/erreur lorsque l'utilisateur modifie les champs
        _uiState.update { it.copy(isSuccess = null, error = null) }
    }

    /**
     * Flow qui indique si le bouton login doit être activé.
     * Le bouton est activé uniquement si identifier et password ne sont pas vides.
     */
    val isLoginEnabled: StateFlow<Boolean> = combine(identifier, password) { id, pwd ->
        id.isNotBlank() && pwd.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    /**
     * Lance la connexion via le repository.
     * Met à jour le [LoginUiState] pour gérer le chargement, le succès et l'erreur.
     */
    fun login() {
        // 1. Début du chargement
        _uiState.update { it.copy(isLoading = true, isSuccess = null, error = null,userId = null) }

        viewModelScope.launch {
            try {
                Logger.d("Tentative de connexion pour user id ${identifier.value}")
                val response = repository.login(identifier.value, password.value)

                // 2. Connexion réussie/échouée (du point de vue du serveur)
                if (response.granted) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, userId = identifier.value) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            error = R.string.error_bad_id // Message d'erreur spécifique à la logique
                        )
                    }
                }

            } catch (e: IOException) {
                // 3. Erreur réseau (pas de connexion, timeout, etc.)
                Logger.d("Erreur réseau: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = R.string.error_network
                    )
                }
            } catch (e: Exception) {
                // 4. Autre erreur
                Logger.d("Autre erreur de connexion: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = R.string.error_generic
                    )
                }
            }
        }
    }


}