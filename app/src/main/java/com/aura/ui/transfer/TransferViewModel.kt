package com.aura.ui.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class TransferViewModel : ViewModel() {

    // MutableStateFlows pour les champs de saisie
    private val recipient : MutableStateFlow<String> = MutableStateFlow("")
    private val amount : MutableStateFlow<String> = MutableStateFlow("")

    /**
     * StateFlow calculé qui détermine si le bouton de virement est activé.
     * Le bouton est activé seulement si recipient ET amount sont non vides.
     */
    val isTransferButtonEnabled: StateFlow<Boolean> = combine(recipient, amount) { rec, amt ->
        // Critère d'acceptation 1 : Les deux champs doivent être remplis (non vides)
        rec.isNotBlank() && amt.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false // Le bouton est désactivé par défaut
    )

    /** Met à jour la valeur du destinataire et déclenche la re-validation. */
    fun setRecipient(newRecipient: String) {
        recipient.value = newRecipient
    }

    /** Met à jour la valeur du montant et déclenche la re-validation. */
    fun setAmount(newAmount: String) {
        amount.value = newAmount
    }

    fun performTransfer() {
        // Logique de virement réel
        // TODO: Mettre en œuvre l'appel API pour le virement
    }
}