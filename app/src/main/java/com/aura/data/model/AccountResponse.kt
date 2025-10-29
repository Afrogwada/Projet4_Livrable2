package com.aura.data.model

import com.squareup.moshi.Json

/**
 * Représente la réponse du serveur pour un compte.
 *
 * @property id ID du compte
 * @property isMain Indique si c'est le compte principal
 * @property balance Solde actuel du compte
 */

data class AccountResponse(
    val id: String,
    @Json(name = "main") val isMain: Boolean,
    val balance: Double
)