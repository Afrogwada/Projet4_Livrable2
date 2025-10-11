package com.aura.ui.data.model

/**
 * Représente la réponse du serveur après le virement.
 *
 * @property result Indique si le virement a réussi (true) ou échoué (false).
 */
data class TransferResponse(
    val result: Boolean
)
