package com.aura.data.model

/**
 * Représente le corps de la requête POST /transfer.
 *
 * @property sender ID de l'expéditeur
 * @property recipient ID du destinataire
 * @property amount Montant à transférer
 */
data class TransferRequest(
    val sender: String,
    val recipient: String,
    val amount: Double
)