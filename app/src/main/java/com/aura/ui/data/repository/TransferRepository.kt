package com.aura.ui.data.repository

import com.aura.ui.data.model.TransferRequest
import com.aura.ui.data.model.TransferResponse
import com.aura.ui.data.network.RetrofitInstance

class TransferRepository {
    private val api = RetrofitInstance.transferApi

    /**
     * Effectue l'appel API pour le virement.
     */
    suspend fun performTransfer(request: TransferRequest): TransferResponse {
        return api.transfer(request)
    }
}