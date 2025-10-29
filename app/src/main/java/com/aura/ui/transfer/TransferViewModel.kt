package com.aura.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.R
import com.aura.ui.Logger
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

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    // MutableStateFlows pour les champs de saisie
    private val recipient: MutableStateFlow<String> = MutableStateFlow("")
    private val amount: MutableStateFlow<String> = MutableStateFlow("")

    /**
     * StateFlow calculé qui détermine si le bouton de virement est activé.
     * Le bouton est activé seulement si recipient ET amount sont non vides.
     */
    val isTransferButtonEnabled: StateFlow<Boolean> = combine(recipient, amount) { rec, amt ->
        // Critère d'acceptation 1 : Les deux champs doivent être remplis (non vides)
        rec.isNotBlank() && amt.isNotBlank() && !_uiState.value.isTransferring
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false // Le bouton est désactivé par défaut
    )

    fun setSenderId(id: String) {
        _uiState.update { it.copy(senderId = id) }
    }


    /** Met à jour la valeur du destinataire et déclenche la re-validation. */
    fun setRecipient(newRecipient: String) {
        recipient.value = newRecipient
    }

    /** Met à jour la valeur du montant et déclenche la re-validation. */
    fun setAmount(newAmount: String) {
        amount.value = newAmount
    }

    fun transfer() {
        val senderId = _uiState.value.senderId
        val recipientId = recipient.value
        val amountValue = amount.value
        //Logger.d("Nouveau transfert de $senderId vers $recipientId")

        // Validation du montant avant l'appel API
        val amountAsDouble = amountValue.toDoubleOrNull()
        //Logger.d("pour un montant de  $amountAsDouble")
        if (amountAsDouble == null || amountAsDouble <= 0.0) {
            _uiState.update {
                it.copy(
                    error = R.string.amount_invalid,
                    transferSuccess = false
                )
            }
            return
        }

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
                val request = TransferRequest(
                    sender = senderId,
                    recipient = recipientId,
                    amount = amountAsDouble
                )
                //Logger.d("début du transfert")
                val response = repository.performTransfer(request)

                // 2. Virement réussi/échoué (du point de vue du serveur)
                if (response.result) {
                    _uiState.update {
                        it.copy(
                            isTransferring = false,
                            transferSuccess = true
                        )
                    }
                    //Logger.d("transfert ok")
                } else {
                    _uiState.update {
                        it.copy(
                            isTransferring = false,
                            transferSuccess = false,
                            error = R.string.transfert_failure
                        )
                    }
                }

            } catch (e: IOException) {
                // Erreur réseau
                _uiState.update {
                    it.copy(
                        isTransferring = false,
                        transferSuccess = false,
                        error = R.string.error_network
                    )
                }
            } catch (e: Exception) {
                // Autre erreur
                _uiState.update {
                    it.copy(
                        isTransferring = false,
                        transferSuccess = false,
                        error = R.string.error_generic
                    )
                }
            }
        }
    }
}
