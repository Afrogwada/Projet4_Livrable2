package com.aura.ui.transfer

/**
 * Représente l'état de l'interface utilisateur pour l'écran de virement.
 * Contient les valeurs des champs et l'état du bouton.
 */
data class TransferUiState(
    val recipient: String = "",
    val amount: String = "",
    // L'état de l'activité du bouton, calculé par le ViewModel
    val isTransferButtonEnabled: Boolean = false
)
