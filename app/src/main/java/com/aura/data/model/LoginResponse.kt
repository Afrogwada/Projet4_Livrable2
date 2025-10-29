package com.aura.data.model


/**
 * Représente la réponse du serveur après une tentative de connexion.
 *
 * @property granted Indique si l'accès est accordé
 */
data class LoginResponse(
    val granted: Boolean,
    val userId: String?
)

