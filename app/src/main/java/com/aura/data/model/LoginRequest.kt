package com.aura.data.model

/**
 * Représente la requête d'authentification envoyée au serveur.
 *
 * @property id Identifiant de l'utilisateur
 * @property password Mot de passe de l'utilisateur
 */
data class LoginRequest(
    val id: String,
    val password: String
)
