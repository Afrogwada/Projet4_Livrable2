package com.aura.ui.network

import com.aura.ui.data.AccountResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface Retrofit pour accéder aux endpoints de l'API.
 */
interface GetAccountsApiService {
    /**
     * Endpoint GET pour récupérer la liste des comptes pour un utilisateur donné.
     *
     * @param userId Identifiant de l'utilisateur
     * @return Liste des AccountResponse
     */
    @GET("/accounts/{userId}")
    suspend fun getAccounts(@Path("userId") userId: String): List<AccountResponse>
}
