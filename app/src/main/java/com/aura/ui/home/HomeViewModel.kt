package com.aura.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.R
import com.aura.data.repository.GetAccountsRepository
import com.aura.data.model.AccountResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel pour l'écran d'accueil. Gère l'état de l'UI et les appels API.
 */
class HomeViewModel(
    private val repository: GetAccountsRepository = GetAccountsRepository()
) : ViewModel() {

    // ---------------------------------------------------------------------------------------------
    // Propriétés Exposées
    // ---------------------------------------------------------------------------------------------

    // État de l'UI exposé à l'activité/fragment.
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ---------------------------------------------------------------------------------------------
    // Fonctions d'Action (Intentions de l'UI)
    // ---------------------------------------------------------------------------------------------

    /**
     * Rafraîchit les données en utilisant l'ID utilisateur actuellement stocké.
     */
    fun refresh() {
        uiState.value.userId?.let { loadUserAccounts(it) }
    }

    /**
     * Lance le processus de chargement des comptes pour un utilisateur donné.
     * @param userId L'ID de l'utilisateur.
     */
    fun loadUserAccounts(userId: String) {

        viewModelScope.launch {
            startLoadingState(userId) // Mettre à jour l'état de chargement

            try {
                val accounts = repository.getAccounts(userId)
                handleSuccess(userId, accounts) // Gérer le succès et mettre à jour le solde
            } catch (e: HttpException) {
                handleHttpError(e) // Gérer les erreurs HTTP (4xx, 5xx)
            } catch (e: IOException) {
                handleNetworkError(e) // Gérer les erreurs réseau
            } catch (e: Exception) {
                handleGenericError(e) // Gérer toutes les autres erreurs
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Fonctions de Logique Interne (Découpage de la gestion de l'état et des erreurs)
    // ---------------------------------------------------------------------------------------------

    /**
     * Met à jour l'état pour indiquer le début du chargement.
     */
    private fun startLoadingState(userId: String) {
        _uiState.update { it.copy(isLoading = true, error = null, userId = userId) }
    }

    /**
     * Gère le résultat réussi de l'appel API.
     * @param accounts La liste des comptes reçus.
     */
    private fun handleSuccess(userId: String, accounts: List<AccountResponse>) {
        val mainAccount = accounts.find { it.isMain }

        _uiState.update {
            it.copy(
                isLoading = false,
                isSuccess = true,
                userId = userId,
                balance = mainAccount?.balance // Affiche le solde du compte principal
            )
        }
    }

    /**
     * Gère les erreurs de type HttpException (erreurs côté serveur).
     */
    private fun handleHttpError(e: HttpException) {
        val errorMessageRes = when (e.code()) {
            404 -> R.string.error_user_not_found
            else -> R.string.error_generic
        }
        updateErrorState(errorMessageRes)
        e.printStackTrace()
    }

    /**
     * Gère les erreurs de type IOException (problèmes de connexion).
     */
    private fun handleNetworkError(e: IOException) {
        updateErrorState(R.string.error_network)
        e.printStackTrace()
    }

    /**
     * Gère toutes les autres exceptions non prévues.
     */
    private fun handleGenericError(e: Exception) {
        updateErrorState(R.string.error_generic)
        e.printStackTrace()
    }

    /**
     * Fonction utilitaire pour mettre à jour l'état en cas d'erreur.
     */
    private fun updateErrorState(errorResId: Int) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSuccess = false,
                error = errorResId,
                balance = null // Effacer le solde en cas d'erreur
            )
        }
    }
}