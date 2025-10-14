package com.aura.ui.home

import androidx.annotation.StringRes

/**
 * Modélise les différents états de l'écran d'accueil.
 *
 * @property isLoading Indique si une opération de chargement est en cours.
 * @property balance Solde principal à afficher, ou null si non chargé ou en erreur.
 * @property error Ressource string de l'erreur à afficher, ou null si aucune erreur.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val balance: Double? = null,
    val isSuccess: Boolean? = null,
    val userId: String? = null,
    @StringRes val error: Int? = null // Utilise l'ID de la ressource String pour l'erreur
)