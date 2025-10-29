package com.aura.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.aura.data.model.TransferRequest
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import com.aura.data.repository.TransferRepository
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TransferViewModel(
    private val repository: TransferRepository = TransferRepository()
):ViewModel() {

    // ---------------------------------------------------------------------------------------------
    // Propriétés et StateFlows Internes
    // ---------------------------------------------------------------------------------------------

    private val _uiState = MutableStateFlow(TransferUiState())

    // MutableStateFlows pour les champs de saisie
    private val recipient: MutableStateFlow<String> = MutableStateFlow("")
    private val amount: MutableStateFlow<String> = MutableStateFlow("")

    // ---------------------------------------------------------------------------------------------
    // Propriétés Exposées (Observables)
    // ---------------------------------------------------------------------------------------------

    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    /**
     * StateFlow calculé qui détermine si le bouton de virement est activé.
     */
    val isTransferButtonEnabled: StateFlow<Boolean> = createTransferButtonEnabledFlow()

    // ---------------------------------------------------------------------------------------------
    // Fonctions d'Entrée Utilisateur (Intentions)
    // ---------------------------------------------------------------------------------------------

    fun setSenderId(id: String) {
        _uiState.update { it.copy(senderId = id) }
    }

    /** Met à jour la valeur du destinataire et réinitialise l'état d'erreur. */
    fun setRecipient(newRecipient: String) {
        recipient.value = newRecipient
        resetTransferStateOnError()
    }

    /** Met à jour la valeur du montant et réinitialise l'état d'erreur. */
    fun setAmount(newAmount: String) {
        amount.value = newAmount
        resetTransferStateOnError()
    }

    /**
     * Tente d'effectuer le virement après avoir validé les entrées.
     */
    fun transfer() {
        if (!validateAmount()) {
            return
        }
        startTransferAttempt()
    }

    // ---------------------------------------------------------------------------------------------
    // Fonctions de Logique Interne (Découpage de 'transfer')
    // ---------------------------------------------------------------------------------------------

    /**
     * Crée le StateFlow pour l'activation du bouton de virement.
     */
    private fun createTransferButtonEnabledFlow(): StateFlow<Boolean> = combine(recipient, amount, _uiState) { rec, amt, state ->
        // Le bouton est activé seulement si les deux champs sont remplis et qu'aucun transfert n'est en cours
        rec.isNotBlank() && amt.isNotBlank() && !state.isTransferring
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    /**
     * Vérifie la validité du montant avant de lancer le processus de virement.
     * @return true si le montant est valide, false sinon.
     */
    private fun validateAmount(): Boolean {
        val amountAsDouble = amount.value.toDoubleOrNull()

        if (amountAsDouble == null || amountAsDouble <= 0.0) {
            _uiState.update {
                it.copy(
                    error = R.string.amount_invalid,
                    transferSuccess = false
                )
            }
            return false
        }
        return true
    }

    /**
     * Initialise l'état de l'UI pour un nouveau virement et lance la coroutine.
     */
    private fun startTransferAttempt() {
        // 1. Début du virement (Loading)
        _uiState.update {
            it.copy(
                isTransferring = true,
                transferSuccess = null,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                performTransferRequest()
            } catch (e: IOException) {
                handleNetworkError()
            } catch (e: Exception) {
                handleGenericError()
            }
        }
    }

    /**
     * Exécute l'appel API de virement et gère la réponse du serveur.
     */
    private suspend fun performTransferRequest() {
        val ui = _uiState.value
        val amountValue = amount.value.toDoubleOrNull() ?: throw IllegalStateException("Amount validation failed")

        val request = TransferRequest(
            sender = ui.senderId,
            recipient = recipient.value,
            amount = amountValue
        )

        val response = repository.performTransfer(request)

        // 2. Virement réussi/échoué (du point de vue du serveur)
        if (response.result) {
            _uiState.update { it.copy(isTransferring = false, transferSuccess = true) }
        } else {
            _uiState.update {
                it.copy(
                    isTransferring = false,
                    transferSuccess = false,
                    error = R.string.transfert_failure
                )
            }
        }
    }

    /**
     * Gère les erreurs de réseau (IOException).
     */
    private fun handleNetworkError() {
        updateErrorState(R.string.error_network)
    }

    /**
     * Gère toutes les autres exceptions non prévues.
     */
    private fun handleGenericError() {
        updateErrorState(R.string.error_generic)
    }

    /**
     * Fonction utilitaire pour mettre à jour l'état en cas d'échec.
     */
    private fun updateErrorState(errorResId: Int) {
        _uiState.update {
            it.copy(
                isTransferring = false,
                transferSuccess = false,
                error = errorResId
            )
        }
    }

    /**
     * Réinitialise l'état d'erreur et de succès lorsque l'utilisateur modifie les entrées.
     */
    private fun resetTransferStateOnError() {
        _uiState.update { it.copy(transferSuccess = null, error = null) }
    }
}