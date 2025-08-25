package com.aura.ui.login

/**
 * Représente l'état de l'interface utilisateur pour l'écran de connexion.
 *
 * @property isLoading indique si une opération de connexion est en cours.
 * @property isSuccess null si pas encore de tentative, true si connexion réussie, false si échec.
 * @property errorMessage contient un message d'erreur si la connexion échoue, sinon null.
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean? = null,
    val errorMessage: String? = null
)
