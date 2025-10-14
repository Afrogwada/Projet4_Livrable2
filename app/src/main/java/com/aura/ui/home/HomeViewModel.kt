package com.aura.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.R
import com.aura.ui.data.repository.GetAccountsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.delay



/**
 * ViewModel pour l'écran d'accueil. Gère l'état de l'UI et les appels API.
 */
class HomeViewModel(
    private val repository: GetAccountsRepository = GetAccountsRepository()
) : ViewModel() {

    // État de l'UI exposé à l'activité/fragment.
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // L'ID utilisateur doit être récupéré après une authentification réussie.
    // private set = lecture seule de l'extérieur
    var currentUserId: String = ""
        private set


    /**
     * Tente de charger les comptes et met à jour l'état de l'UI.
     * Gère les différents cas d'erreur.
     */
    fun loadUserAccounts(userId: String) {
        /*// Si l'ID est le même et qu'il y a déjà un solde, ne rien faire (optimisation)
        if (this.currentUserId == userId && uiState.value.balance != null && !uiState.value.isLoading) {
            return
        }*/

        this.currentUserId = userId

        viewModelScope.launch {
            // 1. État de Chargement (Loading)
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                //delay(5000L) pour tester le loading
                // 2. Appel API (via le Repository)
                val accounts = repository.getAccounts(userId)
                val mainAccount = accounts.find { it.isMain }

                // 3. État de Succès (Success)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        userId =userId,
                        balance = mainAccount?.balance // Affiche le solde du compte principal
                    )
                }

            } catch (e: HttpException) {
                // 4. Gestion des Erreurs HTTP (Ex: 404 Not Found, 401 Unauthorized)
                val errorMessageRes = when (e.code()) {
                    404 -> R.string.error_user_not_found // Utilisateur incorrect
                    else -> R.string.error_generic // Autres erreurs HTTP
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = false, error = errorMessageRes) }
                e.printStackTrace()

            } catch (e: IOException) {
                // 5. Gestion des Erreurs Réseau (Ex: Pas de connexion Internet, Timeout)
                _uiState.update { it.copy(isLoading = false, isSuccess = false, error = R.string.error_network) }
                e.printStackTrace()
            } catch (e: Exception) {
                // 6. Gestion des Autres Erreurs
                _uiState.update { it.copy(isLoading = false, isSuccess = false, error = R.string.error_generic) }
                e.printStackTrace()
            }
        }
    }
}