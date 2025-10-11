package com.aura.ui.transfer

import androidx.annotation.StringRes

/**
 * Représente l'état de l'interface utilisateur pour l'écran de virement.
 * Contient les valeurs des champs et l'état du bouton.
 */
data class TransferUiState(
    val recipient: String = "",
    val amount: String = "",
    val senderId: String = "", // L'ID de l'expéditeur, passé depuis HomeActivity
    val isTransferButtonEnabled: Boolean = false,
    val isTransferring: Boolean = false, // Indique si le virement est en cours
    val transferSuccess: Boolean? = null, // True si succès, False si échec, null si pas encore tenté
    @StringRes val error: Int? = null // Message d'erreur
)
