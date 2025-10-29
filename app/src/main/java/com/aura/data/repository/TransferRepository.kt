package com.aura.data.repository

import com.aura.data.model.TransferRequest
import com.aura.data.model.TransferResponse
import com.aura.data.network.RetrofitInstance

class TransferRepository {
    private val api = RetrofitInstance.transferApi

    /**
     * Effectue l'appel API pour le virement.
     */
    suspend fun performTransfer(request: TransferRequest): TransferResponse {
        return api.transfer(request)
    }
}