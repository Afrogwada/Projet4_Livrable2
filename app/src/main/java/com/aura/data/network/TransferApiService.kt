package com.aura.data.network

import com.aura.data.model.TransferRequest
import com.aura.data.model.TransferResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TransferApiService {
    /**
     * Endpoint POST pour effectuer un virement.
     *
     * @param request Le corps de la requête contenant l'expéditeur, le destinataire et le montant.
     * @return TransferResponse (résultat du virement).
     */
    @POST("/transfer")
    suspend fun transfer(@Body request: TransferRequest): TransferResponse
}